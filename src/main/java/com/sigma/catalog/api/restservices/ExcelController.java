package com.sigma.catalog.api.restservices;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

import com.sigma.catalog.api.exception.PythonExecutionException;
import com.sigma.catalog.api.python.PythonHelper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/excel")
public class ExcelController {
  private static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  private static boolean hasExcelFormat(MultipartFile file) {
    if (!TYPE.equals(file.getContentType())) {
      return false;
    }
    return true;
  }

  private static ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObj) {
    Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", message);
        map.put("status", status.value());
       // map.put("data", responseObj);
        return new ResponseEntity<Object>(map,status);
  }

  private static void loadFileInDb(Map<String, MultipartFile> fileMap) throws Exception{
    Function<String, String> capitalizeFirstLetter = (string) -> string.substring(0, 1).toUpperCase() + string.substring(1);
    for(Map.Entry<String, MultipartFile> entry: fileMap.entrySet()){
      MultipartFile file = entry.getValue();
      String fileCategory = entry.getKey();
      final String excelFileName = file.getOriginalFilename();
      try {
        File pythonLoader = ResourceUtils.getFile("classpath:" + "python/" + "read-job.py");
        Path excelFolderPath = pythonLoader.toPath().getParent().resolve("temp-excel");
  
        File tempFile = new File(excelFolderPath.toFile(), file.getOriginalFilename());
        tempFile.createNewFile();
        file.transferTo(tempFile);
        // String tableName = excelFileName.substring(0, excelFileName.length() - 5).replaceAll(" ", "_");
        String jobId = capitalizeFirstLetter.apply(fileCategory)+"_Job_"+Integer.toString(ThreadLocalRandom.current().nextInt(1000, 9999)); // + "_" + tableName;
        try {
          new Thread(new Runnable() {
            public void run() {
              try {
                new PythonHelper(jobId, excelFileName).insertJobExternal();
              } catch (PythonExecutionException e) {
                Thread t = Thread.currentThread();
                t.getUncaughtExceptionHandler().uncaughtException(t, e);
              }finally {
                tempFile.delete();
              }
            }
          }).start();
        } catch (Exception e) {
          throw e;
        } 
      } catch (Exception e) {
        throw e;
      }
    }
  }

  @PostMapping("/upload")
  public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
    String message = "";
    if (ExcelController.hasExcelFormat(file)) {
      final String excelFileName = file.getOriginalFilename();
      try {
        File pythonLoader = ResourceUtils.getFile("classpath:" + "python/" + "read-job.py");
        Path excelFolderPath = pythonLoader.toPath().getParent().resolve("temp-excel");

        File tempFile = new File(excelFolderPath.toFile(), file.getOriginalFilename());
        tempFile.createNewFile();
        file.transferTo(tempFile);
        // String tableName = excelFileName.substring(0, excelFileName.length() - 5).replaceAll(" ", "_");
        String jobId = "Job_"+Integer.toString(ThreadLocalRandom.current().nextInt(1000, 9999)); // + "_" + tableName;
        try {
          new PythonHelper(jobId, excelFileName).insertJobExternal();
        } catch (Exception e) {
          return ExcelController.generateResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
        } finally {
          tempFile.delete();
        }
        message = "Uploaded the file successfully: " + excelFileName;
        return ExcelController.generateResponse(message, HttpStatus.OK, null);
      } catch (Exception e) {
        message = "Could not upload the file: " + excelFileName + "!";
        return ExcelController.generateResponse(e.getMessage(), HttpStatus.EXPECTATION_FAILED, null);
      }
    }

    message = "Please upload an excel file!";
    return ExcelController.generateResponse(message, HttpStatus.BAD_REQUEST, null);
  }

  @PostMapping("/uploadMultiple")
  public ResponseEntity<Object> uploadFile(@RequestParam(value = "BundleCategory", required = false) MultipartFile bundle,
    @RequestParam(value = "ComponentCategory", required = false) MultipartFile component, 
    @RequestParam(value = "RatingPlanCategory", required = false) MultipartFile ratingPlan,
    @RequestParam(value = "RatingPlanDetailCategory", required = false) MultipartFile ratingPlanDetailCategory,
    @RequestParam(value = "PriceChangeCategory", required = false) MultipartFile priceChangeCategory) {
    Map<String, MultipartFile> fileMap = new HashMap<>();
    Predicate<MultipartFile> isValidParams = (MultipartFile file) -> (file != null) && ExcelController.hasExcelFormat(file);
    if (isValidParams.test(bundle)){
      fileMap.put("BundleCategory", bundle);
    }
    if (isValidParams.test(component)){
      fileMap.put("ComponentCategory", component);
    }
    if (isValidParams.test(ratingPlan)){
      fileMap.put("RatingPlanCategory", ratingPlan);
    }
    if (isValidParams.test(ratingPlanDetailCategory)){
      fileMap.put("RatingPlanDetailCategory", ratingPlanDetailCategory);
    }
    if (isValidParams.test(priceChangeCategory)){
      fileMap.put("PriceChangeCategory", priceChangeCategory);
    }
    try {
      loadFileInDb(fileMap);
    } catch (Exception e) {
      return ExcelController.generateResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null);
    }
    String message = "Uploaded the file(s) successfully";
    return ExcelController.generateResponse(message, HttpStatus.OK, null);
  }
}

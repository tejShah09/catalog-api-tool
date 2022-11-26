/**
 * Home Controller
 * 
 */
reportModule.controller("MainController", function ($scope, UtilService, ReportService,$loading,$timeout) {
    $scope.formModel = {};
    $scope.formModel.operType = 'Export';
    $scope.formModel.isLinked = false;

    $scope.downloadBtnFlag = true;
    $scope.progressBarFlag = false;
    $scope.responseFileName="";

    $scope.exportedFile = null;
    $scope.importFile = null;
    $scope.info = "If Product linked to Changeset ,you have to provide either  provide Changeset Name or select file for Changeset";

    $scope.catalogOperations = UtilService.getCatlogOperations($scope.formModel);
    $scope.templateNames = UtilService.gettemplateNames();
    clearStatusMessage();

    $scope.radioChange = function (type) {  
    	$scope.downloadBtnFlag = true;
        $scope.clearForm();
        $scope.catalogOperations = UtilService.getCatlogOperations($scope.formModel);
    };

    $scope.checkboxChange = function () {
        console.log($scope.formModel.isLinked);
        $scope.formModel.changesetName ='';
        $scope.formModel.changesetFile = null;
        //$scope.formModel.isLinked = !$scope.formModel.isLinked;
    }; 
    
    $scope.onCatalogOperationChange = function () {
    	resetDownloadBtn(); 
    	if($scope.formModel.operType =='Import' && $scope.formModel.catalogOperation =='Allowance Definition'){
    		$scope.formModel.templateName='';
    	}
    };
    
    $scope.onTemplateChange = function () {
    	resetDownloadBtn();
    };
   

    $scope.isDisableRunBtn = function () {       
        if ($scope.formModel && $scope.formModel.operType == 'Export' && $scope.formModel.catalogOperation && $scope.formModel.templateName) {
            return false;
        } else if ($scope.formModel && $scope.formModel.operType == 'Import' && $scope.formModel.catalogOperation
             && $scope.formModel.locFile && $scope.formModel.locFile.name) { 
        	
	        	if($scope.formModel.catalogOperation =='Allowance Definition'){
	        		 if ($scope.formModel.isLinked) {
	 	                if ($scope.formModel.changesetName || $scope.formModel.changesetFile) {
	 	                    return false;
	 	                } else {
	 	                    return true;
	 	                }
	 	            } else {
	 	                return false;
	 	            }
	        	}else{
	        		if($scope.formModel.catalogOperation !='Allowance Definition' && $scope.formModel.templateName){
	        			if ($scope.formModel.isLinked) {
		 	                if ($scope.formModel.changesetName || $scope.formModel.changesetFile) {
		 	                    return false;
		 	                } else {
		 	                    return true;
		 	                }
		 	            } else {
		 	                return false;
		 	            }
	        		}else{
	        			return true;
	        		}
	        	}
	        	
	           
        } else {
            return true;
        }       
    };
    
    $scope.run = function (operType) {
    	 $loading.start('processingBar');
    	 console.log("Operation" + $scope.formModel)
        if (operType == 'Export') {
            ReportService.getExportedFile($scope.formModel).then( function (data) {
            	$loading.finish('processingBar');
            	$scope.exportedFile = data;
            	$scope.downloadBtnFlag = false;
            	 $scope.responseFileName=$scope.formModel.locFile.name;
            	$scope.clearForm();
            	displayMessage('Export executed successfully','alert alert-success');
           },function (data) {
        	   $loading.finish('processingBar');
                console.log("Error while exporting file: ");
                console.log(data);
                $scope.downloadBtnFlag = true;
                displayMessage('Export Failed','alert alert-danger');                
           });
        } else { 
        	console.log($scope.formModel);
            ReportService.importExcelFile($scope.formModel).then( function (data) {
               $loading.finish('processingBar');
               $scope.importFile = data; 
               $scope.downloadBtnFlag = false;
               $scope.responseFileName=$scope.formModel.locFile.name;
               $scope.clearForm();
               displayMessage('Import executed successfully','alert alert-success'); 
              // $('#file_upload').val('');
          }, function (data) {
        	   $loading.finish('processingBar');
                console.log("Error while importing file : - ");
                console.log(data);
                $scope.downloadBtnFlag = true;
                displayMessage('Import Failed','alert alert-danger');
           });
        }
    };    

    $scope.download = function (operType) {
    	$scope.downloadBtnFlag = true;
        var excelFileData = null;
        if (operType == 'Export') {
            excelFileData = $scope.exportedFile;           
        } else {
            excelFileData = $scope.importFile;
        }       
        if (excelFileData) {
  	      try {
  	        if (excelFileData && navigator.msSaveBlob) {
  	          return navigator.msSaveBlob(new Blob([excelFileData], { type: 'application/vnd.ms-excel' }), 'report.xlsx');
  	        }
  	        var file = new Blob([excelFileData], {
  	          type: 'application/vnd.ms-excel'
  	        });
  	        var fileURL = URL.createObjectURL(file);
  	        var a = document.createElement('a');
  	        a.href = fileURL;
  	        a.target = '_blank';
  	        if($scope && $scope.responseFileName){
  	        a.download = 'Report_' + $scope.responseFileName;
  	        }
  	        else{
  	        	a.download = 'Export_' + $scope.formModel.catalogOperation + '.xlsx';
  	        }
  	        document.body.appendChild(a);
  	        a.click();
  	        console.log("File successfully downloaded ");
  	        displayMessage('File successfully downloaded','alert alert-success');  	       
  	      } catch (e) {
  	        console.log("File not downloaded ");
  	        displayMessage('Download Failed','alert alert-danger');
  	      }
  	    }else{
  	    	displayMessage('No file found for download','alert alert-warning');
  	    }
    };    

    $scope.clearForm = function () {
        var type = $scope.formModel.operType;
        $scope.formModel = {};
        $scope.formModel.operType = type;
        $scope.formModel.isLinked = false;
        if(type == 'Import'){
        	 $('#file_upload').val('');
        }
    };     
    
   function clearStatusMessage() {		
		$scope.msgDiv = false;
		$scope.msgClass = '';
		$scope.statusMessage = '';
	};	

	function displayMessage(message,classType) {		
		 $scope.msgDiv = true;
		 $scope.msgClass = classType;
		 $scope.statusMessage = message;	
	};	
	
	function resetDownloadBtn() {		
		$scope.downloadBtnFlag = true; 
    	$scope.exportedFile = null;
        $scope.importFile = null;	
	};	
	
	$scope.$watch("statusMessage", function(newValue, oldValue) {
		 if (newValue !== oldValue && (newValue.trim().length > 0)) {
		    	$timeout(function(){		    		
		    		 clearStatusMessage();
		    	},12000);
		 }   
	});
	
});
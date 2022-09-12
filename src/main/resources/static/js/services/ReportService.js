/**
 * Report  Service
 * 
 */
reportModule.factory('ReportService', function ($q, $http) {
	var reportService = {}; 	
	
	var reportConfig = {
		headers : {
			'Content-Type' : 'application/json'						
		},
		responseType: "arraybuffer"
	};
	
	 var uploadConfig = {
	    transformRequest: angular.identity,	   
		headers : {
		   	'Content-Type': undefined
		},
		responseType : "arraybuffer"
	}

    reportService.getExportedFile = function (formObj) {
		 var entNms = null;
		 if(formObj.entityNames){
			 entNms = formObj.entityNames; 
		 }
    	reportConfig.params = {'templateName':formObj.templateName ,'catalogOperation':formObj.catalogOperation ,'entityNames':entNms};	   
	    return $q(function (resolve, reject) {
	      $http.get('/export',reportConfig).success(function (response) {
	        if (response) {	           
	           resolve(response);
	        }
	      }).error(function (data, status, header, config) {	      
	        console.log('Error occured while getting xlsx file in export case' + data);
	        console.log(data);	        
	        reject(data);
	      });
	    });
     }
  
	  reportService.importExcelFile = function (formObj) {		 
		   var formdata = new FormData();
		   formdata.append('file', formObj.locFile);
		   formdata.append('templateName', formObj.templateName);
		   formdata.append('catalogOperation', formObj.catalogOperation);
	       if(formObj.isLinked){
	    	   formdata.append('changesetName', formObj.changesetName);
	    	   formdata.append('changesetFile', formObj.changesetFile);  
	       }else{
	    	   formdata.append('changesetName', null);
	    	   formdata.append('changesetFile', null); 
	       }      
	       
		    return $q(function (resolve, reject) {
		      $http.post('/import', formdata,uploadConfig).success(function (response) {
		        if (response) {
		           console.log('Import success block');
		           resolve(response);
		        }
		      }).error(function (data, status, header, config) {		      
		        console.log('Error occured whilegetting file in  importing ' + data);
		        console.log(data);		        
		        reject(data);
		      });
		    });
	  }
	

   return reportService;
  
});







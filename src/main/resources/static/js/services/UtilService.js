/**
 * Util Service
 * 
 */
reportModule.factory('UtilService', function() {
	var utilService = {};

	var exportCtlgOper = [ 
				'Entity',		
				'Associations',
				'DiscountRelations',
				'Rates',
				'Characteristic Overrides',
				'TTreeBranch',
				'TTreeLeaf',
				'Lookup',
				'Entity_all_details'
				];

	var importCtlgOper = [ 
				'Entity',
				'Rates',
				'Lookup',
				'Associations',
				'DiscountRelations',
				'Change Status',
				'Characteristic Overrides',
				'TTreeBranch',
				'TTreeLeaf'
			  ];

	var templateNames = [ 
			'TBranch Classification',
			'TProduct_Base_Physical_Branch',
			'TProduct Group Branch',
			'TFeed In Type',
			'TCustomer Type',
			'Lookup Country',
			'Lookup Reward Plan ',
			'Lookup_Discount_Group',
			'Lookup Campaign Offer',
			'Lookup Time Set',
			'Lookup Meter Provider Elec Registered',
			'All Lookup Templates',
			'All TTreeBranch Templates',
			'All TTreeLeaf Templates',
			'OrderCharItem',
			'Package Template - Mobile',
			'Test Package Template',
			'Default Package Template',
			'Default Component Group',
			'Package Template - Internet',	
			'SpecCharValueItem',
			'Default Changeset',
			'Composite Product Offering Voice',
			'Postpaid Product Specification Simple',
			'Electricity Usage Rating Plan',
			'Tiered Recurring Rates',
			'Rating Energy Usage',
			'Rating Demand',
			'Rating Plan Property Set for Agreed Capacity',
			'Rating Plan Property Set for Gas Usage',
			'Rating Plan Property Set for Gas Usage Units',
			'Rating Plan Property Set for Graduated Quantity',
			'Rating Plan Property Set for Spot Price',
			'All Rating Plan Templates',
			'Discount Electricity Pension Property Set',
			'Discount Loyalty Property Set',
			'Discount Plan EEC',
			'Discount Plan RATE REDUCTION',
			'Discount Plan SPC',
			'Discount Plan TBAND',
			'Discount Plan WEC',
			'Discount Plan Fixed',
			'Discount Plan Pending Direct Debit Credit Payment',
			'Discount Plan Pending Prompt Payment',
			'Discount Promotional Property Set',
			'Discount Plan Variable Usage',
			'Discount Plan FCONS',
			'Discount Plan Variable Discount',
			'Discount Plan TimeBand No Concession',
			'Discount Plan PERCEN',
			'All Discount Plan Templates',
			'Product Base Property Set',
			'Product Bundle Property Set',
			'Product Bundle Physical Property Set',
			'Product Base Physical Property Set',
			'All Product Templates',
			'TRetailer',
			'Offer Residential',
			'Offer Commercial',
			'Reference Package',
			'Default',
			'iconnectivity Link Template'
			
						];

	utilService.getCatlogOperations = function(dataObj) {
		if (dataObj.operType == 'Export') {
			return exportCtlgOper;
		} else {
			return importCtlgOper;
		}
	}

	utilService.gettemplateNames = function() {
		return templateNames;
	}

	return utilService;
});
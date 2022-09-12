-- query to create dymic json


select replace (eaaaa."mainquery",'{{JobId}}','JOB_e156') as "mainquery",eaaaa."sheetNames" from (
select 'select row_to_json(t) as Entity , t.id from ( select '||  (select string_agg(cl,',') from ( select 'ent."'||trim(unnest(string_to_array(msr."coloums",','))) ||'"'  as cl ) cls)|| 
	' , '|| (select string_agg (childquery,' , ') from (SELECT  '( select json_agg('|| REPLACE(sr."sheetName",' ','') ||') from (select '|| sr."coloums" ||' from "'|| sr."sheetTable" ||'" where '||  string_agg( '"'||sr."sheetTable"||'"."'|| sc."sheetColumn"||'" = "'  
		|| 'ent' || '"."'|| sc."parentSheetColumn" || '"' ,' AND ')|| ') ' ||  REPLACE("sheetName",' ','') ||') as "'|| sr."sheetName" ||'"'  as childquery
	FROM public."SheetRelation" sr ,"SheetRelationColumLink" scl, "SheetRelationColumn" sc where sc.id = scl."sheetColoumID" and  sr.id = scl."sheetRelationID"	and sr."mainSheet" = msr."sheetTable"
	group by sr."sheetTable", sr."sheetName", sr."mainSheet",sr."coloums") as chilscema)
|| ' from "'|| msr."sheetTable"||'" ent) t'  as "mainquery" , (select string_agg(ssr."sheetName",',') from  "SheetRelation" ssr where ssr."mainSheet"= msr."sheetTable")  as "sheetNames" from "SheetRelation" msr
 where msr."sheetName"= 'Entity' and msr."group" = 'RatePlanDetail') as eaaaa


 "select replace (eaaaa.\"mainquery\",'{{JobId}}','\"+context.JobId+\"') as \"mainquery\",eaaaa.\"sheetNames\" from (
select 'select row_to_json(t) as Entity , t.id from ( select '||  (select string_agg(cl,',') from ( select 'ent.\"'||trim(unnest(string_to_array(msr.\"coloums\",','))) ||'\"'  as cl ) cls)|| 
	' , '|| (select string_agg (childquery,' , ') from (SELECT  '( select json_agg('|| REPLACE(sr.\"sheetName\",' ','') ||') from (select '|| sr.\"coloums\" ||' from \"'|| sr.\"sheetTable\" ||'\" where '||  string_agg( '\"'||sr.\"sheetTable\"||'\".\"'|| sc.\"sheetColumn\"||'\" = \"'  
		|| 'ent' || '\".\"'|| sc.\"parentSheetColumn\" || '\"' ,' AND ')|| ') ' ||  REPLACE(\"sheetName\",' ','') ||') as \"'|| sr.\"sheetName\" ||'\"'  as childquery
	FROM public.\"SheetRelation\" sr ,\"SheetRelationColumLink\" scl, \"SheetRelationColumn\" sc where sc.id = scl.\"sheetColoumID\" and  sr.id = scl.\"sheetRelationID\"	and sr.\"mainSheet\" = msr.\"sheetTable\"
	group by sr.\"sheetTable\", sr.\"sheetName\", sr.\"mainSheet\",sr.\"coloums\") as chilscema)
|| ' from \"'|| msr.\"sheetTable\"||'\" ent) t'  as \"mainquery\" , (select string_agg(ssr.\"sheetName\",',') from  \"SheetRelation\" ssr where ssr.\"mainSheet\"= msr.\"sheetTable\")  as \"sheetNames\" from \"SheetRelation\" msr
 where msr.\"sheetName\"= 'Entity' and msr.\"group\" ='\"+context.group+\"' ) as eaaaa"


 ________________________________


 -- query to find all lookup

 "select s1.OmniformName  as Base_Class, s.OmniformName as ClassType, e.InstanceClassGUID as Public_ID,  e.ElementValue as Name, e2.ElementValue as Description from InstanceElementValues e,InstanceElementValues e2, SchemaClass s,SchemaClass s1 where e.SchemaClassGUID = s.GUID and s.InheritedGUID = s1.guid  and s1.OmniformName = 'Lookup' and e2.InstanceClassGUID=e.InstanceClassGUID and e2.SchemaElementGUID= (select GUID from SchemaElement where SchemaClassGUID = s1.GUID and Name = 'Description' ) and e.SchemaElementGUID = (select GUID from SchemaElement where SchemaClassGUID = s1.GUID and Name = 'Name' ) "


 ---- query for all TTBranch
"select s1.OmniformName as Characteristic_Class_Name , e1.InstanceClassGUID as Characteristic_Value_ID,e2.ElementValue as Name , e1.ElementValue as Description,s2.InheritedGUID, (select ElementValue from InstanceElementValues e where e.InstanceClassGUID = e1.InstanceClassGUID and e.SchemaElementGUID =(select GUID from SchemaElement where Name ='Parent' and SchemaClassGUID = e1.SchemaClassGUID ) ) as Parent_GUID, (select ElementValue from InstanceElementValues where SchemaElementGUID =(select GUID from SchemaElement where Name ='Name' and SchemaClassGUID = s2.InheritedGUID ) and InstanceClassGUID = (select ElementValue from InstanceElementValues e where e.InstanceClassGUID = e1.InstanceClassGUID and e.SchemaElementGUID =(select GUID from SchemaElement where Name ='Parent' and SchemaClassGUID = e1.SchemaClassGUID ) )) as ParentName from InstanceElementValues   e1 INNER JOIN InstanceElementValues   e2 on  e1.InstanceClassGUID = e2.InstanceClassGUID ,SchemaClass s1,SchemaClass s2  where s2.OmniformName = 'TTree_Branch'  and e1.SchemaClassGUID = s1.GUID and s1.InheritedGUID = s2.GUID and e2.SchemaElementGUID= (select GUID from SchemaElement where SchemaClassGUID = s2.InheritedGUID and Name = 'Name' )  and e1.SchemaElementGUID = (select GUID from SchemaElement where SchemaClassGUID = s2.InheritedGUID and Name = 'Description' )"


 ---- query for all TTreeLeaf
 "select s1.OmniformName as Characteristic_Class_Name , e1.InstanceClassGUID as Characteristic_Value_ID, e1.ElementValue as Description,e2.ElementValue as Name from InstanceElementValues   e1 INNER JOIN InstanceElementValues   e2 on  e1.InstanceClassGUID = e2.InstanceClassGUID ,SchemaClass s1,SchemaClass s2 where s2.OmniformName = 'TTree_Leaf'  and e1.SchemaClassGUID = s1.GUID and s1.InheritedGUID = s2.GUID and e2.SchemaElementGUID= (select GUID from SchemaElement where SchemaClassGUID = s2.InheritedGUID and Name = 'Name' ) and e1.SchemaElementGUID = (select GUID from SchemaElement where SchemaClassGUID = s2.InheritedGUID and Name = 'Description' )" 

--query guids of rateplan

select * from Entity where TemplateID In (select TemplateID from TemplateCategory where CategoryID = (select CategoryID from Category where Name = 'Rating Plans'))
and Name in (
'ERBCGDCL0713S',
'ERBPGDCL0713S',
'ERBSGDCL0713S',
'ERBUGDCL0713S',
'ERBJGDCL0713S',
'ERBCGDCL0713S',
'ERBPGDCL0713S',
'ERBSGDCL0713S',
'ERBUGDCL0713S',
'ESBJECL0713S' ,
'ESBCECL0713S' ,
'ESBPECL0713S' ,
'ESBSECL0713S' ,
'ESBUECL0713S' )

select json_agg(e) from (select (select json_agg(z) 
	FROM "CategoryID" as z) as "CategoryID",	
(select json_agg(z) 
	FROM "Distributor" as z)as "Distributor",	
(select json_agg(z) 
	FROM "EntityQueries" as z) as "EntityQueries",
(select json_agg(z) 
	FROM "SheetRelation" as z) as "SheetRelation",	
(select json_agg(z) 
	FROM "SheetRelationColumn" as z)as "SheetRelationColumn",	
(select json_agg(z) 
	FROM "SheetRelationColumLink" as z) as "SheetRelationColumLink") as e

-- Rate REportr

select REPLACE(t.Name,' ','_') as 'Parent_Entity_ClassType', 
e.Name as 'Parent_Entity_Name',
e.GUID as 'Parent_Entity_GUID',
(select ElementValue from InstanceElementValues where SchemaElementGUID = '19AEC7BC-B015-47CA-9EA8-F28DB65D50E7' and InstanceClassGUID=( select ElementValue from InstanceElementValues where InstanceClassGUID=re.InstanceClassGUID and SchemaElementGUID = 'F7D4BE48-8A80-4C59-8F39-E5C662763956' )) as 'Charge_Entity_GUID',
(select REPLACE(Name,' ','_') from Entity where GUID =  (select ElementValue from InstanceElementValues where SchemaElementGUID = '19AEC7BC-B015-47CA-9EA8-F28DB65D50E7' and InstanceClassGUID=( select ElementValue from InstanceElementValues where InstanceClassGUID=re.InstanceClassGUID and SchemaElementGUID = 'F7D4BE48-8A80-4C59-8F39-E5C662763956' ))) as 'Rate_Class',
ri.InstanceClassGUID as 'Rate_Row_GUID',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '524CBE72-B71D-4F9A-8222-06A00A96A842' ) as 'Start_Date',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = 'F0443619-DD01-43A6-9FF1-32C3B71773F4' ) as 'End_Date',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = 'C65D7A60-AB4A-4A3F-B06E-42C2E226373D')  as 'PerUnit',
(select ElementValue from InstanceElementValues where SchemaElementGUID = 'C62958F5-36D9-421C-A552-5349C30EE2D1' and InstanceClassGUID = (select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '363C2ACA-F7C6-4D97-AD75-6BF18AADD2DE') ) as 'Time_Band',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '2F029B20-8A8E-47D7-B58B-CB76F42D9527')  as 'Change_ID',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '6999C3DB-F1E1-4FC4-81EA-B6EA0802DDED')  as 'Submit_Date',
(select ElementValue from InstanceElementValues where SchemaElementGUID = 'C62958F5-36D9-421C-A552-5349C30EE2D1' and InstanceClassGUID = (select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '12552B7C-2BE8-4F51-B907-F40F9FB7417F') ) as 'Rating_Period',
e.Name 'Rating_Plan_ID',
ri.ElementValue as 'Rate_Attribute_Guid',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = '7DC046DB-87BF-4D19-A779-DD575C51F7C5' ) as 'Step',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = '7C23F6A9-3A1D-4C85-A444-1D7BDA94A178' ) as 'UnitsLessOrEqualTo',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = 'D738BE96-E773-4F42-BD6B-EB94D1A22587' ) as 'UnitRate',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = 'FE25B002-5A3F-4BE1-82BE-2A20870FC01C' ) as 'Rate_Submit_Date',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = 'EF7160DA-6CBC-437E-9EB2-76B885138653' ) as 'Rate_Change_ID'
from InstanceElementValues ri,InstanceElementValues rr, InstanceElementValues re ,Entity e, Template t
where e.TemplateID =t.TemplateID and  rr.InstanceClassGUID = re.ElementValue and ri.InstanceClassGUID = rr.ElementValue and re.InstanceClassGUID = e.GUID
and t.Name in ('Rating Energy Usage','Rating Demand','Rating Plan Property Set for Agreed Capacity','Rating Plan Property Set for Gas Usage','Rating Plan Property Set for Gas Usage Units','Rating Plan Property Set for Graduated Quantity','Rating Plan Property Set for Spot Price')
and ri.SchemaElementGUID = '335C7019-17D4-4B1F-8888-5A8326AEF092'
and rr.SchemaElementGUID = '576AEF89-1C17-49A8-87A7-CBF24DAD4DDB'
and re.SchemaElementGUID = '9EFF8CC4-9318-463B-96DA-AEC0EDE039CA'


--- recon offer id
select REPLACE(t.Name,' ','_') as 'EntityType', e.Name as "EntityName",e.GUID as "GUID",(select ElementValue from InstanceElementValues i  where    i.InstanceClassGUID = e.GUID and i.SchemaElementGUID in (select e1.GUID from SchemaClass s1 , SchemaElement e1 where e1.SchemaClassGUID= s1.GUID and s1.Name =t.Name and e1.Name = 'Rating Plan ID') ) as "HUBID", 
null as "CatalogClassType", null as "DomainKey",concat(e.VersionNumber, '.' , e.IsSC, '.',e.Minor) as "versionNumber"
from Entity e, Template t
where  e.IsLive = 1 and e.TemplateID =t.TemplateID
and t.Name in ('Rating Energy Usage','Rating Demand','Rating Plan Property Set for Agreed Capacity','Rating Plan Property Set for Gas Usage','Rating Plan Property Set for Gas Usage Units','Rating Plan Property Set for Graduated Quantity','Rating Plan Property Set for Spot Price')
and e.Name in ('NESTOUCL1SEN0721FIX','NERTOUCL1SEN0721FIX','NESTOUSEN0721FIX','NESTOUCL2SEN0721FIX','NERTOUSEN0721FIX','NERTOUCL2SEN0721FIX')



-- empoty recal




 select (case when t.Name like 'Rating%' then 'RATING'
             when t.Name like 'Product%' then 'PRODUCT'
           
             end) as 'EntityType', e.Name as "EntityName",LOWER( e.GUID) as "GUID",'' as "HUBID", 
'' as "CatalogClassType", '' as "DomainKey",concat(e.VersionNumber, '.' ,e.Minor , '.',e.IsSC) as "versionNumber"
from Entity e, Template t
where  e.IsLive = 1 and e.TemplateID =t.TemplateID
and e.GUID in ('0015B74F-E695-11EA-95D9-05A887BA61B2')

--- genrate offer code reprot

select OfferSchema.Class_Type, i.InstanceClassGUID as "PublicID",e.Name,i.ElementValue as Offer_Id 
from InstanceElementValues i, SchemaElement s , (select OmniformName as "Class_Type", GUID as "Class_GUID"from SchemaClass where OmniformName in ( 'Offer_Commercial', 'Offer_Residential')) as OfferSchema, Entity e
where e.GUID=i.InstanceClassGUID and e.IsLive =1 and s.GUID=i.SchemaElementGUID and OfferSchema.Class_GUID = i.SchemaClassGUID and s.OmniformName = 'Offer_Code' 



--- generate Offer Market Status export

select 'Export' as 'Intent','1' as 'marketstatusid',CONCAT(s.OmniformName , '_Class') as 'Market_Status_Attribute_Class',i.ElementValue as 'Market_Status_Attribute_Guid' ,
Cast((select ElementValue from InstanceElementValues where SchemaElementGUID = '256854A2-BC94-452C-A41C-11C5869AE5F7' and InstanceClassGUID = i.ElementValue)as date) as 'Start_Date',
Cast((select ElementValue from InstanceElementValues where SchemaElementGUID = 'FF9B39F7-294C-48C0-93B4-23CBDBA4477D' and InstanceClassGUID = i.ElementValue)as date) as 'End_Date',
(select ElementValue from InstanceElementValues where SchemaElementGUID = 'C62958F5-36D9-421C-A552-5349C30EE2D1' and InstanceClassGUID = (select ElementValue from InstanceElementValues where SchemaElementGUID = '6DF30B29-B439-4682-A46E-0F5E27E60BDA' and InstanceClassGUID = i.ElementValue)) as 'Value',
(select ElementValue from InstanceElementValues where SchemaElementGUID = (select GUID from SchemaElement where OmniformName = 'Offer_Code' and SchemaClassGUID = OfferSchema.Class_GUID ) and InstanceClassGUID = i.InstanceClassGUID) as 'Offer_Id'
from InstanceElementValues i, SchemaElement s , (select OmniformName as 'Class_Type', GUID as 'Class_GUID'from SchemaClass where OmniformName in ( 'Offer_Commercial', 'Offer_Residential')) as OfferSchema, Entity e
where e.GUID=i.InstanceClassGUID and e.IsLive =1 and s.GUID=i.SchemaElementGUID and OfferSchema.Class_GUID = i.SchemaClassGUID  and s.OmniformName = 'In_Market_Status'


--- genrate SaleChannleExport

select 'Export' as'Intent','1' as 'Sales_Channel_To_Offer_Id',t.OmniformName as 'Sales_Channel_To_Offer_Attribute_Class', t.Sales_Channel_To_Offer_Attribute_Guid,t.Sales_Channel,REPLACE(tp.Name,' ','_') 'Class_Type', CONCAT(e.Name,'|', ISNULL((select Name from SchemaElement where GUID = t.Offer_Entity_Char),'Name') ) as 'Offer_Entity_Reference',
t.Partner_Portal_Sales_Partner, null as 'Do_Not_Sell',t.Start_Date,t.End_Date
from 
(select  s.OmniformName, i.ElementValue as 'Sales_Channel_To_Offer_Attribute_Guid'
, CAST((select ElementValue from InstanceElementValues where InstanceClassGUID = i.ElementValue and SchemaElementGUID = 'B518B3E3-E024-4D95-84E5-DA041DDBB885') as Date) as 'Start_Date'
, CAST((select ElementValue from InstanceElementValues where InstanceClassGUID = i.ElementValue and SchemaElementGUID = 'A7FE6C80-2D3C-4C24-ADA5-FBAC7754CB9B')as Date) as 'End_Date'
, SUBSTRING((select ElementValue from InstanceElementValues where InstanceClassGUID = i.ElementValue and SchemaElementGUID = '50B93194-5E67-4A09-91FB-04215D5CC556'),1,36) as 'Offer_Entity_GUID'
, SUBSTRING((select ElementValue from InstanceElementValues where InstanceClassGUID = i.ElementValue and SchemaElementGUID = '50B93194-5E67-4A09-91FB-04215D5CC556'),40,36) as 'Offer_Entity_Char'
, (select ElementValue from InstanceElementValues where InstanceClassGUID = i.ElementValue and SchemaElementGUID = '50B93194-5E67-4A09-91FB-04215D5CC556') as 'Offer_Entity_Ref_GUID'
, (select i2.ElementValue from InstanceElementValues i1, InstanceElementValues i2 where i2.InstanceClassGUID=i1.ElementValue and i1.InstanceClassGUID = i.ElementValue and i2.SchemaElementGUID = '0DEF7BBD-8471-4574-97BA-B4EE2CFB0091' and i1.SchemaElementGUID = '811324C3-BA31-468D-97FA-8DA7CFB3D21A') as 'Sales_Channel'
, (select i2.ElementValue from InstanceElementValues i1, InstanceElementValues i2 where i2.InstanceClassGUID=i1.ElementValue and i1.InstanceClassGUID = i.ElementValue and i2.SchemaElementGUID = 'C62958F5-36D9-421C-A552-5349C30EE2D1' and i1.SchemaElementGUID = '7870F042-1FCF-4834-969B-4E98E15FE7C9') as 'Partner_Portal_Sales_Partner'
from  InstanceElementValues i, SchemaElement s where s.GUID=i.SchemaElementGUID and  i.InstanceClassGUID = '04885bfa-e770-11ea-9644-fb287483b619' and s.OmniformName = 'Sales_Channel_to_Offer_Mapping' ) as t , Entity e, Template tp
where e.GUID = t.Offer_Entity_GUID and tp.TemplateID = e.TemplateID



---- bundle rate report

select t.Parent_Entity_Name,t.Parent_Entity_GUID,
(select e.Name from Entity e, Template t1 where  t1.TemplateID=e.TemplateID and t1.Name like '%Charge%' and e.GUID = RIGHT(t.Rate_Path,36)) as 'Charge_Entity_Name',RIGHT(t.Rate_Path,36) as 'Charge_Entity_GUID',
null as 'Rate_Class',
UPPER(CONCAT('{', LEFT(t.Rate_Path,36),'}{',SUBSTRING(t.Rate_Path,CHARINDEX(',',t.Rate_Path)+1,36),'}{',RIGHT(t.Rate_Path,36),'}')) as Rate_Path
, t.Rate_Row_GUID,Start_Date,t.End_Date,t.Activation_Stat_Date,t.Activation_End_Date,t.Rate_Period,t.Rate from (
select 
 e.Name as 'Parent_Entity_Name',e.guid as 'Parent_Entity_GUID',i2.ElementValue as 'Rate_Row_GUID',i2.SchemaClassGUID,
(select ElementValue from InstanceElementValues where InstanceClassGUID = i1.ElementValue and SchemaElementGUID = '2C36AAA2-F1EE-40C2-A530-A85EE70C3480')  as 'Rate_Path',
CAST ((select ElementValue from InstanceElementValues where InstanceClassGUID = i2.ElementValue and SchemaElementGUID = '114CBE72-B71D-4F9A-8222-06A00A96A842') as Date) as 'Activation_Stat_Date',
CAST ((select ElementValue from InstanceElementValues where InstanceClassGUID = i2.ElementValue and SchemaElementGUID = '224CBE72-B71D-4F9A-8222-06A00A96A842') as Date) as 'Activation_End_Date',
CAST ((select ElementValue from InstanceElementValues where InstanceClassGUID = i2.ElementValue and SchemaElementGUID = '524CBE72-B71D-4F9A-8222-06A00A96A842') as Date) as 'Start_Date',
CAST ((select ElementValue from InstanceElementValues where InstanceClassGUID = i2.ElementValue and SchemaElementGUID = 'F0443619-DD01-43A6-9FF1-32C3B71773F4') as Date) as 'End_Date',
(select ii2.ElementValue from InstanceElementValues ii1 ,InstanceElementValues ii2 where ii2.SchemaElementGUID = 'C62958F5-36D9-421C-A552-5349C30EE2D1' and ii2.InstanceClassGUID= ii1.ElementValue and ii1.InstanceClassGUID = i2.ElementValue and ii1.SchemaElementGUID = '9C09D8D1-9CDB-4E56-B513-8CB3320886CF')  as 'Rate_Period',
(select ElementValue from InstanceElementValues where InstanceClassGUID = i2.ElementValue and SchemaElementGUID = '79246545-DE3C-400E-88E1-A6BACA6268F2')  as 'Rate'
from InstanceElementValues i1 , InstanceElementValues i2,  Entity e
where i2.InstanceClassGUID=i1.ElementValue and i2.SchemaElementGUID = '576AEF89-1C17-49A8-87A7-CBF24DAD4DDB' and i1.InstanceClassGUID = e.GUID and i1.SchemaElementGUID = '9EFF8CC4-9318-463B-96DA-AEC0EDE039CA'
and e.TemplateID in( select TemplateID from Template where Name in ('Product Bundle Property Set')) and e.IsLive = 1 
) as t 


--- bundle reffranc report

select t.name as 'Class_Type',
 e.guid as 'PublicID',e.Name as 'Name'
from   Entity e,Template t where
e.TemplateID =t.TemplateID and t.Name in ('Product Bundle Property Set')  and e.IsLive = 1



-- bundle bas reffrace

select t.name as 'Class_Type',
 e.guid as 'PublicID',e.Name as 'Name'
from   Entity e,Template t where
e.TemplateID =t.TemplateID and t.Name in ('Product Base Property Set','Product Base Physical Property Set')  and e.IsLive = 1



-- sales channle report
select "Intent","Sales_Channel",count(*) from "SCCCCCC_OfferSalesChannel_SalesChannel_Not_Processed" where "Intent" = 'SALE_CHANNEL_NOT_FOUND' group by "Intent","Sales_Channel"
select "Intent","Partner_Portal_Sales_Partner",count(*) from "SCCCCCC_OfferSalesChannel_SalesChannel_Not_Processed" where "Intent" = 'PARENT_PORTAL_NOT_FOUND' group by "Intent","Partner_Portal_Sales_Partner"
select "Intent","Offer_Entity_Reference",count(*) from "SCCCCCC_OfferSalesChannel_SalesChannel_Not_Processed" where "Intent" = 'OFFER_NOT_FOUND' group by "Intent","Offer_Entity_Reference"


-- guir dquery

select STRING_AGG (distinct(E'\''||"3959RR_RatePlanRate_Rates"."Parent_Entity_Name" ||E'\''),',') from "3959RR_RatePlanRate_Rates"


-- data error bundle

select count(t.adgestmentGuid) ct,t.Bundle,t.Path from  (
select im.ElementValue as 'adgestmentGuid',im.InstanceClassGUID as 'Bundle',
(select i.ElementValue From InstanceElementValues i where i.InstanceClassGUID = im.ElementValue and i.SchemaElementGUID = '2C36AAA2-F1EE-40C2-A530-A85EE70C3480') as  'Path'
From InstanceElementValues im,entity e where e.GUID =im.InstanceClassGUID and im.SchemaElementGUID = '9EFF8CC4-9318-463B-96DA-AEC0EDE039CA' and e.IsLive = 1
 --and im.InstanceClassGUID = '4D883724-F112-11EB-BAE1-CD85BBFC7B36' 
 ) t 
 group by t.Bundle,t.Path
 order by ct desc
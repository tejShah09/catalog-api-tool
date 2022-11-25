
DROP VIEW  IF EXISTS CAPI_LOOKUP
GO
CREATE   VIEW CAPI_LOOKUP AS
select s1.OmniformName  as Base_Class, s.OmniformName as ClassType, e.InstanceClassGUID as Public_ID, 
e.ElementValue as Name  , e2.ElementValue as Description 
from InstanceElementValues e, InstanceElementValues e2, SchemaClass s,SchemaClass s1
where e.SchemaClassGUID = s.GUID and s.InheritedGUID = s1.guid  and s1.OmniformName = 'Lookup' 
and e2.InstanceClassGUID=e.InstanceClassGUID and 
e2.SchemaElementGUID= (select GUID from SchemaElement where SchemaClassGUID = s1.GUID and Name = 'Description' ) 
and e.SchemaElementGUID = (select GUID from SchemaElement where SchemaClassGUID = s1.GUID and Name = 'Name' ) 
GO

DROP VIEW  IF EXISTS CAPI_RatePlanDetail
GO
Create VIEW CAPI_RatePlanDetail as
select  st.SchemaClass as 'ClassType', 
e.Name as 'Name',
UPPER(e.GUID) as 'GUID' ,
e.BusinessID,
CAST(e.EffectiveStartDate as DATE) 'EffectiveStartDate' ,
CAST(e.EffectiveEndDate as DATE) 'EffectiveEndDate' ,
e.IsLive,
e.IsLatest,
UPPER(e.CurrentEntityGUID) as 'CurrentEntityGUID' ,
e.WorkflowStatusID,
e.LaunchableEntityTypeID,
concat(e.VersionNumber, '.' ,e.Minor , '.',e.IsSC) as "VersionNumber"
from  Entity e INNER JOIN
(select s.GUID as SchemaGuid,s.OmniformName as SchemaClass, t.TemplateID from SchemaClass s,Template t where t.TemplateGUID=s.GUID and  s.OmniformName in ('Rating_Energy_Usage','Rating_Plan_Property_Set_for_Gas_Usage_Units','Rating_Plan_Property_Set_for_Gas_Usage','Rating_Plan_Property_Set_for_Graduated_Quantity','Rating_Plan_Property_Set_for_Spot_Price','Rating_Demand','Rating_Plan_Property_Set_for_Agreed_Capacity')) as st
on st.TemplateID = e.TemplateID 
where  e.IsLive = 1 
GO

DROP VIEW  IF EXISTS CAPI_RatePlanDetail_AllStatus
GO
create VIEW CAPI_RatePlanDetail_AllStatus as
select  st.SchemaClass as 'ClassType', 
e.Name as 'Name',
UPPER(e.GUID) as 'GUID' ,
e.BusinessID,
CAST(e.EffectiveStartDate as DATE) 'EffectiveStartDate' ,
CAST(e.EffectiveEndDate as DATE) 'EffectiveEndDate' ,
e.IsLive,
e.IsLatest,
UPPER(e.CurrentEntityGUID) as 'CurrentEntityGUID' ,
(select WorkflowStatusCode from WorkflowStatus where WorkflowStatusID= e.WorkflowStatusID) as WorkflowStatusCode,
(select Ordinal from WorkflowStatus where WorkflowStatusID= e.WorkflowStatusID) WorkflowOrdinal,
e.LaunchableEntityTypeID,
concat(e.VersionNumber, '.' ,e.Minor , '.',e.IsSC) as "VersionNumber"
from  Entity e INNER JOIN
(select s.GUID as SchemaGuid,s.OmniformName as SchemaClass, t.TemplateID from SchemaClass s,Template t where t.TemplateGUID=s.GUID and  s.OmniformName in ('Rating_Energy_Usage','Rating_Plan_Property_Set_for_Gas_Usage_Units','Rating_Plan_Property_Set_for_Gas_Usage','Rating_Plan_Property_Set_for_Graduated_Quantity','Rating_Plan_Property_Set_for_Spot_Price','Rating_Demand','Rating_Plan_Property_Set_for_Agreed_Capacity')) as st
on st.TemplateID = e.TemplateID 
GO



DROP VIEW  IF EXISTS CAPI_RatePlanDetail_Charges
GO
create view CAPI_RatePlanDetail_Charges as 
select c.ClassType,c.Name, UPPER(c.GUID) as 'GUID',
CAST(c.EffectiveStartDate as date)EffectiveStartDate, 
(select count(i.ElementValue) from UV_InstanceElementValues i where c.GUID  = i.InstanceClassGUID and i.SchemaElement = 'Charge Relations') as "Charges"
from CAPI_RatePlanDetail c 
GO

DROP VIEW  IF EXISTS CAPI_RatePlanDetail_RatingPlanId
GO
create view CAPI_RatePlanDetail_RatingPlanId as 
select c.ClassType,c.Name, UPPER(c.GUID) as 'GUID', 
(select i.ElementValue from UV_InstanceElementValues i where c.GUID  = i.InstanceClassGUID and i.SchemaElement = 'Rating Plan Id') as "RatingPlanId"
from CAPI_RatePlanDetail c
GO



DROP VIEW  IF EXISTS CAPI_RatePlanDetail_TimeBand
GO
create  view CAPI_RatePlanDetail_TimeBand as
select  st.SchemaClass as 'ClassType', 
e.Name as 'Name',
 UPPER(e.GUID) as 'GUID' 
,tb.TimeBand as 'TimeBand'
from  Entity e INNER JOIN
(select s.GUID as SchemaGuid,s.OmniformName as SchemaClass, t.TemplateID from SchemaClass s,Template t where t.TemplateGUID=s.GUID and  s.OmniformName in ('Rating_Energy_Usage','Rating_Plan_Property_Set_for_Gas_Usage_Units','Rating_Plan_Property_Set_for_Gas_Usage','Rating_Plan_Property_Set_for_Graduated_Quantity','Rating_Plan_Property_Set_for_Spot_Price','Rating_Demand','Rating_Plan_Property_Set_for_Agreed_Capacity')) as st
on st.TemplateID = e.TemplateID 
LEFT JOIN 
(select u1.InstanceClassGUID, ( select e1.ElementValue from UV_InstanceElementValues e1,UV_InstanceElementValues e2 where e1.InstanceClassGUID=e2.ElementValue and e1.SchemaElement='Name' and e2.InstanceClassGUID = u1.ElementValue and e2.SchemaElement = 'Rating Plan Time Band') as TimeBand from UV_InstanceElementValues u1 where   u1.SchemaElement = 'Time Band Mapping') as tb
on  e.GUID = tb.InstanceClassGUID 
where  e.IsLive = 1 
GO

DROP VIEW  IF EXISTS CAPI_RatePlanDetail_Rates
GO
create  view CAPI_RatePlanDetail_Rates as
select REPLACE(t.Name,' ','_') as 'Parent_Entity_ClassType', 
e.Name as 'Parent_Entity_Name',
e.GUID as 'Parent_Entity_GUID',
(select ElementValue from InstanceElementValues where SchemaElementGUID = '19AEC7BC-B015-47CA-9EA8-F28DB65D50E7' and InstanceClassGUID=( select ElementValue from InstanceElementValues where InstanceClassGUID=re.InstanceClassGUID and SchemaElementGUID = 'F7D4BE48-8A80-4C59-8F39-E5C662763956' )) as 'Charge_Entity_GUID',
(select REPLACE(Name,' ','_') from Entity where GUID =  (select ElementValue from InstanceElementValues where SchemaElementGUID = '19AEC7BC-B015-47CA-9EA8-F28DB65D50E7' and InstanceClassGUID=( select ElementValue from InstanceElementValues where InstanceClassGUID=re.InstanceClassGUID and SchemaElementGUID = 'F7D4BE48-8A80-4C59-8F39-E5C662763956' ))) as 'Rate_Class',
ri.InstanceClassGUID as 'Rate_Row_GUID',
Cast((select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '524CBE72-B71D-4F9A-8222-06A00A96A842' )as date) as 'Start_Date',
Cast((select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = 'F0443619-DD01-43A6-9FF1-32C3B71773F4')as date) as 'End_Date',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = 'C65D7A60-AB4A-4A3F-B06E-42C2E226373D')  as 'PerUnit',
(select ElementValue from InstanceElementValues where SchemaElementGUID = 'C62958F5-36D9-421C-A552-5349C30EE2D1' and InstanceClassGUID = (select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '363C2ACA-F7C6-4D97-AD75-6BF18AADD2DE') ) as 'Time_Band',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '2F029B20-8A8E-47D7-B58B-CB76F42D9527')  as 'Change_ID',
Cast((select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '6999C3DB-F1E1-4FC4-81EA-B6EA0802DDED')as date)  as 'Submit_Date',
(select ElementValue from InstanceElementValues where SchemaElementGUID = 'C62958F5-36D9-421C-A552-5349C30EE2D1' and InstanceClassGUID = (select ElementValue from InstanceElementValues where InstanceClassGUID=ri.InstanceClassGUID and SchemaElementGUID = '12552B7C-2BE8-4F51-B907-F40F9FB7417F') ) as 'Rating_Period',
e.Name 'Rating_Plan_ID',
ri.ElementValue as 'Rate_Attribute_Guid',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = '7DC046DB-87BF-4D19-A779-DD575C51F7C5' ) as 'Step',
ISNULL((select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = '7C23F6A9-3A1D-4C85-A444-1D7BDA94A178' ),'') as 'UnitsLessOrEqualTo',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = 'D738BE96-E773-4F42-BD6B-EB94D1A22587' ) as 'UnitRate',
Cast((select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = 'FE25B002-5A3F-4BE1-82BE-2A20870FC01C' )as date) as 'Rate_Submit_Date',
(select ElementValue from InstanceElementValues where InstanceClassGUID=ri.ElementValue and SchemaElementGUID = 'EF7160DA-6CBC-437E-9EB2-76B885138653' ) as 'Rate_Change_ID'
from InstanceElementValues ri,InstanceElementValues rr, InstanceElementValues re ,Entity e, Template t
where e.IsLive = 1 and e.TemplateID =t.TemplateID and  rr.InstanceClassGUID = re.ElementValue and ri.InstanceClassGUID = rr.ElementValue and re.InstanceClassGUID = e.GUID
and t.Name in ('Rating Energy Usage','Rating Demand','Rating Plan Property Set for Agreed Capacity','Rating Plan Property Set for Gas Usage','Rating Plan Property Set for Gas Usage Units','Rating Plan Property Set for Graduated Quantity','Rating Plan Property Set for Spot Price')
and ri.SchemaElementGUID = '335C7019-17D4-4B1F-8888-5A8326AEF092'
and rr.SchemaElementGUID = '576AEF89-1C17-49A8-87A7-CBF24DAD4DDB'
and re.SchemaElementGUID = '9EFF8CC4-9318-463B-96DA-AEC0EDE039CA'
GO


DROP VIEW  IF EXISTS CAPI_Bundle
GO
Create VIEW CAPI_Bundle as
select  st.SchemaClass as 'ClassType', 
e.Name as 'Name',
 UPPER(e.GUID) as 'GUID',
e.BusinessID,
CAST(e.EffectiveStartDate as DATE) 'EffectiveStartDate' ,
CAST(e.EffectiveEndDate as DATE) 'EffectiveEndDate' ,
e.IsLive,
e.IsLatest,
UPPER(e.CurrentEntityGUID) as 'CurrentEntityGUID' ,
e.WorkflowStatusID,
e.LaunchableEntityTypeID,
concat(e.VersionNumber, '.' ,e.Minor , '.',e.IsSC) as "VersionNumber"
from  Entity e INNER JOIN
(select s.GUID as SchemaGuid,s.OmniformName as SchemaClass, t.TemplateID from SchemaClass s,Template t where t.TemplateGUID=s.GUID and  s.OmniformName in ('Product_Bundle_Property_Set','Product_Base_Property_Set')) as st
on st.TemplateID = e.TemplateID 
where  e.IsLive = 1 
GO




DROP VIEW  IF EXISTS CAPI_Bundle_AllStatus
GO
create VIEW CAPI_Bundle_AllStatus as
select  st.SchemaClass as 'ClassType', 
e.Name as 'Name',
UPPER(e.GUID) as 'GUID',
e.BusinessID,
CAST(e.EffectiveStartDate as DATE) 'EffectiveStartDate' ,
CAST(e.EffectiveEndDate as DATE) 'EffectiveEndDate' ,
e.IsLive,
e.IsLatest,
UPPER(e.CurrentEntityGUID) as 'CurrentEntityGUID' ,
(select WorkflowStatusCode from WorkflowStatus where WorkflowStatusID= e.WorkflowStatusID) as WorkflowStatusCode,
(select Ordinal from WorkflowStatus where WorkflowStatusID= e.WorkflowStatusID) WorkflowOrdinal,
e.LaunchableEntityTypeID,
concat(e.VersionNumber, '.' ,e.Minor , '.',e.IsSC) as "VersionNumber"
from  Entity e INNER JOIN
(select s.GUID as SchemaGuid,s.OmniformName as SchemaClass, t.TemplateID from SchemaClass s,Template t where t.TemplateGUID=s.GUID and  s.OmniformName in ('Product_Bundle_Property_Set','Product_Base_Property_Set')) as st
on st.TemplateID = e.TemplateID 
GO



DROP VIEW  IF EXISTS CAPI_Bundle_Association
GO
create VIEW CAPI_Bundle_Association as
select asoc.SchemaElement  ,  asoc.ElementValue as 'AssocationGUID', cb.ClassType 'ParentEntityClass' , cb.GUID as 'ParentEntityGuid',cb.Name as 'ParentEntityName',
(select REPLACE(t.Name,' ','_') from InstanceElementValues inner join Entity e on ElementValue=e.guid  inner join Template t on e.TemplateID= t.TemplateID  where InstanceClassGUID= asoc.ElementValue and SchemaElementGUID = '5FB99F6C-7036-40DF-BF67-3666336FCF30') as 'ChildEntityClass',
(select ElementValue from InstanceElementValues where InstanceClassGUID= asoc.ElementValue and SchemaElementGUID = '5FB99F6C-7036-40DF-BF67-3666336FCF30') as 'ChildEntityGUID',
(select e.Name from InstanceElementValues inner join Entity e on ElementValue=e.guid where InstanceClassGUID= asoc.ElementValue and SchemaElementGUID = '5FB99F6C-7036-40DF-BF67-3666336FCF30') as 'ChildEntityName',
Cast((select ElementValue from InstanceElementValues where InstanceClassGUID= asoc.ElementValue and SchemaElementGUID = 'A033E802-188F-4740-96B8-4536EFD93F46')as date) as 'AssociationStartDate',
Cast((select ElementValue from InstanceElementValues where InstanceClassGUID= asoc.ElementValue and SchemaElementGUID = '5D7A88E8-A1D9-471D-884F-D6B9976C2315')as date) as 'AssociationEndDate'
 from UV_InstanceElementValues asoc inner join CAPI_Bundle cb on asoc.InstanceClassGUID=cb.GUID where asoc.SchemaElement = 'Product Associations'
 GO

DROP VIEW  IF EXISTS CAPI_Bundle_Reference
GO
create VIEW CAPI_Bundle_Reference as
select UPPER(i.InstanceClassGUID) as BUNDLE_GUID,e.Name as BUNDLE_NAME ,UPPER(i.ElementValue) as REF_GUID , 
(select ElementValue from UV_InstanceElementValues where InstanceClassGUID =i.ElementValue and SchemaElement='Rating Plan Reference' ) as Rating_Plan_Reference,
(select e.Name from UV_InstanceElementValues u inner join Entity e on e.BusinessID = u.ElementValue  where e.IsLive = 1 and u.InstanceClassGUID =i.ElementValue and u.SchemaElement='Rating Plan Reference' ) as Rating_Plan_Name,
(select ElementValue from UV_InstanceElementValues where InstanceClassGUID =i.ElementValue and SchemaElement='Base Product Reference' ) as Base_Product_Reference,
(select e.Name from UV_InstanceElementValues u inner join Entity e on e.BusinessID = u.ElementValue  where e.IsLive = 1 and u.InstanceClassGUID =i.ElementValue and u.SchemaElement='Base Product Reference' ) as Base_Product_Name,
cast((select ElementValue from UV_InstanceElementValues where InstanceClassGUID =i.ElementValue and SchemaElement='Start Date' ) as DATE) as Start_Date,
cast((select ElementValue from UV_InstanceElementValues where InstanceClassGUID =i.ElementValue and SchemaElement='End Date' ) as DATE) as End_Date
from UV_InstanceElementValues i,Entity e
where e.isLive = 1 and e.GUID = i.InstanceClassGUID and SchemaClass in ( 'Product Bundle Property Set') and i.SchemaElement = 'Reference' 
 GO


DROP VIEW  IF EXISTS CAPI_Bundle_Rate
GO
CREATE   VIEW CAPI_Bundle_Rate AS
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
GO

DROP VIEW  IF EXISTS CAPI_Entity_AllStatus
GO
create VIEW CAPI_Entity_AllStatus as
select  st.SchemaClass as 'ClassType', 
e.Name as 'Name',
UPPER(e.GUID) as 'GUID',
e.BusinessID,
CAST(e.EffectiveStartDate as DATE) 'EffectiveStartDate' ,
CAST(e.EffectiveEndDate as DATE) 'EffectiveEndDate' ,
e.IsLive,
e.IsLatest,
UPPER(e.CurrentEntityGUID) as 'CurrentEntityGUID' ,
(select WorkflowStatusCode from WorkflowStatus where WorkflowStatusID= e.WorkflowStatusID) as WorkflowStatusCode,
(select Ordinal from WorkflowStatus where WorkflowStatusID= e.WorkflowStatusID) WorkflowOrdinal,
e.LaunchableEntityTypeID,
concat(e.VersionNumber, '.' ,e.Minor , '.',e.IsSC) as "VersionNumber"
from  Entity e INNER JOIN
(select s.GUID as SchemaGuid,s.OmniformName as SchemaClass, t.TemplateID from SchemaClass s,Template t where t.TemplateGUID=s.GUID  ) as st
on st.TemplateID = e.TemplateID 
GO





DROP VIEW  IF EXISTS CAPI_TTREE
GO
CREATE   VIEW CAPI_TTREE AS
select s1.OmniformName as Characteristic_Class_Name , e1.InstanceClassGUID as Characteristic_Value_ID,e2.ElementValue as Name ,
e1.ElementValue as Description,s2.InheritedGUID, 
(select ElementValue from InstanceElementValues e where e.InstanceClassGUID = e1.InstanceClassGUID and e.SchemaElementGUID =(select GUID from SchemaElement where Name ='Parent' and SchemaClassGUID = e1.SchemaClassGUID ) ) as Parent_GUID, 
(select ElementValue from InstanceElementValues where SchemaElementGUID =(select GUID from SchemaElement where Name ='Name' and SchemaClassGUID = s2.InheritedGUID ) and InstanceClassGUID = (select ElementValue from InstanceElementValues e where e.InstanceClassGUID = e1.InstanceClassGUID and e.SchemaElementGUID =(select GUID from SchemaElement where Name ='Parent' and SchemaClassGUID = e1.SchemaClassGUID ) )) as ParentName,
(select ElementValue from InstanceElementValues where SchemaElementGUID =(select GUID from SchemaElement where Name ='Description' and SchemaClassGUID = s2.InheritedGUID ) and InstanceClassGUID = (select ElementValue from InstanceElementValues e where e.InstanceClassGUID = e1.InstanceClassGUID and e.SchemaElementGUID =(select GUID from SchemaElement where Name ='Parent' and SchemaClassGUID = e1.SchemaClassGUID ) )) as ParentDescription
from InstanceElementValues   e1 INNER JOIN InstanceElementValues   e2 on  e1.InstanceClassGUID = e2.InstanceClassGUID ,SchemaClass s1,SchemaClass s2 
 where s2.OmniformName in ('TTree_Branch','TTree_Leaf')  and e1.SchemaClassGUID = s1.GUID and s1.InheritedGUID = s2.GUID and e2.SchemaElementGUID= (select GUID from SchemaElement where SchemaClassGUID = s2.InheritedGUID and Name = 'Name' )  
 and e1.SchemaElementGUID = (select GUID from SchemaElement where SchemaClassGUID = s2.InheritedGUID and Name = 'Description' )
 --and e1.InstanceClassGUID in ( '46605c47-10a5-4fda-bd20-6da6dfed5926','eff62c93-9533-11ea-aa3a-3f31cdbdd854','cdb456d8-5b2a-99fe-18a2-db940b16493c','33fcd8fb-08dc-6455-d1a5-e67f1776313c','7a2dec4e-8cc3-5a20-8553-b44e8b3341b7')
--and s1.OmniformName  in ('TProduct_Subclass')
GO


DROP VIEW  IF EXISTS CAPI_Discount
GO
CREATE   VIEW CAPI_Discount AS
select e.Name, e.GUID as publicid,REPLACE( t.Name, ' ', '_' ) as Class_Type from Entity e inner join Template t on t.TemplateID = e.TemplateID
where  e.isLive = 1 and  t.Name in ('Discount Plan Fixed','Discount Plan Pending Prompt Payment') 
GO


DROP VIEW  IF EXISTS CAPI_Offer_Code
GO
CREATE   VIEW CAPI_Offer_Code AS
select OfferSchema.Class_Type, i.InstanceClassGUID as PublicID,e.Name,i.ElementValue as Offer_Id 
from InstanceElementValues i, SchemaElement s , (select OmniformName as Class_Type, GUID as Class_GUID from SchemaClass where OmniformName in ( 'Offer_Commercial', 'Offer_Residential')) as OfferSchema, Entity e
where e.GUID=i.InstanceClassGUID and e.IsLive =1 and s.GUID=i.SchemaElementGUID and OfferSchema.Class_GUID = i.SchemaClassGUID and s.OmniformName = 'Offer_Code' 
GO


DROP VIEW  IF EXISTS CAPI_Offer_SalesChannel
GO
CREATE VIEW CAPI_Offer_SalesChannel As
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
GO



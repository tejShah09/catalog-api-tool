
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
e.GUID as 'GUID' ,
e.BusinessID,
e.EffectiveStartDate,
e.EffectiveEndDate,
e.IsLive,
e.IsLatest,
e.CurrentEntityGUID,
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
e.GUID as 'GUID' ,
e.BusinessID,
e.EffectiveStartDate,
e.EffectiveEndDate,
e.IsLive,
e.IsLatest,
e.CurrentEntityGUID,
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
select c.ClassType,c.Name,c.GUID,
CAST(c.EffectiveStartDate as date)EffectiveStartDate, 
(select count(i.ElementValue) from UV_InstanceElementValues i where c.GUID  = i.InstanceClassGUID and i.SchemaElement = 'Charge Relations') as "Charges"
from CAPI_RatePlanDetail c 
GO

DROP VIEW  IF EXISTS CAPI_RatePlanDetail_RatingPlanId
GO
create view CAPI_RatePlanDetail_RatingPlanId as 
select c.ClassType,c.Name,c.GUID, 
(select i.ElementValue from UV_InstanceElementValues i where c.GUID  = i.InstanceClassGUID and i.SchemaElement = 'Rating Plan Id') as "RatingPlanId"
from CAPI_RatePlanDetail c
GO


DROP VIEW  IF EXISTS CAPI_RatePlanDetail_TimeBand
GO
create  view CAPI_RatePlanDetail_TimeBand as
select  st.SchemaClass as 'ClassType', 
e.Name as 'Name',
e.GUID as 'GUID' 
,tb.InstanceClassGUID 'TimeBand'
from  Entity e INNER JOIN
(select s.GUID as SchemaGuid,s.OmniformName as SchemaClass, t.TemplateID from SchemaClass s,Template t where t.TemplateGUID=s.GUID and  s.OmniformName in ('Rating_Energy_Usage','Rating_Plan_Property_Set_for_Gas_Usage_Units','Rating_Plan_Property_Set_for_Gas_Usage','Rating_Plan_Property_Set_for_Graduated_Quantity','Rating_Plan_Property_Set_for_Spot_Price','Rating_Demand','Rating_Plan_Property_Set_for_Agreed_Capacity')) as st
on st.TemplateID = e.TemplateID 
LEFT JOIN 
(select u1.InstanceClassGUID, ( select e1.ElementValue from UV_InstanceElementValues e1,UV_InstanceElementValues e2 where e1.InstanceClassGUID=e2.ElementValue and e1.SchemaElement='Name' and e2.InstanceClassGUID = u1.ElementValue and e2.SchemaElement = 'Rating Plan Time Band') as TimeBand from UV_InstanceElementValues u1 where   u1.SchemaElement = 'Time Band Mapping') as tb
on  e.GUID = tb.InstanceClassGUID 
where  e.IsLive = 1 
GO



DROP VIEW  IF EXISTS CAPI_Bundle
GO
Create VIEW CAPI_Bundle as
select  st.SchemaClass as 'ClassType', 
e.Name as 'Name',
e.GUID as 'GUID' ,
e.BusinessID,
e.EffectiveStartDate,
e.EffectiveEndDate,
e.IsLive,
e.IsLatest,
e.CurrentEntityGUID,
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
e.GUID as 'GUID' ,
e.BusinessID,
e.EffectiveStartDate,
e.EffectiveEndDate,
e.IsLive,
e.IsLatest,
e.CurrentEntityGUID,
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

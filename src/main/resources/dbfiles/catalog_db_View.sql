CREATE   VIEW  AS
select s1.OmniformName  as Base_Class, s.OmniformName as ClassType, e.InstanceClassGUID as Public_ID, 
e.ElementValue as Name  , e2.ElementValue as Description 
from InstanceElementValues e, InstanceElementValues e2, SchemaClass s,SchemaClass s1
where e.SchemaClassGUID = s.GUID and s.InheritedGUID = s1.guid  and s1.OmniformName = 'Lookup' 
and e2.InstanceClassGUID=e.InstanceClassGUID and 
e2.SchemaElementGUID= (select GUID from SchemaElement where SchemaClassGUID = s1.GUID and Name = 'Description' ) 
and e.SchemaElementGUID = (select GUID from SchemaElement where SchemaClassGUID = s1.GUID and Name = 'Name' ) 
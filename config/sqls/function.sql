CREATE OR REPLACE FUNCTION public.geterrors(
	tablename character varying)
    RETURNS TABLE(err text) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE 
   errorjson json;
   allerrors json [];
   inputs VARCHAR [];
   code VARCHAR;
   finalQuery VARCHAR:='select CAST(json_strip_nulls(to_json(t)) as TEXT) from (select';
BEGIN
SELECT ARRAY_AGG(column_name) into inputs FROM information_schema.columns where table_name like tablename;

IF inputs is NOT NULL THEN
  FOREACH code  IN ARRAY inputs
   LOOP
   IF code <> 'ROW' THEN
   finalQuery = finalQuery || ' array_agg (distinct "'|| code||'") filter (where "'|| code||'" is not null)  as "'|| code ||'" ,';
   END IF;
   END LOOP;

finalQuery = substring(finalQuery,1,length(finalQuery)-1);
finalQuery = finalQuery || 'from "'|| tablename||'") as t';
RETURN QUERY EXECUTE finalQuery;
--  Exception when others then
--  RETURN QUERY EXECUTE ''
--RETURN finalQuery;
ELSE
RETURN QUERY EXECUTE 'select null';
END IF;
END
$BODY$;



CREATE OR REPLACE FUNCTION public.getstatuscount(
	tablename character varying,
	keyname character varying)
    RETURNS TABLE(err text) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE 
 
   finalQuery VARCHAR:='';
   countQuery VARCHAR:='';
   messagesize Integer;
BEGIN

finalQuery = 'select CAST(json_agg(to_json(t)) as TEXT) as ct from (SELECT  "status",count(*),status_reason as response,ARRAY_AGG("'||keyname||'") as entites FROM "'||tablename||'" group by status,status_reason) as t';
countQuery = 'select  length(ct)  from ( '||finalQuery||' ) as t';
EXECUTE countQuery into messagesize;
IF messagesize < 2048 THEN
RETURN QUERY EXECUTE finalQuery;
ELSE
RETURN QUERY EXECUTE 'select CAST(json_agg(to_json(t)) as TEXT) as ct from (SELECT  "status",count(*),SUBSTRING(split_part(status_reason,'':'',1),1,15) as response FROM "'||tablename||'" group by status,SUBSTRING(split_part(status_reason,'':'',1),1,15)) as t';
END IF;

 Exception when others then
  RETURN QUERY EXECUTE 'select null'
  END;
END
$BODY$;





CREATE OR REPLACE FUNCTION public.getRowCount(
	tablename character varying)
    RETURNS INTEGER 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE

AS $BODY$
DECLARE 
  
   inputs VARCHAR [];
   code VARCHAR;
   rowcount INTEGER;
   
BEGIN
SELECT ARRAY_AGG(column_name) into inputs FROM information_schema.columns where table_name like tablename;

IF inputs is  NULL THEN
rowcount := 0;
ELSE
 EXECUTE  'select count(*)  from "'||tablename || '"' INTO  rowcount;
END IF;

return rowcount;
END
$BODY$;


CREATE OR REPLACE FUNCTION public.getReconJSON(
	tablename character varying)
    RETURNS TABLE(err text) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE 
 
   finalQuery VARCHAR:='';
   countQuery VARCHAR:='';
   messagesize Integer;
BEGIN

finalQuery = 'select CAST(json_build_object(''catalogReconciliations'',json_strip_nulls(json_agg(row_to_json(t)))) as TEXT)from (select "entityType","entityName",lower("catalogGuid") as "catalogGuid","guidVersion","hubid" as "hubId" From "'||tablename||'") as t';
RETURN QUERY EXECUTE finalQuery;

END
$BODY$;


CREATE OR REPLACE FUNCTION public.getAllJobs()
    RETURNS TABLE(err text) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE 
 
   finalQuery VARCHAR:='';
   countQuery VARCHAR:='';
   messagesize Integer;
BEGIN

finalQuery = 'select CAST(json_build_object(''response'',json_strip_nulls(json_agg(row_to_json(t)))) as TEXT)from (select  job_id,job_category as job_type,(select json_strip_nulls(json_agg(message)) from jobs where job_id = j.job_id and job_type = ''SAVED_FILE_NAME'' ) as "input",(select CASE WHEN status  like ''JOB_%'' THEN status ELSE ''JOB_IN_PROGRESS'' END from jobs where job_id = j.job_id order by done_at desc limit 1) as "status" from jobs j where job_type=''START'' order by id desc limit 100) as t';
RETURN QUERY EXECUTE finalQuery;

END
$BODY$;


CREATE OR REPLACE FUNCTION public.executeQuery(queryText character varying)
    RETURNS TABLE(err text) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
DECLARE 
 
   finalQuery VARCHAR:='';
   countQuery VARCHAR:='';
   messagesize Integer;
BEGIN

finalQuery = 'select CAST(json_strip_nulls(json_agg(row_to_json(t))) as TEXT)from ('||queryText ||') as t';
RETURN QUERY EXECUTE finalQuery;

END
$BODY$;
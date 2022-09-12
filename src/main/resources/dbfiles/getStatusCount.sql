-- FUNCTION: public.getstatuscount(character varying, character varying)

-- DROP FUNCTION IF EXISTS public.getstatuscount(character varying, character varying);

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

finalQuery = 'select CAST(json_agg(to_json(t)) as TEXT) as ct from (SELECT  "status",count(*),response,ARRAY_AGG("'||keyname||'") as entites FROM "'||tablename||'" group by status,response) as t';
countQuery = 'select  length(ct)  from ( '||finalQuery||' ) as t';
EXECUTE countQuery into messagesize;
IF messagesize < 2048 THEN
RETURN QUERY EXECUTE finalQuery;
ELSE
RETURN QUERY EXECUTE 'select CAST(json_agg(to_json(t)) as TEXT) as ct from (SELECT  "status",count(*),SUBSTRING(split_part(response,'':'',1),1,15) as response FROM "'||tablename||'" group by status,SUBSTRING(split_part(response,'':'',1),1,15)) as t';
END IF;

 Exception when others then
  RETURN QUERY EXECUTE 'select null'
  END;
END
$BODY$;

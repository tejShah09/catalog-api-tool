import pandas as pd
from sqlalchemy import create_engine, types
import sqlalchemy as sqAl
import os
import json
import argparse
import configparser
import warnings
from jproperties import Properties
from openpyxl.workbook import Workbook

warnings.simplefilter('ignore')
dirname = os.path.dirname(__file__)


def initConfig():
    configParser = configparser.RawConfigParser()
    configFilePath = os.path.join(dirname, 'config.txt')
    configParser.read(configFilePath)
    return configParser

def readDBConfig(configParser):
    db_config_path = configParser.get('DATABASE_CONFIG',
            'db_config_path')
    db_config = Properties()
    with open(db_config_path, 'rb') as read_prop:
        db_config.load(read_prop)
    username = db_config.get('username').data
    password = db_config.get('password').data
    dbname = db_config.get('database').data
    host = db_config.get('host').data
    port = db_config.get('port').data
    database = db_config.get('database').data
    db_url = 'postgresql://' + username + ':' + password + '@' + host \
        + ':' + port + '/' + database
    return db_url

def readExecArgs():
    parser = argparse.ArgumentParser()
    parser.add_argument("jobId", help="db table name")
    args = parser.parse_args()
    return args

def getSheetOrder(configParser, excelFilename):
    excel_to_sql = configParser.get('EXCEL_TO_SQL', 'excel_to_sql')
    excel_to_sql_config = None
    with open(excel_to_sql) as f:
        excel_to_sql_config = json.load(f)
    sheet_order = None
    if excel_to_sql_config != None:
        for config in excel_to_sql_config['Sheet_Order']:
            if excelFilename.find(config['Workbook']) != -1:
                sheet_order = config['Sheets']  
    return sheet_order
   
def convertSqltoExcel(configParser):
    args = readExecArgs()
    jobId = args.jobId

    db_url = readDBConfig(configParser)
    db_engine = create_engine(db_url)
    workbook_dict = {}
    for tablename in db_engine.table_names():
        jobPrefix = tablename[0:8]
        if jobId ==jobPrefix:
            workbook_dict[jobPrefix] = []

    for tablename in db_engine.table_names():
        jobPrefix = tablename[0:8]
        if jobId == jobPrefix:
            tables = workbook_dict[jobPrefix]
            tables.append(tablename)
            workbook_dict[jobPrefix] = tables

    if len(workbook_dict.keys()) == 0:
        raise Exception("No Database table found for the given jobId : "+ jobId)    

    import_directory = configParser.get('EXCEL_DIR', 'import_directory')
    sheet_order = getSheetOrder(configParser, 'Offer_Entity')
    
    for workbookNm in workbook_dict:
        filepath = os.path.join(import_directory, workbookNm + '.xlsx')
        writer = pd.ExcelWriter(filepath)
        sheets = workbook_dict[workbookNm]
        for sheetNm in sheet_order:
            db_table = jobId+"_"+sheetNm
            if sqAl.inspect(db_engine).has_table(db_table):
                metadata = sqAl.MetaData()
                metadata.bind = db_engine
                mytable = sqAl.Table(db_table, metadata, autoload=True)
                db_connection = db_engine.connect()
                select = sqAl.sql.select([mytable])
                df = pd.read_sql_query(select, db_engine)
                df.drop(['id'], axis = 1, inplace = True)
                df.to_excel(writer, sheet_name=sheetNm, index=False)
        writer.save()


cfgParser = initConfig()
convertSqltoExcel(cfgParser)

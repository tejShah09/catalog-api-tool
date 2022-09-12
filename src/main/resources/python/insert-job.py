import pandas as pd
from sqlalchemy import create_engine, types
import os
import json
import argparse
import configparser
import warnings
from jproperties import Properties
  
warnings.simplefilter("ignore")
dirname = os.path.dirname(__file__)

def initConfig():
    configParser = configparser.RawConfigParser()   
    configFilePath = os.path.join(dirname, 'config.txt')
    configParser.read(configFilePath)
    return configParser

#Read Database
def readDBConfig(configParser):
    db_config_path = configParser.get('DATABASE_CONFIG', 'db_config_path')
    db_config = Properties()
    with open(db_config_path, 'rb') as read_prop:
        db_config.load(read_prop)
    username = db_config.get("username").data
    password = db_config.get("password").data
    dbname = db_config.get("database").data
    host = db_config.get("host").data
    port = db_config.get("port").data
    database = db_config.get("database").data
    db_url = 'postgresql://'+username+':'+password+'@'+host+':'+port+'/'+database
    return db_url

def readExecArgs():
    parser = argparse.ArgumentParser()
    parser.add_argument("excelFile", help="excel file name")
    parser.add_argument("jobId", help="db table name")
    parser.add_argument("role", help="export or import role")
    args = parser.parse_args()
    return args

def getSheetSelection(configParser, excelFilename):
    excel_to_sql = configParser.get('EXCEL_TO_SQL', 'excel_to_sql')
    excel_to_sql_config = None
    with open(excel_to_sql) as f:
        excel_to_sql_config = json.load(f)

    sheet_selection = None
    if excel_to_sql_config != None:
        for config in excel_to_sql_config['Sheet_Selection']:
            if excelFilename.find(config['Workbook']) != -1:
                sheet_selection = config['Sheets']  
    return sheet_selection              

def getExcelInputFileDirectory(configParser, role):
    #Read Excel Folder
    export_directory = configParser.get('EXCEL_DIR', 'export_directory')
    import_directory = configParser.get('EXCEL_DIR', 'import_directory')

    if role == "export": 
        excel_directory = export_directory 

    elif role == "import": 
        excel_directory = import_directory 

    elif role == "external": 
        excel_directory = os.path.join(dirname,'.\\temp-excel\\') 
    else: 
        print("wrong role name") 
    return excel_directory    

def convertExcelToSql(configParser):
    args = readExecArgs()

    filename1 = args.excelFile 
    jobId = args.jobId
    role = args.role

    excel_directory = getExcelInputFileDirectory(configParser, role)
    db_url = readDBConfig(configParser)

    filepath = os.path.join(excel_directory, filename1)
    db_engine = create_engine(db_url) 
    
    sheet_selection = getSheetSelection(configParser, filename1)

    df_all = pd.read_excel(filepath, engine="openpyxl", sheet_name = sheet_selection) 

    for sheetNm, df in df_all.items():
        df['id'] = range(1, len(df) + 1)
        df.set_index('id')
        #set id as first column
        first_column = df.pop('id')
        df.insert(0, 'id', first_column)
        df = df.assign(status='', status_reason='', payloads='')
        df.columns = [c.replace(' ', '_') for c in df.columns]
        sheet_name = jobId+'_'+sheetNm
        df.to_sql(name=sheet_name[:63], con=db_engine, index=False, if_exists='replace') 
    print(jobId)     

cfgParser = initConfig()
convertExcelToSql(cfgParser)

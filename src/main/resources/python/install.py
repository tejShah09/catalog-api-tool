import subprocess
import sys

def install(package):
    subprocess.check_call([sys.executable, "-m", "pip", "install", package])

install('pandas')
install('sqlalchemy')
install('argparse')
install('configparser')
install('psycopg2')
install('openpyxl')
install('jproperties')
import os
import re

os.chdir(r'c:\Users\Acer\OneDrive\Documents\NetBeansProjects\Caguioa Bank\src\caguioa\bank')

files_to_fix = [
    'AdminLoanHelper.java',
    'LoanApplicationHelper.java',
    'LoanPaymentHelper.java',
    'DBInit.java',
    'LoanApplicationDialog.java',
    'LoanPaymentDialog.java',
    'UserDashboard.java'
]

for file in files_to_fix:
    if os.path.exists(file):
        with open(file, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        # Replace DB.getConnection() with DB.connect()
        content = content.replace('DB.getConnection()', 'DB.connect()')
        
        # Remove LoanManager.ensureLoanTableAndSchema() calls
        content = re.sub(r'\s*LoanManager\.ensureLoanTableAndSchema\(\);\n', '\n', content)
        
        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'Fixed: {file}')
    else:
        print(f'Not found: {file}')

print('All files fixed!')

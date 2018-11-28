import subprocess
subprocess.call(['sed','-i','/.*a3*/d','test.txt'])

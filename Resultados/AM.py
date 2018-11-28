from pylab import *
import numpy
import matplotlib.pyplot as plt

grupos = 7
Precision = (93.3, 84.8, 95.6, 82.3, 85.6 , 68.8, 82.0)
Recall = (91.8, 90.1, 83.0, 82.9, 93.1, 46.4, 94.3)



fig, ax = plt.subplots()
indice = np.arange(grupos)
bar_larg = 0.4
transp = 0.7
plt.bar(indice, Precision, bar_larg, alpha=transp, color="#f39700", label='Precisão', hatch="*", edgecolor='black')

plt.bar(indice + bar_larg, Recall, bar_larg, alpha=transp, color="#d7d7db", label='Revocação', hatch="/", edgecolor='black')
plt.xlabel('Classe (Resolução do Vídeo)', fontsize=16) 
plt.ylabel('%', fontsize=16)
plt.rcParams['legend.fontsize'] = 16
#plt.title('Notas por pessoa') 

plt.tick_params(axis='both', which='major', labelsize=16)
plt.xticks(indice + bar_larg, ('320', '640', '480', '768', '1024', '1280','1920')) 
plt.legend() 
plt.tight_layout() 
plt.show()
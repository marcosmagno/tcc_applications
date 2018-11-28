from pylab import *
import numpy
import matplotlib.pyplot as plt

grupos = 4
forD = (1075.612903,594.580645,1042.580645,742.193548)
yerr4G = [332.521712, 256.743976, 286.767161, 252.416121]

D2D = (1230.702703,1230.702703,1230.702703,1230.702703)
yerrD2D = [420.637410,420.637410,420.637410,420.637410]

fig, ax = plt.subplots()
indice = np.arange(grupos)
bar_larg = 0.4
transp = 0.7
plt.bar(indice, forD, bar_larg, alpha=transp, color="b", label='4G', hatch="*", edgecolor='black', yerr=yerr4G)

plt.bar(indice, forD, bar_larg, alpha=transp, color="b", hatch="*", edgecolor='black', yerr=yerr4G)

plt.bar(indice, forD, bar_larg, alpha=transp, color="b", hatch="*", edgecolor='black', yerr=yerr4G)

plt.bar(indice, forD, bar_larg, alpha=transp, color="b", hatch="*", edgecolor='black', yerr=yerr4G)

plt.bar(indice + bar_larg, D2D, bar_larg, alpha=transp, color="r", label='4G/D2D', hatch="/", edgecolor='black', yerr=yerrD2D)

#plt.xlabel('Celulares', fontsize=16) 
plt.ylabel('Resolução do Vídeo (média)', fontsize=16)
#plt.title('Notas por pessoa') 

plt.tick_params(axis='both', which='major', labelsize=16)
plt.xticks(indice + bar_larg, ('Celular 1', 'Celular 2', 'Celular 3', 'Celular 4', 'Celular 5')) 
plt.legend() 
plt.tight_layout() 
plt.show()
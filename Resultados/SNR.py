"""
Demo of the errorbar function
"""
# -*- coding: utf-8 -*-
from pylab import *
import numpy
import matplotlib.pyplot as plt


# Dual
x = (0,1)
#	 Single 	 Dual
y = [4.564400, 11.813704]  # media
yerr = [0.290000, 3.160000 ]  # desvio padrao
width = 0.5 

fig = plt.figure()
#ax = plt.axes()
#fig, ax = plt.subplots()

#plt.xticks(np.arange(min(x), max(x)+1, 1.0), x)
# standard error bars
bar1 = plt.bar(x[0], y[0], width=0.60,color='b',   hatch="*", edgecolor='black', yerr=yerr[0])

bar2 = plt.bar(x[1], y[1], width=0.60,  color='r',   yerr=yerr[1], hatch="/", edgecolor='black')

plt.tick_params(axis='both', which='major', labelsize=16)


#plt.xlabel('Node ID')
plt.ylabel('Média do SNR', fontsize=16)
plt.xticks(x, ('4G', '4G/D2D'))
plt.legend()

plt.show()
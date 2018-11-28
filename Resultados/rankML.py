from pylab import *
import numpy
import matplotlib.pyplot as plt

grupos = 9
rank = (0.545, 0.520, 0.491, 0.423, 0.410 , 0.317, 0.270, 0.204, 0.165)


"""
 0.545 +- 0.005     1   +- 0       5 snr
 0.52  +- 0.003     2   +- 0       9 blerup
 0.491 +- 0.013     3   +- 0       3 bratedown
 0.423 +- 0.005     4   +- 0.1     7 mcsup
 0.41  +- 0.006     5   +- 0.1     8 brateup
 0.317 +- 0.001     6   +- 0       2 mcsdown
 0.27  +- 0.002     7   +- 0       1 cqi
 0.204 +- 0.004     8   +- 0       6 phr
 0.165 +- 0.004     9   +- 0       4 blerdown

"""
import matplotlib.pyplot as plt
import numpy as np

# Fixing random state for reproducibility
np.random.seed(19680801)


plt.rcdefaults()
fig, ax = plt.subplots()

# Example data
people = ('snr', 'bler (uplink)', 'brate (downlink)', 'mcs (uplink)', 'brate (uplink)', 'mcs (downlink)','cqi','phr','bler (downlink)')

error = np.random.rand(len(people))

ax.barh(people, rank,  align='center',
        color='#4e1e84', ecolor='black')
ax.set_yticks(people)
ax.set_yticklabels(people)
ax.set_ylabel('Características da rede 4G',fontsize=16)
ax.invert_yaxis()  # labels read top-to-bottom
ax.set_xlabel('Média de ganho de informação',fontsize=16)


plt.show()
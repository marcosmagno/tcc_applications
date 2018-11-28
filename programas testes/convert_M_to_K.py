arq = open('btirate.txt', 'r')
arqResult = open('result.txt', 'w')
texto = arq.readlines()
for i in texto:
	#print(i)
	if(str(i).find("M") != -1):
		value = str(i.split("M")[0])
		try:
			result = float(value)
			multi = result * 100000
			arqResult.write(str(multi)+"\n")
			print(multi)
		except ValueError:
			print("e")
	else:
		arqResult.write(str(i))
#print(texto)
arq.close()
arqResult.close()

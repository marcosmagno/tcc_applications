import socket

clientsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
clientsocket.connect(('150.164.10.58', 10001))
clientsocket.send('-2-00:00:00:00:00:00')
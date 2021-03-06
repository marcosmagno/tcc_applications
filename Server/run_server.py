import select
import socket
import sys
import Queue
#from multiprocessing import Queue
#import mod_server
import logging
import time
# Create a TCP/IP socket
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setblocking(0)

# Bind the socket to the port
server_address = ('192.168.0.105', 10001)
print >>sys.stderr, 'starting up on %s port %s' % server_address
server.bind(server_address)

# Listen for incoming connections
server.listen(5)
# Sockets from which we expect to read
inputs = [ server ]

# Sockets to which we expect to write
outputs = [ ]
global macGo

ips_clients = []
# Outgoing message queues (socket:Queue)
message_queues = {}
devices = {}
# Set Loggin
log = logging.getLogger('geni-delegate')
while inputs:

    # Wait for at least one of the sockets to be ready for processing
    print >>sys.stderr, '\nwaiting for the next event'
    readable, writable, exceptional = select.select(inputs, outputs, inputs)
    # Handle inputs
    for s in readable:
        if s is server:
            # A "readable" server socket is ready to accept a connection
            connection, client_address = s.accept()
            print >>sys.stderr, 'new connection from', client_address
            connection.setblocking(0)
            inputs.append(connection)

            # Give the connection a queue for data we want to send
            message_queues[connection] = Queue.Queue()
        else:
            data = s.recv(1024)
            if data:
                # A readable client socket has data
                print >>sys.stderr, 'received "%s" from %s' % (data, s.getpeername())
                message_queues[s].put(data)
                #print(data)
                splitMsg(data, s)

                # Add output channel for response
                if s not in outputs:
                    outputs.append(s)                
            else:
                # Interpret empty result as closed connection
                print >>sys.stderr, 'closing', client_address, 'after reading no data'
                # Stop listening for input on the connection
                if s in outputs:
                    outputs.remove(s)
                inputs.remove(s)
                s.close()
                # Remove message queue
                del message_queues[s]
    

    # Handle outputs
    for s in writable:
        try:
            next_msg = message_queues[s].get_nowait()
            #print("Socket", s)
            #print("mensagem queue", message_queues)
            #print("writable", writable)
            #print("next_msg", next_msg)

        except Queue.Empty:
            outputs.remove(s)


    def splitMsg(msg, connection):
        # TODO Pegar o IP do GO
        type_msg = msg.split("-")[1]
        
        if type_msg == "1":
            macAddress = msg.split("-")[2]
            devices[macAddress] = connection 
            print("devices", devices)    


            

        if type_msg == "2":
            createGO()

        if type_msg == "3":
            ip_client = msg.split("-")[2]
            ips_clients.append(ip_client)
            print(ips_clients)

        if type_msg == "4":
            print("Enviando para", connection)
            connection.send(str(ips_clients) + "\n")

            #getSocketGo().send(str(ip_client + "\n"))
    

    def createGO():
        print("GO")
        count = 0
        count_port = 0
        global macGo
        global socketGo
        socket_port = [0,1403,1404,1405]

        for mac,socket in devices.items():

            if count == 0:
                print(mac)
                macGo = mac
                print("Mac GO", macGo)
                socketGo = socket
            count = count + 1

        for k,v in devices.items():
            print("macccc", macGo)
            if(k == macGo):
                v.send(macGo + "-" + str(socket_port)+ "\n")
            else:                
                print("Enviando count", count_port)
                msg = str(macGo + "-" + str(socket_port[count_port]) + "\n")
                print("mensagem" + msg)
                v.send(msg)
            count_port = count_port + 1    




 

    #for s in writable:
    #    try:
    #        next_msg = message_queues[s].get_nowait()
    #    except Queue.Empty:
    #        # No messages waiting so stop checking for writability.
    #        print >>sys.stderr, 'output queue for', s.getpeername(), 'is empty'
    #        outputs.remove(s)
    #    else:
    #        print("error")
    #        #data_server = mod_server.Mod_Server()
    #
    #        #validation = data_server.slitp_msg(next_msg)
    #        #print(validation)
    #        #s.send(validation) 
 
    #Handle "exceptional conditions"
    for s in exceptional:
        print >>sys.stderr, 'handling exceptional condition for', s.getpeername()
    #    # Stop listening for input on the connection
        inputs.remove(s)
        if s in outputs:
            outputs.remove(s)
        s.close()

    #    # Remove message queue
        del message_queues[s]
    

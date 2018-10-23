import mysql.connector
import select
import socket
import sys
from multiprocessing import Queue
import threading
import time
import struct
from random import *
import zipfile
import os
from threading import Thread
import subprocess
import getpass


class Mod_Server(object):

    def __init__(self):
        try:
            self.connection_db = mysql.connector.connect(
                host='localhost', user='root', password='server', database='viajap')
            self.cursor = self.connection_db.cursor()
        except mysql.connector.Error as e:
            print("Error code:", e.errno)        # error numbe
            print("SQLSTATE value:", e.sqlstate)  # SQLSTATE value
            print("Error message:", e.msg)       # error message
            print("Error:", e)                   # errno, sqlstate, msg values
            s = str(e)
            print("Error:", s)                   # errno, sqlstate, msg values
        
    def slitp_msg(self, mensagem):
        print(str(mensagem).split("-"))
        if mensagem == "-":
            return "0"
        else:
            type_msg = str(mensagem).split("-")[1]            

            if (type_msg == "1"):
                # TODO - Login
                usuario = str(mensagem).split("-")[2]
                print("Usuario" + str(usuario))
                senha = str(mensagem).split("-")[3]
                print("Senha" + str(senha))
                print("Long request")
                vuser = self.verify_user(usuario,senha)
                if vuser == usuario:
                    print("vuser se e igual")
                    return str("Ok\n")
                else:
                    return str("error\n")
            elif (type_msg == "2"):
                getFullName = str(mensagem).split("-")[2]
                getEmail = str(mensagem).split("-")[3]
                getMobileNumber = str(mensagem).split("-")[4]
                getLocation = str(mensagem).split("-")[5]
                getCnpjCpf = str(mensagem).split("-")[6]
                getChassi = str(mensagem).split("-")[7]
                getPassword = str(mensagem).split("-")[8]
                getConfirmPassword = str(mensagem).split("-")[9]
                print("Sigup request")
                self.cadastra_usuario(getFullName,getEmail,getMobileNumber,getLocation,getCnpjCpf,getChassi,getPassword,getConfirmPassword)
                return str("Ok\n")
            else:
                print("return error")
                return "error\n"

    def verify_user(self,user,password):
        """ Verify user and pass in BD
        
        """
        print("verificando usuario e senha")
        try:            
            query = "SELECT Email,Senha from cadastro_usuarios where Email =" +"'"+ str(user) +"'" +"and Senha =" +"'" +str(password)+"'" +""
            self.cursor.execute(query)
            self.connection_db.commit()
        except mysql.connector.Error as err:
            print("Erro para encontrar o usuario")

        for(Email) in self.cursor:
            print(Email[0])
            return Email[0]

    def cadastra_usuario(self,getFullName,getEmail,getMobileNumber,getLocation,getCnpjCpf,getChassi,getPassword,getConfirmPassword):
        """ Verify user and pass in BD
        
        """
        print("verificando usuario e senha")
        try:
            #Nome,Email,Numero_celular,Endereco,Cnpj_Cpf,Chassi,Senha,Confirmar_senha,Aceita_termo
            
            add_user = ("INSERT INTO cadastro_usuarios"
              "(Nome, Email, Numero_celular, Endereco,Cnpj_Cpf,Chassi,Senha,Confirmar_senha,Aceita_termo) "
              "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)")
            
            data_employee = (getFullName,getEmail,getMobileNumber,getLocation,getCnpjCpf,getChassi,getPassword,getConfirmPassword,"true")
            self.cursor.execute(add_user, data_employee)
            self.connection_db.commit()
            self.cursor.close()
            self.connection_db.close()
        except mysql.connector.Error as err:
            print("Erro para cadastrar o usuario")
            print(err)
        self.connection_db.close()

                

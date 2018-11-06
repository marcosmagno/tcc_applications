package com.example.winet;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocketD2D {

    Socket smtpSocket = null;
    DataOutputStream os_send = null;
    String responseLine = null;

    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    public final static int FILE_SIZE = 6022386;
    int bytesRead;
    int current = 0;

    int count = 0;
    public void startConnection(String IP, int port) {
        // Initialization section:
        // Try to open a socket on port 10001
        // Try to open input and output streams
        Log.d("ClientSocketD2D", "Iniciando Conexao no cliente");
        try {

            this.smtpSocket = new Socket();

            Log.d("ClientSocketD2D", "conectando na porta " + String.valueOf(port));

            this.smtpSocket.connect(new InetSocketAddress("192.168.49.1",port), 1000);
            this.os_send = new DataOutputStream(this.smtpSocket.getOutputStream());




            //InputStream is = smtpSocket.getInputStream();
            //byte [] mybytearray  = new byte [FILE_SIZE];
            //Log.d("ArquivoMybytes", String.valueOf(mybytearray));
            //fos = new FileOutputStream("/storage/emulated/0/Android/data/com.example.exoplayercodelab/cache/downloads/videorecebi.exo");
            //bos = new BufferedOutputStream(fos);

            //bytesRead = is.read(mybytearray,0,mybytearray.length);
            //current = bytesRead;

            //do {
            //    bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
            //    if(bytesRead >= 0) current += bytesRead;
            //} while(bytesRead > -1);

           //bos.write(mybytearray, 0 , current);
           // bos.flush();


            //responseLine = this.is_recev.readUTF();
            //while (true) {
            //    responseLine = d.readLine();
            //    Log.d("yyyyyyRes", responseLine);
           // }

        } catch (UnknownHostException e) {

            Log.d("ClientSocketD2D", "Don't know about host: hostname" );
        } catch (IOException e) {
            Log.d("ClientSocketD2D", "Couldn't get I/O for the connection to: hostname" );

        }

    }

    public void sendDataCellPhone(String data, Context context) {
        if (this.smtpSocket != null && this.os_send != null ) {

            try {
                this.os_send.writeBytes(count + "\n");
                // receive file
                byte[] mybytearray = new byte[FILE_SIZE];
                InputStream is = this.smtpSocket.getInputStream();


                fos = new FileOutputStream("/storage/emulated/0/Android/data/com.example.exoplayercodelab/files/file.recv" + count);
                bos = new BufferedOutputStream(fos);

                Log.d("Mybytes", String.valueOf(mybytearray));
                Log.d("MybytesLength", String.valueOf(mybytearray.length));
                bytesRead = is.read(mybytearray, 0, mybytearray.length);

                current = bytesRead;
                Log.d("Current", String.valueOf(current));
                    do {
                    bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
                    Log.d("BytesRead", String.valueOf(bytesRead));
                    if (bytesRead >= 0) current += bytesRead;
                     } while (bytesRead > -1);
                        bos.write(mybytearray, 0, current);
                        bos.flush();
                        fos.close();
                        Log.d("File", current + "bytes read");


            } catch (UnknownHostException e) {
                Log.d("ClientSocketD2D", "Erro para receber mensagem: " + e);
            } catch (IOException e) {
                Log.d("ClientSocketD2D", "Erro para receber mensagem");
            }
        }


    }



    public void sendFile(final String fileToSend){

    }


}

package com.example.winet;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorSocketD2D {
    int port;
    String go;
    DataOutputStream os_send = null;
    String responseLine = null;
    String fileSolicitation = null;
    DataInputStream is_recev = null;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    OutputStream os = null;
    ReadFile readFile = null;
    public ServidorSocketD2D() {

    }

    public void startSocket(int port, String go, Context context) {

            Log.d("SocketServerD2D", "porta aberta" + port);
            try {
                ServerSocket server = new ServerSocket(port);
                Log.d("SocketServerD2D", "Criando o socket");
                // Cliente
                Socket cliente = server.accept();
                // DataOutputStream to send a msg
                this.is_recev = new DataInputStream(cliente.getInputStream());
                this.os_send = new DataOutputStream(cliente.getOutputStream());

                    // DataInputStream to recv a msg
                    Log.d("SocketServerD2D", "Cliente conectado do IP " + cliente.getInetAddress().getHostAddress());
                    // Buffer to recv file last
                    BufferedReader d = new BufferedReader(new InputStreamReader(this.is_recev));

                        responseLine = d.readLine();
                        Log.d("SocketServerD2D", "Servidor recebeu Ultimo arquivo: " + responseLine);
                        Log.d("Responsile", String.valueOf(responseLine.length()));
                        sendFile(context);

                 /*
                     Procedimento para enviar os arquivos para cada cliente
                 */
           } catch (IOException ex) {
                Log.d("ServidorSocketD2D:", "Erro socket" + ex);
            }


            }

        public void sendFile(Context context){
        try {
            Log.d("ListOfFiless", String.valueOf(getReadFile().getListOfTheFiles().get(0)));
            //BufferedReader in = new BufferedReader(new FileReader("/storage/emulated/0/Android/data/com.example.exoplayercodelab/files/"));
            File file = new File(context.getExternalCacheDir(), "downloads/" + String.valueOf(getReadFile().getListOfTheFiles().get(0)));
            Log.d("FileLocal", String.valueOf(file));

            byte[] mybytearray = new byte[(int) file.length()];
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);


            Log.d("Sending: ", mybytearray.length + "bytes" + "File" + file);
            this.os_send.write(mybytearray, 0, mybytearray.length);
            this.os_send.flush();

            Log.d("ServidorSocketD2D: ", "Done");
        } catch (FileNotFoundException e) {
            Log.d("ServidorSocketD2D: ", "Erro file not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ServidorSocketD2D: ", "error io expt");
            e.printStackTrace();
        }

    }
    public void setReadFile(ReadFile readFile) {
        this.readFile = readFile;
    }

    public ReadFile getReadFile(){
        return this.readFile;
    }
    public void listOfFiles(ReadFile readFile) {


    }
}

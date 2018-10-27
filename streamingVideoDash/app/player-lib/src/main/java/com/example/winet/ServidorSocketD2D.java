package com.example.winet;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorSocketD2D {
    int port;
    String go;
    DataOutputStream os_send = null;
    String responseLine = null;

    public ServidorSocketD2D(int port, String go) throws IOException {
        this.port = port;
        this.go = go;

    }

    public void startSocket() {
    int sending = 0;
        Log.d("SocketServerD2D", "porta aberta" + port);
        try {
            ServerSocket server = new ServerSocket(port);
            Log.d("SocketServerD2D", "Criando o socket");
            while (true) {
                Socket cliente = server.accept();
                Log.d("SocketServerD2D", "Cliente conectado do IP " + cliente.getInetAddress().getHostAddress());
                os_send = new DataOutputStream(cliente.getOutputStream());
                while (sending <= 100) {
                    Log.d("SocketServerD2D", String.valueOf(sending + port));
                    os_send.writeBytes("Enviando para o cliente" + port + sending +  "\n");
                    sending++;
                }
                //server.close();
        }
    } catch (IOException ex) {
        Log.d("SocketServerD2D", "Erro socket" + ex);
    }
    }
}

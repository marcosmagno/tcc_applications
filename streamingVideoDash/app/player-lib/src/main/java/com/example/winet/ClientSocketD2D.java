package com.example.winet;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocketD2D {

    Socket smtpSocket = null;
    DataOutputStream os_send = null;
    String responseLine = null;

    public void startConnection(String IP, int port) {
        // Initialization section:
        // Try to open a socket on port 10001
        // Try to open input and output streams
        Log.d("yyyyyy", "Iniciando Conexao no cliente");
        try {

            this.smtpSocket = new Socket();

            Log.d("yyyyyyPort", String.valueOf(port));

            this.smtpSocket.connect(new InetSocketAddress("192.168.49.1",port), 1000);
            //this.os_send = new DataOutputStream(this.smtpSocket.getOutputStream());
            Log.d("yyyyyy", "tentando conectar" );

            BufferedReader d = new BufferedReader(new InputStreamReader(smtpSocket.getInputStream()));
            //responseLine = this.is_recev.readUTF();
            while (true) {
                responseLine = d.readLine();
                Log.d("yyyyyyRes", responseLine);
            }

        } catch (UnknownHostException e) {

            Log.d("yyyyyy", "Don't know about host: hostname" );
        } catch (IOException e) {
            Log.d("yyyyyy", "Couldn't get I/O for the connection to: hostname" );

        }

    }

    public void sendDataCellPhone(String macAddress) {

        if (this.smtpSocket != null && this.os_send != null) {
            try {
                this.os_send.writeBytes("-"  +"1" + "-" + macAddress +"\n");
                // keep on reading from/to the socket till we receive the "Ok" from SMTP,
                // once we received that then we want to break.
            } catch (UnknownHostException e) {
                Log.d("Error", "Erro para receber mensagem: " + e);
            } catch (IOException e) {
                Log.d("Error", "Erro para receber mensagem");
            }
        }

    }
}

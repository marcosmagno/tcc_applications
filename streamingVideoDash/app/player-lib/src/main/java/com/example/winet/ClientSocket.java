package com.example.winet;


import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocket {

    Socket smtpSocket = null;
    DataOutputStream os_send = null;
    DataInputStream is_recev = null;

    public void startConnection() {
        // Initialization section:
        // Try to open a socket on port 10001
        // Try to open input and output streams
        try {
            this.smtpSocket = new Socket("150.164.10.58", 10001);
            this.os_send = new DataOutputStream(this.smtpSocket.getOutputStream());
            this.is_recev = new DataInputStream(this.smtpSocket.getInputStream());
            Log.d("clienteSocket", "tentando conectar" );
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: hostname");
            Log.d("clienteSocket", "Don't know about host: hostname" );
        } catch (IOException e) {
            Log.d("clienteSocket", "Couldn't get I/O for the connection to: hostname" );
            System.err.println("Couldn't get I/O for the connection to: hostname");
        }

    }

    public void sendDataCellPhone() {
        if (this.smtpSocket != null && this.os_send != null && this.is_recev != null) {
            try {
                 this.os_send.writeBytes("-"  +"1" + "-" + "\n");
                // keep on reading from/to the socket till we receive the "Ok" from SMTP,
                // once we received that then we want to break.
                char responseLine;
                responseLine = this.is_recev.readChar();
                Log.d("clienteSocket","wait responsiline" );
                Log.d("clienteSocket", String.valueOf(responseLine));

            } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + e);
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }

    }



}

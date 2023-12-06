package src.controllers;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientMessage extends Thread{
        private Socket clientMSocket;
        private DataInputStream disMReader;
        private DataOutputStream dosMWriter;
        private boolean isConnected = true;

        private JTextArea textArea1;

        ClientMessage(Socket clientMSocket, JTextArea textArea1) {
            try {
                    this.clientMSocket = clientMSocket;
                    this.disMReader = new DataInputStream(clientMSocket.getInputStream());
                    this.dosMWriter = new DataOutputStream(clientMSocket.getOutputStream());
                    this.textArea1 = textArea1;
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }

        public void run() {
            while (isConnected) {
                try {
                    String message;
                    message = disMReader.readUTF();
                    textArea1.append(message);
                } catch (IOException e) {
                    closeEverything();
                }
            }
        }

        private void closeEverything() {
            try {
                disMReader.close();
                dosMWriter.close();
                clientMSocket.close();
                isConnected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

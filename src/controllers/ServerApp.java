package src.controllers;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.List;

public class ServerApp extends Thread{
    private Socket serverClientSocket;
    private Socket serverMClientSocket;
    private DataInputStream disReader;
    private DataOutputStream dosWriter;
    private DataInputStream disMReader;
    private DataOutputStream dosMWriter;
    private int clientNo;
    private String alias;
    private boolean isRunning = true;
    private JTextArea textArea1;

    ServerApp(Socket inSocket, Socket inFSocket, int ClientNo, JTextArea textArea1) {
        try {
            this.serverClientSocket = inSocket;
            this.serverMClientSocket = inFSocket;
            this.clientNo = ClientNo;
            this.disReader = new DataInputStream(serverClientSocket.getInputStream());
            this.dosWriter = new DataOutputStream(serverClientSocket.getOutputStream());
            this.disMReader = new DataInputStream(serverMClientSocket.getInputStream());
            this.dosMWriter = new DataOutputStream(serverMClientSocket.getOutputStream());
            this.textArea1 = textArea1;

            broadcastMessage("Broadcast: A new client has entered the server. \n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String[] clientMessage;

        while (isRunning) {
            try {
                clientMessage = disReader.readUTF().split(" ");
                processClientMessage(clientMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processClientMessage(String[] message) throws IOException {
        switch (message[0]) {
            case "/join" ->
            {
                this.dosWriter.writeUTF("Server: Connection to the File Exchange Server is Successful" + "\n");
            }
            case "/leave" -> {
                textArea1.append("Client " + clientNo + " is leaving. \n");
                this.dosWriter.writeUTF("Server: Connection closed. Thank you! \n");
                isRunning = false;
                closeEverything();
            }
            case "/register" -> {
                alias = message[1];
                textArea1.append("Client " + clientNo  + " is registering. \n");
                if (!Server.isAliasRegistered(alias)) {
                    Server.registerAlias(alias);
                    this.dosWriter.writeUTF("Server: Welcome " + alias + "\n");
                } else {
                    this.dosWriter.writeUTF("Error: Registration failed. Handle or alias already exists. \n");
                }
            }
            case "/store" -> {
                textArea1.append("Client " + clientNo + " is storing " + message[1] + "\n");
                receiveFile(message[1]);
            }
            case "/dir" -> {
                textArea1.append("Client " + clientNo + " is checking directory. \n");
                getDirectory();
            }
            case "/get" -> {
                textArea1.append("Client " + clientNo + " is getting " + message[1] + "\n");
                sendFile(message[1]);

            }
        }
    }

    private void sendFile(String fileName) throws IOException {
        String filePath = new File("").getAbsolutePath();
        filePath = filePath.concat("\\src\\controllers\\serverFiles\\" + fileName);

        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream fileIS = new FileInputStream(file);
            int bytes;

            //send file's length to client
            this.dosWriter.writeLong(file.length());

            //segment the file into chunks
            byte[] buffer = new byte[4 * 1024];

            while((bytes = fileIS.read(buffer)) != -1) {
                this.dosWriter.write(buffer, 0, bytes);
                this.dosWriter.flush();
            }

            //close the file
            fileIS.close();

            this.dosWriter.writeUTF("Server: File received from Server: " + fileName + "\n");
        } else {
            this.dosWriter.writeLong(-1);
            this.dosWriter.writeUTF("Server: Error: File not found in the server. \n");
        }
    }

    private void receiveFile(String fileName) throws IOException{
        //read file length from server
        long fileSize = this.disReader.readLong();

        if (fileSize > 0) {
            String filePath = new File("").getAbsolutePath();
            filePath = filePath.concat("\\src\\controllers\\serverFiles\\" + fileName);
            FileOutputStream fileOS = new FileOutputStream(filePath);
            int bytes;

            //segment the file into chunks
            byte[] buffer = new byte[4 * 1024];

            while(fileSize > 0 && (bytes = this.disReader.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) != -1) {
                //send file to client socket
                fileOS.write(buffer, 0, bytes);
                fileSize -= bytes;
            }

            //close the file
            fileOS.close();

            this.dosWriter.writeUTF("Server: " + this.alias + Server.log() + ": Uploaded " + fileName + "\n");
        }
    }

    private void getDirectory() throws IOException {
        String folderPath = new File("").getAbsolutePath() + "\\src\\controllers\\serverFiles";
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                this.dosWriter.writeInt(files.length);
                for (File file : files) {
                    if (file.isFile()) {
                        this.dosWriter.writeUTF(file.getName() + "\n");
                    }
                }
            }
        } else {
            textArea1.append("The serverFiles folder does not exist or is not a directory.\n");
        }
    }

    private void broadcastMessage(String message) {
        List<ServerApp> serverApps = Server.getServerApps();
        for (ServerApp serverApp : serverApps) {
            try {
                serverApp.dosMWriter.writeUTF(message);
                serverApp.dosMWriter.flush();
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    private void closeEverything() {
        Server.removeServerApp(this);
        try {
            if (this.dosWriter != null) {
                this.dosWriter.close();
            }
            if (this.disReader != null) {
                this.disReader.close();
            }
            if (this.disMReader != null) {
                this.disMReader.close();
            }
            if (this.dosMWriter != null) {
                this.dosMWriter.close();
            }
            if (serverClientSocket != null) {
                serverClientSocket.close();
            }
            if (serverMClientSocket != null) {
                serverMClientSocket.close();
            }
            if (alias != null) {
                Server.unregisterAlias(alias);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



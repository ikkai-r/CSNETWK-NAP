package src.controllers;

import java.net.*;
import java.io.*;
import javax.swing.*;

public class Client {
    public Client(JTextArea textArea1) {
        this.textArea1 = textArea1;
    }
    private JTextArea textArea1;
    private boolean isRunning = false;
    private String greeting = null;
    private Socket clientSocket = null;
    private Socket clientMSocket = null;
    private DataInputStream disReader = null;
    private DataOutputStream dosWriter = null;
    public void clientStart() throws Exception {

        textArea1.append("Client is ready to connect to a server\n");
        isRunning = true;

    }

    private boolean validateInput(String[] inputArr) {
        int expectedLength = switch (inputArr[0]) {
            case "/join" -> 3;
            case "/leave", "/dir", "/?" -> 1;
            case "/register", "/store", "/get" -> 2;
            default -> inputArr.length;
        };

        return inputArr.length == expectedLength;
    }

    public void getInput(String input) throws Exception {

        try {

        if(isRunning) {
            String[] inputArr = input.split(" ");

            if (validateInput(inputArr)) {

                switch (inputArr[0]) {
                    case "/join" -> {
                        try {
                            if (clientSocket == null) {

                                clientSocket = new Socket(inputArr[1], Integer.parseInt(inputArr[2]));
                                clientMSocket = new Socket(inputArr[1], 5050);

                                disReader = new DataInputStream(clientSocket.getInputStream());
                                dosWriter = new DataOutputStream(clientSocket.getOutputStream());

                                dosWriter.writeUTF(input);
                                textArea1.append(disReader.readUTF());

                                ClientMessage cMessage = new ClientMessage(clientMSocket, textArea1);
                                cMessage.start();

                            } else {
                                textArea1.append("Error: You're already connected to the server.\n");
                            }
                        } catch (Exception e) {
                            textArea1.append("Error: Connection to the Server has failed! Please check IP Address and Port Number.\n");
                            e.printStackTrace();
                        }
                    }
                    case "/leave" -> {
                        if (clientSocket == null) {
                            textArea1.append("Error: Disconnection failed. Please connect to the server first.\n");
                        } else {
                            isRunning = false;
                            dosWriter.writeUTF(input);
                            textArea1.append(disReader.readUTF());
                            closeAllConnections();
                            clientStart();
                        }
                    }
                    case "/get" -> {

                        if (clientSocket == null) {
                            textArea1.append("Error: Please connect to the server first.\n");
                        } else if (greeting == null) {
                            textArea1.append("Error: Please register to the server first.\n");
                        } else {
                            dosWriter.writeUTF(input);
                            receiveFile(inputArr[1], disReader);
                        }
                    }
                    case "/store" -> {
                        if (clientSocket == null) {
                            textArea1.append("Error: Please connect to the server first.\n");
                        } else if (greeting == null) {
                            textArea1.append("Error: Please register to the server first.\n");
                        } else {
                            dosWriter.writeUTF(input);
                            sendFile(inputArr[1], disReader, dosWriter);
                        }
                    }
                    case "/dir" -> {
                        if (clientSocket == null) {
                            textArea1.append("Error: Please connect to the server first.\n");
                        } else if (greeting == null) {
                            textArea1.append("Error: Please register to the server first.\n");
                        } else  {
                            dosWriter.writeUTF(input);
                            getDirectory(disReader);
                        }
                    }
                    case "/register" -> {
                        if (clientSocket == null) {
                            textArea1.append("Error: Please connect to the server first.\n");
                        } else if (greeting != null) {
                            textArea1.append("Error: You're already registered to the server.\n");
                        } else {
                            dosWriter.writeUTF(input);
                            greeting = disReader.readUTF();
                            textArea1.append(greeting+"\n");
                        }
                    }
                    case "/?" -> printCmds();
                    default -> textArea1.append("Error: Command not found.\n");
                }
            } else {
                textArea1.append("Error: Command parameters do not match or are not allowed.\n");
            }
        } else {
            textArea1.append("Error: Server is not running\n");
            closeAllConnections();
        }
        } catch (java.net.SocketException e) {
            textArea1.append("Server connection reset by peer: Server is offline \n");
            closeAllConnections();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeAllConnections() throws IOException {
            if(clientSocket != null) {
                clientSocket.close();
            }
            if(clientMSocket != null) {
                clientMSocket.close();
            }
            if (dosWriter != null) {
                dosWriter.close();
            }
            if (disReader != null) {
                disReader.close();
            }

            clientSocket = null;
            clientMSocket = null;
            dosWriter = null;
            disReader = null;
            greeting = null;
    }

    private void sendFile(String fileName, DataInputStream disReader, DataOutputStream dosWriter) throws IOException {
        String filePath = new File("").getAbsolutePath();
        filePath = filePath.concat("\\src\\controllers\\clientFiles\\" + fileName);

        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream fileIS = new FileInputStream(file);
            int bytes;

            //send file's length to client
            dosWriter.writeLong(file.length());

            //segment the file into chunks
            byte[] buffer = new byte[4 * 1024];

            while((bytes = fileIS.read(buffer)) != -1) {
                dosWriter.write(buffer, 0, bytes);
                dosWriter.flush();
            }

            //close the file
            fileIS.close();
            textArea1.append(disReader.readUTF()+"\n");
        } else {
            dosWriter.writeLong(-1);
            textArea1.append("Error: File not found.\n");
        }
    }

    private void receiveFile(String fileName, DataInputStream disReader) throws IOException{
        //read file length from server
        long fileSize = disReader.readLong();
        if (fileSize > 0) {
            String filePath = new File("").getAbsolutePath();
            filePath = filePath.concat("\\src\\controllers\\clientFiles\\" + fileName);
            FileOutputStream fileOS = new FileOutputStream(filePath);
            int bytes;

            //segment the file into chunks
            byte[] buffer = new byte[4 * 1024];

            while(fileSize > 0 && (bytes = disReader.read(buffer, 0, (int)Math.min(buffer.length, fileSize))) != -1) {
                //send file to client socket
                fileOS.write(buffer, 0, bytes);
                fileSize -= bytes;
            }

            //close the file
            fileOS.close();
        }
        textArea1.append(disReader.readUTF()+"\n");
    }

    private void getDirectory(DataInputStream disReader) throws IOException {
        textArea1.append("Server Directory\n");
        int fileLength = disReader.readInt();

        for (int i = 0; i < fileLength; i++) {
            textArea1.append(disReader.readUTF());
        }
    }

    private void printCmds() {
        String[] cmds = {"/join <server_ip_add> <port>", "/leave", "/register <handle>",
                         "/store <filename>", "/dir", "/get <filename>", "/?"};
        String[] desc = {"Connect to the server application", "Disconnect to the server application",
                         "Register a unique handle or alias", "Send file to server",
                         "Request directory file list from a server", "Fetch a file from a server",
                         "Request command help to output all Input Syntax commands for references"};

        for (int i = 0; i < cmds.length; i++) {
            textArea1.append(cmds[i] + ": " + desc[i] +"\n");
        }
    }
}

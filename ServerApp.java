import java.net.*;
import java.io.*;

public class ServerApp extends Thread{
    Socket serverClientSocket;
    int clientNo;
    String alias;
    boolean isRunning = true;

    ServerApp(Socket inSocket, int ClientNo) {
        serverClientSocket = inSocket;
        clientNo = ClientNo;
    }

    public void run() {
        try {
            DataInputStream disReader = new DataInputStream(serverClientSocket.getInputStream());
            DataOutputStream dosWriter = new DataOutputStream(serverClientSocket.getOutputStream());

            String[] clientMessage;

            while (isRunning) {
                clientMessage = disReader.readUTF().split(" ");
                processClientMessage(clientMessage, dosWriter);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void processClientMessage(String[] message, DataOutputStream dosWriter) throws IOException {
        switch (message[0]) {
            case "/join" -> {
                dosWriter.writeUTF("Connection to the File Exchange Server is Succesful");
            }
            case "/leave" -> {
                System.out.println("Client " + clientNo + " is leaving.");
                dosWriter.writeUTF("Connection closed. Thank you!");
                isRunning = false;
                if (alias != null) {
                    Server.unregisterAlias(alias);
                }
                serverClientSocket.close();
            }
            case "/register" -> {
                alias = message[1];
                System.out.println("Client " + clientNo  + " is registering.");

                if (!Server.isAliasRegistered(alias)) {
                    Server.registerAlias(alias);
                    dosWriter.writeUTF("Welcome " + alias);
                } else {
                    dosWriter.writeUTF("Error: Registration failed. Handle or alias already exists.");
                }
            }
            case "/store" -> {
                System.out.println("Client " + clientNo + " is storing.");
                dosWriter.writeUTF("File stored successfully!");
            }
            case "/dir" -> {
                System.out.println("Client " + clientNo + " is checking directory.");
                dosWriter.writeUTF("Directory contents: file1.txt, file2.txt");
            }
            case "/get" -> {
                System.out.println("Client " + clientNo + " is getting.");
                dosWriter.writeUTF("File content: This is the content of the requested file.");
            }
            case "/?" -> {
                System.out.println("Client " + clientNo + " is requesting help.");
                dosWriter.writeUTF("Available commands: /register, /store, /dir, /get, /leave");
            }
            default -> dosWriter.writeUTF("Invalid command. Type /? for help.");
        }
    }
}



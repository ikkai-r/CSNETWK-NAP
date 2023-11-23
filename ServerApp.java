import java.net.*;
import java.io.*;

public class ServerApp extends Thread{
    private Socket serverClientSocket;
    private int clientNo;
    private String alias;
    private boolean isRunning = true;

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
                processClientMessage(clientMessage, dosWriter, disReader);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void processClientMessage(String[] message, DataOutputStream dosWriter, DataInputStream disReader) throws IOException {
        switch (message[0]) {
            case "/join" -> dosWriter.writeUTF("Connection to the File Exchange Server is Succesful");
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
                System.out.println("Client " + clientNo + " is storing " + message[1]);
                receiveFile(message[1], dosWriter, disReader, alias);
            }
            case "/dir" -> {
                System.out.println("Client " + clientNo + " is checking directory.");
                getDirectory(dosWriter);
            }
            case "/get" -> {
                System.out.println("Client " + clientNo + " is getting " + message[1]);
                sendFile(message[1], dosWriter);

            }
        }
    }

    private void sendFile(String fileName, DataOutputStream dosWriter) throws IOException {
        String filePath = new File("").getAbsolutePath();
        filePath = filePath.concat("\\serverFiles\\" + fileName);

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

            dosWriter.writeUTF("File received from Server: " + fileName);
        } else {
            dosWriter.writeLong(-1);
            dosWriter.writeUTF("Error: File not found in the server.");
        }
    }
    private static void receiveFile(String fileName, DataOutputStream dosWriter, DataInputStream disReader, String alias) throws IOException{
        //read file length from server
        long fileSize = disReader.readLong();

        if (fileSize > 0) {
            String filePath = new File("").getAbsolutePath();
            filePath = filePath.concat("\\serverFiles\\" + fileName);
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

            dosWriter.writeUTF(alias + Server.log() + ": Uploaded " + fileName);
        }
    }

    private static void getDirectory(DataOutputStream dosWriter) throws IOException {
        String folderPath = new File("").getAbsolutePath() + "\\serverFiles";
        File folder = new File(folderPath);

        System.out.println(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                dosWriter.writeInt(files.length);
                for (File file : files) {
                    if (file.isFile()) {
                        dosWriter.writeUTF(file.getName());
                    }
                }
            }
        } else {
            System.out.println("The serverFiles folder does not exist or is not a directory.");
        }
    }
}



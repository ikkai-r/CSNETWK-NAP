import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class ServerWorker implements Runnable{

    public static ArrayList<ServerWorker> serverWorkers = new ArrayList<>();
    private Socket serverClientSocket;
    private DataOutputStream dosWriter;
    private DataInputStream disReader;
    private BufferedWriter bWriter;
    private BufferedReader bReader;
    private int clientNo;
    private String clientAlias;

    public ServerWorker(Socket inSocket, int clientNo) {
        try {
            this.serverClientSocket = inSocket;
            this.clientNo = clientNo;
            this.dosWriter = new DataOutputStream(serverClientSocket.getOutputStream());
            this.disReader = new DataInputStream(serverClientSocket.getInputStream());
            this.bWriter = new BufferedWriter(new OutputStreamWriter(serverClientSocket.getOutputStream()));
            this.bReader = new BufferedReader(new InputStreamReader(serverClientSocket.getInputStream()));

            // adds this instance to the list of serverWorkers
            serverWorkers.add(this);
            broadcastMessage("A client has entered the server.");
            
        } catch(IOException e) {
            closeEverything(serverClientSocket, dosWriter, disReader, bWriter, bReader);
        }
    }
    
    @Override
    public void run() {
        String[] clientCommand;

        while(serverClientSocket.isConnected()) {
            try {
                clientCommand = disReader.readUTF().split(" ");
                processClientCommand(clientCommand, dosWriter, disReader);
            } catch (IOException e) {
                closeEverything(serverClientSocket, dosWriter, disReader, bWriter, bReader);
                break;
            }
        }
    }

    private void processClientCommand(String[] command, DataOutputStream dosWriter, DataInputStream disReader) throws IOException {
        switch (command[0]) {
            case "/leave" -> {
                System.out.println("Client " + clientNo + " is leaving.");
                dosWriter.writeUTF("Connection closed. Thank you!");
                //dosWriter.writeUTF("Connection closed. Thank you!");
                closeEverything(serverClientSocket, dosWriter, disReader, bWriter, bReader);
            }
            case "/register" -> {
                String alias = command[1];
                System.out.println("Client " + this.clientNo  + " is registering.");
                registerAlias(alias);
                System.out.println("Sending greeting to client.");
                dosWriter.writeUTF("Welcome");
                System.out.println("Sent greeting to client.");
                
                /*
                if (!isAliasRegistered(alias)) {
                    registerAlias(alias);
                    dosWriter.writeUTF("Welcome " + this.clientAlias);
                } else {
                    dosWriter.writeUTF("Error: Registration failed. Handle or alias already exists.");
                }

                */
            }
            /*
            case "/store" -> {
                String message = this.clientAlias + " is storing " + command[1];
                System.out.println(message);
                receiveFile(command[1], dosWriter, disReader, this.clientAlias);
                broadcastMessage(message);
            }
            case "/dir" -> {
                System.out.println("Client " + this.clientNo + " is checking directory.");
                getDirectory(dosWriter);
            }
            case "/get" -> {
                System.out.println("Client " + this.clientNo + " is getting " + command[1]);
                sendFile(command[1], dosWriter);
            }
            */
        }
    }

    /* 
    public boolean isAliasRegistered(String alias) {
        boolean isRegistered = false;
        for(ServerWorker worker : serverWorkers) {
            if(worker.clientAlias != null) {
                if(worker.clientAlias.equals(alias)) {
                    isRegistered = true;
                }
            }    
        }
        return isRegistered;
    }

    */

    public void registerAlias(String alias) {
        this.clientAlias = alias;
        System.out.println("Alias registered");
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

            dosWriter.writeUTF(alias + Server.log() + ": Uploaded ");
        }
    }

    private void getDirectory(DataOutputStream dosWriter) throws IOException {
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

    private void broadcastMessage(String message) {
        for (ServerWorker worker : serverWorkers) {
            try {
                worker.bWriter.write(message);
                worker.bWriter.newLine();
                worker.bWriter.flush();
            } catch (IOException e) {
                closeEverything(serverClientSocket, dosWriter, disReader, bWriter, bReader);
            }
        }
    }

    public void disconnectClient() {
        serverWorkers.remove(this);
        broadcastMessage("Client" + clientNo + "left the server.");
    }

    public void closeEverything(Socket clientServerSocket, DataOutputStream dosWriter, DataInputStream disReader, BufferedWriter bWriter, BufferedReader bReader) {
        disconnectClient();
        try {
            if(dosWriter != null) {
                dosWriter.close();
            }

            if(disReader != null) {
                disReader.close();
            }

            if(bWriter != null) {
                bWriter.close();
            }

            if(bReader != null) {
                bReader.close();
            }

            if(clientServerSocket != null) {
                clientServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

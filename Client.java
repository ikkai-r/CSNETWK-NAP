import java.util.Scanner;
import java.io.*;
import java.net.*;

public class Client {
    private Socket clientSocket;
    private DataInputStream disReader;
    private DataOutputStream dosWriter;
    private String alias = null;

    public Client(Socket socket) {
        try {
            this.clientSocket = socket;
            this.dosWriter = new DataOutputStream(this.clientSocket.getOutputStream());
            this.disReader = new DataInputStream(this.clientSocket.getInputStream());
        } catch(IOException e) {
            closeEverything(clientSocket, dosWriter, disReader);
        }
    }

    public void getCommand() {
        try {
            Scanner scanner = new Scanner(System.in);
            String input;
            String[] inputArr;
            while(clientSocket.isConnected()) {
                input = scanner.nextLine();
                inputArr = input.split(" ");

                // Assumes client is already connected to the server
                if (validateInput(inputArr)) {
                    switch (inputArr[0]) {
                        case "/join" -> {
                            System.out.println("Error: You're already connected to the server.");
                        }

                        case "/register" -> {
                            System.out.println("Registering");
                            dosWriter.writeUTF(input);
                            this.alias = inputArr[1];
                            System.out.println("Done");
                        }
        
                        case "/leave" -> {
                            dosWriter.writeUTF(input);
                            closeEverything(clientSocket, dosWriter, disReader);
                        }

                        case "/get" -> {
                            if (this.alias == null) {
                                System.out.println("Error: Please register to the server first.");
                            } else {
                                dosWriter.writeUTF(input);
                                receiveFile(inputArr[1], disReader);
                            }
                        }

                        case "/store" -> {
                            if (this.alias == null) {
                                System.out.println("Error: Please register to the server first.");
                            } else {
                                dosWriter.writeUTF(input);
                                sendFile(inputArr[1], disReader, dosWriter);
                            }
                        }

                        case "/dir" -> {
                            dosWriter.writeUTF(input);
                        }
                        
                        case "/?" -> printCmds();
                        default -> System.out.println("Error: Command not found.");
                    }
                } else {
                    System.out.println("Error: Command parameters do not match or are not allowed.");
                }
            }
        } catch(IOException e) {
            System.out.println("Calling 1");
            closeEverything(clientSocket, dosWriter, disReader);
        }
    }

    public void listenForMessageFromServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                
                while(clientSocket.isConnected()) {
                    try {
                        message = disReader.readUTF();
                        System.out.println(message);
                    } catch(IOException e) {
                        closeEverything(clientSocket, dosWriter, disReader);
                        break;
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket clientServerSocket, DataOutputStream dosWriter, DataInputStream disReader) {
        try {
            if(dosWriter != null) {
                dosWriter.close();
            }

            if(disReader != null) {
                disReader.close();
            }

            if(clientServerSocket != null) {
                clientServerSocket.close();
            }

            System.out.println("Connection ended.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        Boolean isConnectedToServer = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome Client");

        String input;
        String[] inputArr;

        Socket clientSocket;

        while(!isConnectedToServer) {
            input = scanner.nextLine();
            inputArr = input.split(" ");

            // Client needs to join first before we create a Client instance
            if (validateInput(inputArr)) {
                switch (inputArr[0]) {
                    case "/join" -> {
                        try {
                            String ipAddress = inputArr[1];
                            int port = Integer.parseInt(inputArr[2]);
                            clientSocket = new Socket(ipAddress, port);
                            Client client = new Client(clientSocket);
                            client.listenForMessageFromServer();
                            client.getCommand();
                            isConnectedToServer = true;
                            System.out.println("You are connected to the Server");
                        } catch (Exception e) {
                            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
                        }
                    }
                    case "/leave" -> {
                        System.out.println("Error: Disconnection failed. Please connect to the server first.");
                    }
                    case "/get" -> {
                        System.out.println("Error: Please connect to the server first.");
                    }
                    case "/store" -> {
                        System.out.println("Error: Please connect to the server first.");
                    }
                    case "/dir" -> {
                        System.out.println("Error: Please connect to the server first.");
                    }
                    case "/register" -> {
                        System.out.println("Error: Please connect to the server first.");
                    }
                    case "/?" -> printCmds();
                    default -> System.out.println("Error: Command not found.");
                }
            } else {
                System.out.println("Error: Command parameters do not match or are not allowed.");
            }
        }
    }

    private static boolean validateInput(String[] inputArr) {
        int expectedLength = switch (inputArr[0]) {
            case "/join" -> 3;
            case "/leave", "/dir", "/?" -> 1;
            case "/register", "/store", "/get" -> 2;
            default -> inputArr.length;
        };

        return inputArr.length == expectedLength;
    }

    private static void sendFile(String fileName, DataInputStream disReader, DataOutputStream dosWriter) throws IOException {
        String filePath = new File("").getAbsolutePath();
        filePath = filePath.concat("\\clientFiles\\" + fileName);

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
            System.out.println("file uploaded");
        } else {
            dosWriter.writeLong(-1);
            System.out.println("Error: File not found.");
        }
    }

    private static void receiveFile(String fileName, DataInputStream disReader) throws IOException{
        //read file length from server
        System.out.println("Getting File Size");
        long fileSize = disReader.readLong();
        System.out.println("File Size: " + fileSize);
        if (fileSize > 0) {
            String filePath = new File("").getAbsolutePath();
            filePath = filePath.concat("\\clientFiles\\" + fileName);
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
    }

    private static void printCmds() {
        String[] cmds = {"/join <server_ip_add> <port>", "/leave", "/register <handle>",
                         "/store <filename>", "/dir", "/get <filename>", "/?"};
        String[] desc = {"Connect to the server application", "Disconnect to the server application",
                         "Register a unique handle or alias", "Send file to server",
                         "Request directory file list from a server", "Fetch a file from a server",
                         "Request command help to output all Input Syntax commands for references"};

        for (int i = 0; i < cmds.length; i++) {
            System.out.println(cmds[i] + ": " + desc[i]);
        }
    }
}

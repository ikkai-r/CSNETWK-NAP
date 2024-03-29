import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String greeting = null;
        Socket clientSocket = null;
        Socket clientMSocket = null;
        DataInputStream disReader = null;
        DataOutputStream dosWriter = null;


        System.out.println("Client is ready to connect to a server");
        boolean isRunning = true;

        try {
            while(isRunning) {
                String input = scanner.nextLine();
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
                                    System.out.println(disReader.readUTF());

                                    ClientMessage cMessage = new ClientMessage(clientMSocket);
                                    cMessage.start();


//                                    DataInputStream finalDisMReader = disMReader;
//                                    AtomicBoolean finalIsConnected = new AtomicBoolean(true);
//                                    new Thread(() -> {
//                                        try {
//                                            String message;
//                                            while(finalIsConnected.get()) {
//                                                message = finalDisMReader.readUTF();
//                                                System.out.println(message);
//                                            }
//
//                                        } catch (IOException e) {
//                                            try {
//                                                finalDisMReader.close();
//                                                finalIsConnected.set(false);
//                                            } catch (IOException ex) {
//                                                ex.printStackTrace();
//                                            }
//                                        }
//                                    }).start();


                                } else {
                                    System.out.println("Error: You're already connected to the server.");
                                }
                            } catch (Exception e) {
                                System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
                            }
                        }
                        case "/leave" -> {
                            if (clientSocket == null) {
                                System.out.println("Error: Disconnection failed. Please connect to the server first.");
                            } else {
                                isRunning = false;
                                dosWriter.writeUTF(input);
                                System.out.println(disReader.readUTF());
                            }
                        }
                        case "/get" -> {

                            if (clientSocket == null) {
                                System.out.println("Error: Please connect to the server first.");
                            } else if (greeting == null) {
                                System.out.println("Error: Please register to the server first.");
                            } else {
                                dosWriter.writeUTF(input);
                                receiveFile(inputArr[1], disReader);
                            }
                        }
                        case "/store" -> {
                            if (clientSocket == null) {
                                System.out.println("Error: Please connect to the server first.");
                            } else if (greeting == null) {
                                System.out.println("Error: Please register to the server first.");
                            } else {
                                dosWriter.writeUTF(input);
                                sendFile(inputArr[1], disReader, dosWriter);
                            }
                        }
                        case "/dir" -> {
                            if (clientSocket == null) {
                                System.out.println("Error: Please connect to the server first.");
                            } else {
                                dosWriter.writeUTF(input);
                                getDirectory(disReader);
                            }
                        }
                        case "/register" -> {
                            if (clientSocket == null) {
                                System.out.println("Error: Please connect to the server first.");
                            } else if (greeting != null) {
                                System.out.println("Error: You're already registered to the server.");
                            } else {
                                dosWriter.writeUTF(input);
                                greeting = disReader.readUTF();
                                System.out.println(greeting);
                            }
                        }
                        case "/?" -> printCmds();
                        default -> System.out.println("Error: Command not found.");
                    }
                } else {
                    System.out.println("Error: Command parameters do not match or are not allowed.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(clientSocket != null) {
                clientSocket.close();
            }
            if(clientMSocket != null) {
                clientMSocket.close();
            }
            if (clientMSocket != null) {
                clientMSocket.close();
            }
            if (dosWriter != null) {
                dosWriter.close();
            }
            if (disReader != null) {
                disReader.close();
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
            System.out.println(disReader.readUTF());
        } else {
            dosWriter.writeLong(-1);
            System.out.println("Error: File not found.");
        }
    }

    private static void receiveFile(String fileName, DataInputStream disReader) throws IOException{
        //read file length from server
        long fileSize = disReader.readLong();
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
        System.out.println(disReader.readUTF());
    }

    private static void getDirectory(DataInputStream disReader) throws IOException {
        System.out.println("Server Directory");
        int fileLength = disReader.readInt();

        for (int i = 0; i < fileLength; i++) {
            System.out.println(disReader.readUTF());
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

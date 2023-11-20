import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        Socket clientSocket = null;
        DataInputStream disReader = null;
        DataOutputStream dosWriter = null;

        System.out.println("Client is ready to connect to a server");
        boolean isRunning = true;
        try {
            while(isRunning) {
                System.out.print("Input: ");
                String input = scanner.nextLine();
                String[] inputArr = input.split(" ");

                //pachex if edeps pa to isimplify
                switch (inputArr[0]) {
                    case "/join", "/leave", "/register", "/store", "/dir", "/get", "/?" -> {
                        if (validateInput(inputArr)) {
                            switch (inputArr[0]) {
                                case "/join" -> {
                                    try {
                                        clientSocket = new Socket(inputArr[1], Integer.parseInt(inputArr[2]));
                                        disReader = new DataInputStream(clientSocket.getInputStream());
                                        dosWriter = new DataOutputStream(clientSocket.getOutputStream());
                                        dosWriter.writeUTF(String.join(" ", inputArr));
                                        System.out.println(disReader.readUTF());
                                    } catch(Exception e){
                                        System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
                                    }
                                }
                                case "/leave" -> {
                                    if (clientSocket == null) {
                                        System.out.println("Error: Disconnection failed. Please connect to the server first.");
                                    } else {
                                        isRunning = false;
                                        dosWriter.writeUTF(String.join(" ", inputArr));
                                        System.out.println(disReader.readUTF());
                                    }
                                }
                                case "/register", "/store", "/dir", "/get" -> {
                                    if (clientSocket == null) {
                                        System.out.println("Error: Please connect to the server first.");
                                    } else {
                                        dosWriter.writeUTF(String.join(" ", inputArr));
                                        System.out.println(disReader.readUTF());
                                    }
                                }
                            }
                        } else {
                            System.out.println("Error: Command parameters do not match or are not allowed.");
                        }
                    }
                    default -> System.out.println("Error: Command not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(clientSocket != null) {
                clientSocket.close();
            }
        }
    }
    private static boolean validateInput(String[] inputArr) {
        int expectedLength = switch (inputArr[0]) {
            case "/join" -> 3;
            case "/leave", "/dir", "/?" -> 1;
            case "/register", "/store", "/get" -> 2;
            default -> 0;
        };

        return inputArr.length == expectedLength;
    }
}

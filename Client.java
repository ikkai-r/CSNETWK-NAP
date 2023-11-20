import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Socket clientSocket;
        System.out.println("Client is ready to connect to a server");
        boolean isRunning = true;

        try {
            while(isRunning) {
                System.out.print("Input: ");
                String input = scanner.nextLine();
                String[] inputArr = input.split(" ");

                switch(inputArr[0]) {
                    case "/join" -> {
                        clientSocket = new Socket(inputArr[1], Integer.parseInt(inputArr[2]));
                        System.out.println("Client connected to server at " + clientSocket.getRemoteSocketAddress());
                    }
                    case "/leave" -> {
                        System.out.println("leave");
                        isRunning = false;
                    }
                    case "/register" -> {
                        System.out.println("register");
                    }
                    case "/store" -> {
                        System.out.println("store");
                    }
                    case "/dir" -> {
                        System.out.println("dir");
                    }
                    case "/get" -> {
                        System.out.println("get");
                    }
                    case "/?" -> {
                        System.out.println("?");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Client connection is terminated.");
        }
    }
}

import java.net.*;
import java.io.*;

public class ServerApp extends Thread{
    Socket serverClientSocket;
    int clientNo;

    ServerApp(Socket inSocket, int ClientNo) {
        serverClientSocket = inSocket;
        clientNo = ClientNo;
    }

    public void run() {
        try {
            DataInputStream inputStream = new DataInputStream(serverClientSocket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(serverClientSocket.getOutputStream());

            String clientMessage = inputStream.readUTF();
            String serverMessage = "";

            outputStream.writeUTF("Client " + clientNo + " says " + clientMessage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

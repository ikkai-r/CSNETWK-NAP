import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private ServerSocket serverSocket;
    private int count = 0;

    public Server (ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            System.out.println("Server started. Waiting for clients");
            while(!serverSocket.isClosed()) {
                count++;

                Socket socket = serverSocket.accept();
                System.out.println("New Client has connected!");

                ServerWorker serverWorker = new ServerWorker(socket, count);

                Thread thread = new Thread(serverWorker);
                thread.start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        int PORT = 8080;
        ServerSocket serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.startServer();
    }

    public static String log() {
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return  "<" + timestamp.format(formatter) + ">";
    }

    public void closeServerSocket() {
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
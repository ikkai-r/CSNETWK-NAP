import java.net.*;
public class Server {
    public static void main(String[] args) throws Exception {
        //Listen to port
        ServerSocket server = new ServerSocket(8080);

        int count = 0;
        System.out.println("Server Started...");

        while(true) {
            count++;

            //Accept requests and wait until client connects
            Socket serverClientSocket = server.accept();
            System.out.println("Client " + count);

            ServerApp sa = new ServerApp(serverClientSocket, count);
            sa.start();
        }
    }
}

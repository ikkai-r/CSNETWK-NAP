import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private static final int PORT = 8080;
    private static List<String> registeredAliases = new ArrayList<>();
    private static List<ServerApp> connectedClients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        // Listen to port
        ServerSocket server = new ServerSocket(PORT);

        int count = 0;
        System.out.println("Server Started...");

        while (true) {
            count++;

            // Accept requests and wait until client connects
            Socket serverClientSocket = server.accept();
            ServerApp sa = new ServerApp(serverClientSocket, count);
            connectedClients.add(sa);
            sa.start();

            broadcast();
        }
    }

    public static boolean isAliasRegistered(String alias) {
        return registeredAliases.contains(alias);
    }

    public static void registerAlias(String alias) {
        registeredAliases.add(alias);
    }

    public static void unregisterAlias(String alias) {
        registeredAliases.remove(alias);
    }

    public static String log() {
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return "<" + timestamp.format(formatter) + ">";
    }

    public static void broadcast() {
        for (ServerApp client : connectedClients) {
            client.sendMessageToClient("New client has joined!");
        }
    }
}

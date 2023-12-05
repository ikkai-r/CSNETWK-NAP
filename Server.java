import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private static final int PORT = 8080;
    private static final int MPORT = 5050;
    private static List<String> registeredAliases = new ArrayList<>();
    private static List<ServerApp> clients = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        //Listen to port
        ServerSocket server = new ServerSocket(PORT);
        ServerSocket fServer = new ServerSocket(MPORT);

        int count = 0;
        System.out.println("Server Started...");

        while (true) {
            count++;

            //Accept requests and wait until client connects
            Socket serverClientSocket = server.accept();
            Socket serverMClientSocket = fServer.accept();
            System.out.println("Client " + count);

            ServerApp sa = new ServerApp(serverClientSocket, serverMClientSocket, count);
            clients.add(sa);
            sa.start();
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

    public static List<ServerApp> getServerApps() {
        return clients;
    }

    public static void removeServerApp(ServerApp serverApp) {
        clients.remove(serverApp);
    }

    public static String log() {
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return  "<" + timestamp.format(formatter) + ">";
    }
}

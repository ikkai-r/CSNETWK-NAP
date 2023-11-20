import java.net.*;
import java.util.ArrayList;
import java.util.List;
public class Server {
    private static final int PORT = 8080;
    private static List<String> registeredAliases = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        //Listen to port
        ServerSocket server = new ServerSocket(PORT);

        int count = 0;
        System.out.println("Server Started...");

        while (true) {
            count++;

            //Accept requests and wait until client connects
            Socket serverClientSocket = server.accept();
            System.out.println("Client " + count);

            ServerApp sa = new ServerApp(serverClientSocket, count);
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
}

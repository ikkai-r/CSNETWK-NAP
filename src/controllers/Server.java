package src.controllers;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {

    public Server(JTextArea textArea1, String HOST, int PORT) {
        this.textArea1 = textArea1;
        this.HOST = HOST;
        this.PORT = PORT;
    }
    private JTextArea textArea1;
    private String HOST;
    private int PORT;
    private static final int BACKLOG = 100;
    private static final int MPORT = 5050;
    private static List<String> registeredAliases = new ArrayList<>();
    private static List<ServerApp> clients = new ArrayList<>();

    public void start() throws IOException {
        ServerSocket server = new ServerSocket(PORT, BACKLOG, InetAddress.getByName(HOST));
        ServerSocket fServer = new ServerSocket(MPORT, BACKLOG, InetAddress.getByName(HOST));

        int count = 0;
        textArea1.append("Server Started. Listening to incoming connections on " + HOST + " " + PORT + "...\n");

        while (true) {
            count++;

            //Accept requests and wait until client connects
            Socket serverClientSocket = server.accept();
            Socket serverMClientSocket = fServer.accept();
            textArea1.append("Client " + count + "\n");

            ServerApp sa = new ServerApp(serverClientSocket, serverMClientSocket, count , textArea1);
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

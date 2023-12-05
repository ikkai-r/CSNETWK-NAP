import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientMessage extends Thread{
        private Socket clientMSocket;
        private DataInputStream disMReader;
        private DataOutputStream dosMWriter;
        private boolean isConnected = true;

        ClientMessage(Socket clientMSocket) {
            try {
                    this.clientMSocket = clientMSocket;
                    this.disMReader = new DataInputStream(clientMSocket.getInputStream());
                    this.dosMWriter = new DataOutputStream(clientMSocket.getOutputStream());
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }

        public void run() {
            while (isConnected) {
                try {
                    String message;
                    message = disMReader.readUTF();
                    System.out.println(message);
                } catch (IOException e) {
                    closeEverything();
                }
            }
        }

        private void closeEverything() {
            try {
                disMReader.close();
                dosMWriter.close();
                clientMSocket.close();
                isConnected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

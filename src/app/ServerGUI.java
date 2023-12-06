package src.app;

import src.controllers.Server;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

public class ServerGUI extends JFrame {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField textField1;
    private JTextArea textArea1;

    private String host;
    private int port;

    public ServerGUI() {
        setTitle("Server - File Exchange System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setContentPane(contentPane);

        textArea1.setEditable(false);

        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

    }

    private void onOK() {
        if (host == null || host.isEmpty()) {
            host = textField1.getText();
            textArea1.append("Your host is " + host +"\n");
            textArea1.append("Input your port.\n");
        } else {
            try {
                port = Integer.parseInt(textField1.getText());
                textArea1.append("Your port is " + port+"\n");
                serverStart();
            } catch (NumberFormatException ex) {
                textArea1.append("Invalid port. Input port again.\n");
            } catch (IOException e) {
                textArea1.append("Error: Cannot assign requested address.");
            }
        }
        textField1.setText("");
    }

    private void serverStart() throws IOException {
        textArea1.setText("");
        System.out.println("START");
        textField1.setText("");
        textField1.setEnabled(false);
        buttonOK.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                start1();
                return null;
            }
        };
        worker.execute();
    }

    private void start1() throws IOException{
        Server server = new Server(textArea1, host, port);
        server.start();
    }

    private void start() {
        textArea1.append("Set up the server. \n" + "Input host. \n");
    }

    public static void main(String[] args) throws IOException {
        ServerGUI frame = new ServerGUI();
        frame.setVisible(true);
        frame.start();

    }
}

package src.app;

import src.controllers.Client;

import javax.swing.*;
import java.awt.event.*;

public class ClientGUI extends JFrame {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextField textField1;
    private JTextArea textArea1;
    private Client client;

    public ClientGUI() {
        setTitle("Client - File Exchange System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onOK();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        textArea1.setEditable(false);
    }

    private void onOK() throws Exception {
        String input = textField1.getText();
        client.getInput(input);
        textField1.setText("");
    }

    private void start() {
        try {
            client = new Client(textArea1);
            client.clientStart();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            ClientGUI frame = new ClientGUI();
            frame.setVisible(true);
            frame.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
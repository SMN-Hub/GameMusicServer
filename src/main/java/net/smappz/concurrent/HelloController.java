package net.smappz.concurrent;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import static java.lang.System.out;

public class HelloController {
    public static final int PORT = 12345;

    private ServerSocket server;
    private final List<Client> clients = new ArrayList<>();
    private int totalMessages = 0;
    private final Deque<Integer> pendingMessages = new ConcurrentLinkedDeque<>();

    @FXML
    private Label welcomeText;
    @FXML
    private TextArea infoText;

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            out.printf("InetAddress: %s\n", inetAddress);
        }
        out.printf("\n");
    }

    @FXML
    protected void onConnectButtonClick() {
        welcomeText.setText("Starting");
        new ConnectAsync().start();
        //Enumeration<NetworkInterface> nets = null;
        //try {
        //    nets = NetworkInterface.getNetworkInterfaces();
        //    for (NetworkInterface netint : Collections.list(nets))
        //        displayInterfaceInformation(netint);
        //
        //    java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
        //    out.println(addr.toString());
        //} catch (SocketException e) {
        //    throw new RuntimeException(e);
        //} catch (UnknownHostException e) {
        //    throw new RuntimeException(e);  // TODO handle exception
        //}
    }

    @FXML
    public void onSend1ButtonClick() {
        onMessageReceived(1);
    }

    @FXML
    public void onSend2ButtonClick() {
        onMessageReceived(2);
    }

    @FXML
    public void onSend3ButtonClick() {
        onMessageReceived(2);
    }

    @FXML
    public void onSpamButtonClick() {
        onMessageReceived(1);
        onMessageReceived(1);
        onMessageReceived(1);
        onMessageReceived(2);
        onMessageReceived(2);
        onMessageReceived(3);
        onMessageReceived(3);
        onMessageReceived(2);
        onMessageReceived(1);
        onMessageReceived(3);
    }

    private void updateUI() {
        Platform.runLater(() -> {
            infoText.setText(String.format("%s\nConnected %d clients\nReceived %d messages", server.getInetAddress().getHostName().toString(), this.clients.size(), totalMessages));
        });
    }

    private void displayText(String text) {
        Platform.runLater(() -> this.welcomeText.setText(text));
    }

    private void onMessageReceived(int message) {
        totalMessages++;
        pendingMessages.push(message);
        updateUI();
    }

    private void onProcessMessage(int message) {
        clients.forEach(client -> {
            try {
                client.output.writeInt(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private final class ConnectAsync extends Thread {
        @Override
        public void run() {
            // Start server
            try {
                java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                out.println(addr.toString());
                server = new ServerSocket(PORT, 0, addr);
                displayText("Connected");
                updateUI();
            } catch (IOException e) {
                displayText("Cannot start server\n" + e.getMessage());
                e.printStackTrace();
            }
            // Then launch processing task
            new ProcessAsync().start();
            // Then listen for incoming connections
            try {
                while (!server.isClosed()) {
                    Socket clientSocket = server.accept();
                    Client client = new Client(clientSocket);
                    clients.add(client);
                    updateUI();
                    // start reading
                    new ReadAsync(client).start();
                }
            } catch (IOException e) {
                displayText("Cannot listen server\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private final class ReadAsync extends Thread {
        private final Client client;

        public ReadAsync(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                while (client.socket.isConnected()) {
                    int message = client.input.readInt();
                    onMessageReceived(message);
                }
            } catch (IOException  e) {
                displayText("Cannot read client\n" + e.getMessage());
                e.printStackTrace();
                clients.remove(client);
                updateUI();
            }
        }
    }

    private final class ProcessAsync extends Thread {
        @Override
        public void run() {
            while (server == null || !server.isClosed()) {
                if (!pendingMessages.isEmpty()) {
                    Integer message = pendingMessages.pop();
                    onProcessMessage(message);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return; // exit thread if interrupted
                }
            }
        }
    }
}

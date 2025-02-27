package net.smappz.concurrent;

import java.io.*;
import java.net.Socket;

public class Client {
    Socket socket;
    DataInputStream input;
    DataOutputStream output;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }
}

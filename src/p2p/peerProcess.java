package p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class peerProcess {
    private static final int sPort = 8000;   //The server will be listening on this port number

    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");

        int clientNum = 1;
        try (ServerSocket listener = new ServerSocket(sPort)) {
            while (true) {
                new ClientHandler(listener.accept(), clientNum).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        }

    }
}

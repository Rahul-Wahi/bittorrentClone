package p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class peerProcess {
    private static final int sPort = 8000;   //The server will be listening on this port number
    public static String target = "localhost";
    public static int targetPort = 8001;
    public static boolean terminate = false;
    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        int currentPeerId = Integer.parseInt(args[0]);
        Peer currentPeer = LoadConfig.getCurrentPeer(currentPeerId);

        if (currentPeer == null) {
            System.out.println("Peer with peerof = " + currentPeerId + " not found");
            return;
        }

        List<Peer> peers = LoadConfig.loadPeersInfo();
        CommonConfig commonConfig = LoadConfig.loadCommonConfig();


        for (Peer peer : peers) {
            if (peer.getPeerid() == currentPeerId) {
                break;
            }

            new ServerHandler(currentPeer, peer.hostName, peer.portno).start();
        }


        try (ServerSocket listener = new ServerSocket(currentPeer.portno)) {
            while (true) {
                new ClientHandler(listener.accept(), currentPeer).start();
                System.out.println("Client "  + " is connected!");
                if (terminate) {
                    break;
                }


            }
        }




    }
}

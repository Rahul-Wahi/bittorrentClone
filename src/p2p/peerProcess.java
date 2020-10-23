package p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class peerProcess {
    public static boolean terminate = false;
    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        int currentPeerId = Integer.parseInt(args[0]);
        Peer currentPeer = LoadConfig.getCurrentPeer(currentPeerId);
        Logging.setup(currentPeerId);
        Logger logger = Logging.getLOGGER();

        if (currentPeer == null) {
            logger.log(Level.INFO, "Peer with peer id: [" + currentPeerId + "] not found");
            return;
        }

        List<Peer> peers = LoadConfig.loadPeersInfo();
        CommonConfig commonConfig = LoadConfig.loadCommonConfig();


        for (Peer peer : peers) {
            if (peer.getPeerid() == currentPeerId) {
                break;
            }

            new ServerHandler(currentPeer, peer.getHostName(), peer.getPortno()).start();
        }


        try (ServerSocket listener = new ServerSocket(currentPeer.getPortno())) {
            while (true) {
                new ClientHandler(listener.accept(), currentPeer).start();
                System.out.println("Client "  + " is connected!");
                logger.log(Level.INFO, "Client is connected");
                if (terminate) {
                    break;
                }


            }
        }

    }
}

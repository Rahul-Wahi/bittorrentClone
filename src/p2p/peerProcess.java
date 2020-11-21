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
    private static Peer currentPeer;

    public static void setTerminate () {
        terminate = true;
    }

    public static boolean getTerminate () {
        return terminate;
    }

    public static Peer getCurrentPeer() {
        return currentPeer;
    }

    public static void setCurrentPeer(Peer currentPeer) {
        peerProcess.currentPeer = currentPeer;
    }


    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        CommonConfig commonConfig = LoadConfig.loadCommonConfig();
        List<Peer> peers = LoadConfig.loadPeersInfo();
        int currentPeerId = Integer.parseInt(args[0]);
        Peer currentPeer = LoadConfig.getCurrentPeer(currentPeerId);
        peerProcess.setCurrentPeer(currentPeer);
        Logging.setup(currentPeerId);
        Logger logger = Logging.getLOGGER();

        if (currentPeer == null) {
            logger.log(Level.INFO, "Peer with peer id: [" + currentPeerId + "] not found");
            return;
        }

        for (Peer peer : peers) {
            if (peer.getPeerid() == currentPeerId) {
                break;
            }

            new ServerHandler(currentPeer, peer.getHostName(), peer.getPortno()).start();
            logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() +"] makes a connection to " +
                    "Peer [" + peer.getPeerid() + "].");
        }


        try (ServerSocket listener = new ServerSocket(currentPeer.getPortno())) {
            while (true) {
                new ClientHandler(listener.accept(), currentPeer).start();
                logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() +"] is connected from");
                if (terminate) {
                    break;
                }
            }
        }

    }
}

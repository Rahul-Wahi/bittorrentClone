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
    private static Integer noOfPeerWithFile = 0;
    private static Integer totalNumOfPeers = 0;
    static ServerSocket listener;
    public static void setTotalNumOfPeers(Integer totalNumOfPeers) {
        peerProcess.totalNumOfPeers = totalNumOfPeers;
    }

    public static void setTerminate () {
        terminate = true;
    }

    public static boolean shouldTerminate () throws IOException {
        boolean result = noOfPeerWithFile == totalNumOfPeers;

        if (result) {
            currentPeer.cleanup();
        }

        return result;
    }

    synchronized public static void incrementNoOfPeerWithFile() {
        noOfPeerWithFile++;
        if (noOfPeerWithFile == totalNumOfPeers) {
            try {
                currentPeer.cleanup();
            } catch (Exception e) {

            }

        }

        System.out.println("No of peer with file " + noOfPeerWithFile);
    }

    public static Integer getNoOfPeerWithFile() {
        return noOfPeerWithFile;
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
        List<PeerInfo> peers = LoadConfig.loadPeersInfo();
        peerProcess.setTotalNumOfPeers(peers.size());
        int currentPeerId = Integer.parseInt(args[0]);
        PeerInfo currentPeerInfo = LoadConfig.getCurrentPeer(currentPeerId);
        int numOfConnectionToAccept = peers.size() - 1;
        Logging.setup(currentPeerId);
        Logger logger = Logging.getLOGGER();

        if (currentPeerInfo == null) {
            logger.log(Level.INFO, "Peer with peer id: [" + currentPeerId + "] not found");
            return;
        }

        Peer currentPeer = new Peer(currentPeerInfo);
        peerProcess.setCurrentPeer(currentPeer);

        for (PeerInfo peer : peers) {
            if (peer.getPeerid() == currentPeerId) {
                break;
            }
            numOfConnectionToAccept--;
            new ServerHandler(currentPeer, peer.getHostName(), peer.getPortno()).start();
            logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() +"] makes a connection to " +
                    "Peer [" + peer.getPeerid() + "].");
        }


        currentPeer.selectPreferredNeighbors();
        currentPeer.selectOptimisticUnchokedNeighbor();
        try (ServerSocket listener = new ServerSocket(currentPeer.getPortno())) {
            while (numOfConnectionToAccept-- > 0) {
                System.out.println(numOfConnectionToAccept);
                new ClientHandler(listener.accept(), currentPeer).start();
                logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() +"] is connected from");
            }
        }

        System.out.println("jaja");
    }
}

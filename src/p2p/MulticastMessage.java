package p2p;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MulticastMessage extends Thread {
    private byte[] message;
    Set<Integer> nodes;

    //for all the connections
    public MulticastMessage(byte[] message) {
        this.message = message;
        nodes = peerProcess.getCurrentPeer().getConnections().keySet();
    }

    //only to provided nodes
    public MulticastMessage(byte[] message, Set<Integer> nodes) {
        this.message = message;
        this.nodes = nodes;
    }

    public void run() {
        Map<Integer, PeerHandler> connections = peerProcess.getCurrentPeer().getConnections();
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfCores);
        for (Integer peerid : nodes) {
            executor.submit(() -> connections.get(peerid).sendMessage(message));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

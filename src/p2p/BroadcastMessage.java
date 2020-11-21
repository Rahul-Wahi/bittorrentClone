package p2p;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BroadcastMessage extends Thread {
    private byte[] message;

    public BroadcastMessage(byte[] message) {
        this.message = message;
    }

    public void run() {
        Map<Integer, PeerHandler> connections = peerProcess.getCurrentPeer().getConnections();
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfCores);
        for (Integer peeid : connections.keySet()) {
            executor.submit(() -> connections.get(peeid).sendMessage(message));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

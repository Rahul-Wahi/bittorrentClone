package p2p;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Peer {
    private int peerid;
    private String hostName;
    private int portno;
    private boolean hasFile;
    private Set<Integer> neighbours;
    private Integer optimisticNeighbour;
    private int numberOfPieces;
    private final BitField bitField;
    private Map<Integer, BitField> peersBitField;
    private Set<Integer> interestedPeers;
    private Set<Integer> neededPieces;
    private final FileHandler fileHandler;
    private final Map<Integer, PeerHandler> connections;
    private final ScheduledThreadPoolExecutor prefferedNeighbourScheduler;
    private final ScheduledThreadPoolExecutor optimisticNeighbourScheduler;
    CommonConfig commonConfig = CommonConfig.getInstance();

    public Map<Integer, PeerHandler> getConnections() {
        return connections;
    }

    synchronized public void addConnection(int remotePeerid, PeerHandler peerHandler) {
        this.connections.put(remotePeerid, peerHandler);
    }

    public PeerHandler getConnection(int remotePeerid) {
        return connections.get(remotePeerid);
    }

    public Peer (PeerInfo peerInfo) throws IOException {
        this.peerid = peerInfo.getPeerid();
        this.hostName = peerInfo.getHostName();
        this.portno = peerInfo.getPortno();
        this.hasFile = peerInfo.hasFile();
        this.connections = new HashMap<>();
        this.interestedPeers = new HashSet<>();
        this.peersBitField = new HashMap<>();
        this.neighbours = new HashSet<>();
        bitField = new BitField(hasFile);
        peersBitField = new HashMap<>();
        fileHandler = new FileHandler(this);
        prefferedNeighbourScheduler = new ScheduledThreadPoolExecutor(1);
        optimisticNeighbourScheduler = new ScheduledThreadPoolExecutor(1);
    }

    public void addBitField(int peerid, BitField bitField) {
        peersBitField.put(peerid, bitField);
    }

    public Map<Integer, BitField> getPeersBitField() {
        return peersBitField;
    }

    public BitField getPeerBitField(int remotePeerid) {
        return peersBitField.get(remotePeerid);
    }

    synchronized public void addPeersBitField(int remotePeerid, BitField peerBitField) {
         peersBitField.put(remotePeerid, peerBitField);
    }

    synchronized public void addInterestedPeers(int remotePeerid) {
        interestedPeers.add(remotePeerid);
    }

    synchronized public void removeInterestedPeers(int remotePeerid) {
        interestedPeers.remove(remotePeerid);
    }

    public int getPeerid() {
        return peerid;
    }

    public void setPeerid(int peerid) {
        this.peerid = peerid;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPortno() {
        return portno;
    }

    public void setPortno(int portno) {
        this.portno = portno;
    }

    public boolean hasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public Set<Integer> getNeighbours() {
        return neighbours;
    }

    synchronized public void setNeighbours(Set<Integer> neighbours) {
        this.neighbours = neighbours;
    }

    public void selectPreferredNeighbors() {
        prefferedNeighbourScheduler.scheduleAtFixedRate(() -> {
            List<Integer> neighboursId = new ArrayList<>(connections.keySet());
            Set<Integer> prevNeighbour =  this.neighbours;
            List<Integer> newNeighbour;
            if (hasFile) {
                newNeighbour = randomKElements(neighboursId, commonConfig.getNumberOfPreferredNeighbors());
            } else {
                neighboursId.sort((Integer p1, Integer p2) -> {
                    return -1;
                });
                newNeighbour = neighboursId.subList(0, commonConfig.getNumberOfPreferredNeighbors());
            }


            this.setNeighbours(new HashSet<>(newNeighbour));
            new MulticastMessage(Message.unchokeMessage(), this.neighbours
                    .stream()
                    .filter(peerid -> !prevNeighbour.contains(peerid))
                    .collect(Collectors.toSet())).start();
        }, 1, commonConfig.getUnchokingInterval(), TimeUnit.SECONDS);

    }

    public void selectOptimisticUnchokedNeighbor() {

        if (connections.size() == commonConfig.getNumberOfPreferredNeighbors()) {
            return;
        }

        optimisticNeighbourScheduler.scheduleAtFixedRate(() -> {
            List<Integer> neighboursList = new ArrayList<>(this.connections.keySet());
            int index = ThreadLocalRandom.current().nextInt(0, connections.size());

            while (this.neighbours.contains(neighboursList.get(index))) {
                index = ThreadLocalRandom.current().nextInt(0, connections.size());
            }

            this.optimisticNeighbour = neighboursList.get(index);
            Set<Integer> optimisticNeighbourSet = new HashSet<>();
            optimisticNeighbourSet.add(optimisticNeighbour);
            new MulticastMessage(Message.unchokeMessage(), optimisticNeighbourSet).start();
        }, 1, commonConfig.getOptimisticUnchokingInterval(), TimeUnit.SECONDS);
    }

    public BitField getBitField() {
        return bitField;
    }

    public FileHandler getFileHandler() { return fileHandler; }

    private List<Integer> randomKElements(List<Integer> list, int K) {
        // create a temporary list for storing
        // selected element
        List<Integer> newList = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            // take a random index between 0 to size
            // of given List
            int randomIndex = ThreadLocalRandom.current().nextInt(list.size());

            // add element in temporary list
            newList.add(list.get(randomIndex));

            // Remove selected element from orginal list
            list.remove(randomIndex);
        }
        return newList;
    }
}

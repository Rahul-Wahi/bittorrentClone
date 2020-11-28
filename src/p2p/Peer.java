package p2p;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private Set<Integer> interestedPeers; // peers which are interested in this peer data
    private final Set<Integer> interestingPeers; // peers in which this peer is interested
    private final List<Integer> neededPieces;
    private Set<Integer> requestedPieces;
    private final FileHandler fileHandler;
    private final Map<Integer, PeerHandler> connections;
    private final ScheduledThreadPoolExecutor prefferedNeighbourScheduler;
    private final ScheduledThreadPoolExecutor optimisticNeighbourScheduler;
    private final ReentrantLock reentrantLock;
    CommonConfig commonConfig = CommonConfig.getInstance();
    Logger logger = Logging.getLOGGER();

    public Map<Integer, PeerHandler> getConnections() {
        return connections;
    }

    synchronized public void addConnection(int remotePeerid, PeerHandler peerHandler) {
        this.connections.put(remotePeerid, peerHandler);
    }

    public PeerHandler getConnection(int remotePeerid) {
        return connections.get(remotePeerid);
    }

    public Peer(PeerInfo peerInfo) throws IOException {
        this.peerid = peerInfo.getPeerid();
        this.hostName = peerInfo.getHostName();
        this.portno = peerInfo.getPortno();
        this.hasFile = peerInfo.hasFile();
        this.connections = new HashMap<>();
        this.interestedPeers = new HashSet<>();
        this.peersBitField = new HashMap<>();
        this.neighbours = new HashSet<>();
        this.interestedPeers = new HashSet<>();
        this.interestingPeers = new HashSet<>();
        this.requestedPieces = new HashSet<>();
        this.neededPieces = new ArrayList<>();
        bitField = new BitField(hasFile);
        peersBitField = new HashMap<>();
        fileHandler = new FileHandler(this);
        prefferedNeighbourScheduler = new ScheduledThreadPoolExecutor(1);
        optimisticNeighbourScheduler = new ScheduledThreadPoolExecutor(1);
        reentrantLock = new ReentrantLock();
        setNeededPieces();
    }

    public void addNeededPiece(int pieceIndex) {
        reentrantLock.lock();
        try {
            this.neededPieces.add(pieceIndex);
        } finally {
            reentrantLock.unlock();
        }
    }

    public void setNeededPieces() {
        String bitFieldString = bitField.getBitFieldString();
        reentrantLock.lock();
        try {
            for (int i = 0; i < bitFieldString.length(); i++) {
                if (bitFieldString.charAt(i) == '0') {
                    this.neededPieces.add(i);
                }
            }
        } finally {
            reentrantLock.unlock();
        }

    }
    public void removeNeededPiece(Integer pieceIndex) {
        reentrantLock.lock();
        try {
            this.neededPieces.remove(pieceIndex);
            this.requestedPieces.remove(pieceIndex);
        } finally {
            reentrantLock.unlock();
        }
    }

    public Integer getNeededPiece(int index) {
        return this.neededPieces.get(index);
    }

    public List<Integer> getNeededPieces() {
        return this.neededPieces;
    }

    public void setRequestedPieces(Set<Integer> requestedPieces) {
        reentrantLock.lock();
        try {
            this.requestedPieces = requestedPieces;
        } finally {
            reentrantLock.unlock();
        }
    }

    public void clearRequestedPieces() {
        reentrantLock.lock();
        try {
            this.requestedPieces.clear();
        } finally {
            reentrantLock.unlock();
        }
    }

    public Integer selectPiece(int remotePeerid) {
        reentrantLock.lock();
        Integer randomIndex = null;
        try {
            if (neededPieces.size() == 0 || (neededPieces.size() == requestedPieces.size())) {
                return randomIndex;
            }

            randomIndex = ThreadLocalRandom.current().nextInt(this.neededPieces.size());
            BitField remoteBitField = this.getPeerBitField(remotePeerid);
            Set<Integer> remotePeerPieces = new HashSet<>(remoteBitField.getHavePieces());
            remotePeerPieces.removeAll(requestedPieces);

            if (remotePeerPieces.size() == 0) {
                return randomIndex;
            }

            while (requestedPieces.contains(neededPieces.get(randomIndex)) ||
                    !remotePeerPieces.contains(neededPieces.get(randomIndex)) ) {
                randomIndex = ThreadLocalRandom.current().nextInt(this.neededPieces.size());
            }

            requestedPieces.add(neededPieces.get(randomIndex));
        } finally {
            reentrantLock.unlock();
        }

        return neededPieces.get(randomIndex);
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

    public Set<Integer> getInterestingPeers() { return interestingPeers; }

    synchronized public void addInterestingPeer(Integer remotePeerid) { this.interestingPeers.add(remotePeerid); }

    synchronized public void removeInterestingPeer(Integer remotePeerid) {
        this.interestingPeers.remove(remotePeerid);

        if (this.interestingPeers.size() == 0) {
            this.setHasFile(true);
        }
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
            logger.log(Level.INFO, "Select Pref Neighbours");
            List<Integer> neighboursId = new ArrayList<>(interestedPeers);
            Set<Integer> prevNeighbour = new HashSet<>(this.neighbours);
            List<Integer> newNeighbour;

            if (hasFile) {
                newNeighbour = randomKElements(neighboursId, commonConfig.getNumberOfPreferredNeighbors());
            } else {
                neighboursId.sort((Integer p1, Integer p2) -> {
                    //use download speed function here for sort
                    return -1;
                });
                newNeighbour = neighboursId.subList(0, Math.min(commonConfig.getNumberOfPreferredNeighbors(), neighboursId.size()));
            }
            this.setNeighbours(new HashSet<>(newNeighbour));
            List<String> intString = new ArrayList<>();
            for (Integer i : this.neighbours) {
                intString.add(String.valueOf(i));
            }

            String result = String.join(",", intString);

            logger.log(Level.INFO, result);
            //unchoke neighbours if not already
            new MulticastMessage(Message.unchokeMessage(), this.neighbours
                    .stream()
                    .filter(peerid -> !(prevNeighbour.contains(peerid)))
                    .collect(Collectors.toSet())).start();

            //choke old neighbours if not selected again
            new MulticastMessage(Message.chokeMessage(), prevNeighbour
                    .stream()
                    .filter(peerid -> !(this.neighbours.contains(peerid)) && !peerid.equals(this.optimisticNeighbour))
                    .collect(Collectors.toSet())).start();
        }, 1, commonConfig.getUnchokingInterval(), TimeUnit.SECONDS);


    }

    public void selectOptimisticUnchokedNeighbor() {

        optimisticNeighbourScheduler.scheduleAtFixedRate(() -> {
            logger.log(Level.INFO, "Select Optimistic Neighbours");
            if (neighbours.size() <= commonConfig.getNumberOfPreferredNeighbors()) {
                return;
            }
            List<Integer> neighboursList = new ArrayList<>(interestedPeers);
            int index = ThreadLocalRandom.current().nextInt(0, neighboursList.size());

            while (this.neighbours.contains(neighboursList.get(index))) {
                index = ThreadLocalRandom.current().nextInt(0, neighboursList.size());
            }
            Integer prevOptimisticNeighbour = this.optimisticNeighbour;
            this.optimisticNeighbour = neighboursList.get(index);
            Set<Integer> optimisticNeighbourSet = new HashSet<>();
            optimisticNeighbourSet.add(optimisticNeighbour);
            Set<Integer> prevOptimisticNeighbourSet = new HashSet<>();
            prevOptimisticNeighbourSet.add(prevOptimisticNeighbour);
            new MulticastMessage(Message.unchokeMessage(), optimisticNeighbourSet.stream()
                    .filter(peerid -> !(peerid.equals(prevOptimisticNeighbour)))
                    .collect(Collectors.toSet())).start();

            //choke old neighbours if not selected again
            new MulticastMessage(Message.chokeMessage(), prevOptimisticNeighbourSet
                    .stream()
                    .filter(peerid -> !(this.neighbours.contains(peerid)) && !peerid.equals(this.optimisticNeighbour))
                    .collect(Collectors.toSet())).start();
        }, 1, commonConfig.getOptimisticUnchokingInterval(), TimeUnit.SECONDS);
    }

    public BitField getBitField() {
        return bitField;
    }

    public FileHandler getFileHandler() {
        return fileHandler;
    }

    public void cleanup() throws IOException {
        logger.log(Level.INFO, "cleanup");
        prefferedNeighbourScheduler.shutdownNow();
        optimisticNeighbourScheduler.shutdownNow();
        //fileHandler.clean();

    }

    private List<Integer> randomKElements(List<Integer> list, int K) {
        // create a temporary list for storing
        // selected element
        List<Integer> newList = new ArrayList<>();
        for (int i = 0; i < K && i < list.size(); i++) {
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

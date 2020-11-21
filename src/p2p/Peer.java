package p2p;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Peer {
    private int peerid;
    private String hostName;
    private int portno;
    private boolean hasFile;
    private Set<Integer> neighbours;
    private int numberOfPieces;
    private BitField bitField;
    private Map<Integer, BitField> peersBitField;
    private Set<Integer> interestedPeers;
    private Set<Integer> neededPieces;
    private FileHandler fileHandler;
    private Map<Integer, PeerHandler> connections;

    public Map<Integer, PeerHandler> getConnections() {
        return connections;
    }

    synchronized public void addConnection(int remotePeerid, PeerHandler peerHandler) {
        this.connections.put(remotePeerid, peerHandler);
    }

    public PeerHandler getConnection(int remotePeerid) {
        return connections.get(remotePeerid);
    }

    public Peer (int peerid, String hostName, int portno, boolean hasFile) throws IOException {
        this.peerid = peerid;
        this.hostName = hostName;
        this.portno = portno;
        this.hasFile = hasFile;
        bitField = new BitField(hasFile);
        peersBitField = new HashMap<>();
        fileHandler = new FileHandler(this);
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

    public void setNeighbours(Set<Integer> neighbours) {
        this.neighbours = neighbours;
    }

    public void selectPreferredNeighbors() {

    }

    public void selectOptimisticUnchokedNeighbor() {

    }

    public BitField getBitField() {
        return bitField;
    }

    public FileHandler getFileHandler() { return fileHandler; }
}

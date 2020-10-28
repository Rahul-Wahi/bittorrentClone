package p2p;

import java.util.Map;
import java.util.Set;

public class Peer {
    private int peerID;
    private String hostName;
    private int portNo;
    private boolean hasFile;
    private Set<Integer> neighbours;

    public Peer (int peerID, String hostName, int portNo, boolean hasFile) {
        this.peerID = peerID;
        this.hostName = hostName;
        this.portNo = portNo;
        this.hasFile = hasFile;
    }

    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID) {
        this.peerID = peerID;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPortNo() {
        return portNo;
    }

    public void setPortNo(int portNo) {
        this.portNo = portNo;
    }

    public boolean isHasFile() {
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
}

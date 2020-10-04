package p2p;

import java.util.Map;
import java.util.Set;

public abstract class Peer {
    int peerid;
    String hostName;
    int portno;
    boolean hasFile;
    Set<Integer> neighbours;

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

    public abstract void selectPreferredNeighbors();
    public abstract void selectOptimisticUnchokedNeighbor();
}

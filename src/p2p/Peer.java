package p2p;

import java.util.Map;
import java.util.Set;

public class Peer {
    private int peerid;
    private String hostName;
    private int portno;
    private boolean hasFile;
    private Set<Integer> neighbours;

    public Peer (int peerid, String hostName, int portno, boolean hasFile) {
        this.peerid = peerid;
        this.hostName = hostName;
        this.portno = portno;
        this.hasFile = hasFile;
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

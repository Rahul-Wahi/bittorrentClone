package p2p;

import java.io.IOException;

public interface PeerHandler {

    void sendMessage(byte[] msg);

    void setIsCurrentPeerChoked (boolean isCurrentPeerChoked);

    boolean isCurrentPeerChoked();
}

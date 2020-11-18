package p2p;

import java.io.IOException;

public interface PeerHandler {

    //send a message to the output stream
    void sendMessage(byte[] msg);
}

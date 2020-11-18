package p2p;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageHandler extends Thread {
    private MessageType messageType;
    private byte[] messagePayload;
    private PeerHandler peerHandler;
    private Peer currentPeer;
    private int remotePeerid;
    Logger logger = Logging.getLOGGER();

    public MessageHandler(PeerHandler peerHandler, Peer currentPeer, int remotePeerid, String messageType, byte[] messagePayload) {
        this.peerHandler = peerHandler;
        this.currentPeer = currentPeer;
        this.messageType = MessageType.values()[Integer.parseInt(messageType)];
        this.messagePayload = messagePayload;
        this.remotePeerid = remotePeerid;
    }

    public void run () {
        switch (messageType) {
            case BITFIELD:
                BitField receivedBitField = new BitField(ByteConversionUtil.bytesToString(messagePayload));
                BitField currentBitField = currentPeer.getBitField();
                currentPeer.addPeersBitField(remotePeerid, receivedBitField);

                if (currentBitField.containsInterestedPieces(receivedBitField.getBitFieldString())) {
                    peerHandler.sendMessage(Message.message(MessageType.INTERESTED));
                } else {
                    peerHandler.sendMessage(Message.message(MessageType.NOTINTRESTED));
                }
                break;
            case HAVE:
                logger.log(Level.FINE, "Received 'Have' Message from [" + remotePeerid + "]");
                int pieceIndex = ByteConversionUtil.bytesToInt(messagePayload);
                BitField remoteBitField = currentPeer.getPeerBitField(remotePeerid);
                remoteBitField.setBit(pieceIndex);
                BitField bitField = currentPeer.getBitField();
                if (bitField.containsInterestedPieces(remoteBitField.getBitFieldString())) {
                    peerHandler.sendMessage(Message.message(MessageType.INTERESTED));
                } else {
                    peerHandler.sendMessage(Message.message(MessageType.NOTINTRESTED));
                }
                break;
            case CHOKE:
                logger.log(Level.FINE, "Received 'Choke' Message from [" + remotePeerid + "]");
                break;
            case UNCHOKE:
                logger.log(Level.FINE, "Received 'Unchoke' Message from [" + remotePeerid + "]");
                break;
            case PIECE:
                logger.log(Level.FINE, "Received 'Piece' Message from [" + remotePeerid + "]");
                break;
            case REQUEST:
                logger.log(Level.FINE, "Received 'Request' Message from [" + remotePeerid + "]");
                break;
            case INTERESTED:
                logger.log(Level.FINE, "Received Interested Message from [" + remotePeerid + "]");
                currentPeer.addInterestedPeers(remotePeerid);
                break;
            case NOTINTRESTED:
                logger.log(Level.FINE, "Received Not Interested Message from [" + remotePeerid + "]");
                currentPeer.removeInterestedPeers(remotePeerid);
                break;
        }
    }
}

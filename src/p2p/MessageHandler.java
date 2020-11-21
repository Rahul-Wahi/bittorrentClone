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

    public void run() {
        switch (messageType) {
            case BITFIELD:
                logger.log(Level.INFO, "Received 'Bitfield' Message from [" + remotePeerid + "]");
                //add received bit field to current peer's remotePeer bitfield map
                BitField receivedBitField = new BitField(ByteConversionUtil.bytesToString(messagePayload));
                currentPeer.addPeersBitField(remotePeerid, receivedBitField);
                evaluateRemoteBitField(receivedBitField);
                break;

            case HAVE: {
                logger.log(Level.INFO, "Received 'Have' Message from [" + remotePeerid + "]");
                //update remote peer bitfield
                int pieceIndex = ByteConversionUtil.bytesToInt(messagePayload);
                BitField remoteBitField = currentPeer.getPeerBitField(remotePeerid);
                remoteBitField.setBit(pieceIndex);
                evaluateRemoteBitField(remoteBitField);
                break;
            }

            case CHOKE:
                logger.log(Level.INFO, "Received 'Choke' Message from [" + remotePeerid + "]");
                peerHandler.setIsCurrentPeerChoked(true);
                break;

            case UNCHOKE:
                logger.log(Level.INFO, "Received 'Unchoke' Message from [" + remotePeerid + "]");
                peerHandler.setIsCurrentPeerChoked(false);

                //send request message for piece if any else send not interested
                break;

            case PIECE: {
                logger.log(Level.INFO, "Received 'Piece' Message from [" + remotePeerid + "]");
                // process piece and store
                int pieceIndex = Piece.getPieceIndex(messagePayload);
                Piece.store(Piece.getPieceContent(messagePayload), pieceIndex);
                new MulticastMessage(Message.haveMessage(pieceIndex)).run();
                //send request message for piece if any or not choked (if no piece send not interested)
                break;
            }

            case REQUEST:
                logger.log(Level.INFO, "Received 'Request' Message from [" + remotePeerid + "]");
                //send piece
                break;

            case INTERESTED:
                logger.log(Level.INFO, "Received Interested Message from [" + remotePeerid + "]");
                currentPeer.addInterestedPeers(remotePeerid);
                break;

            case NOTINTRESTED:
                logger.log(Level.INFO, "Received Not Interested Message from [" + remotePeerid + "]");
                currentPeer.removeInterestedPeers(remotePeerid);
                break;

            default:
                logger.log(Level.INFO, "Received 'Unknown' Message from [" + remotePeerid + "]");
                break;
        }
    }

    private void evaluateRemoteBitField(BitField remoteBitField) {
        BitField bitField = currentPeer.getBitField();

        //respond interested/not-interested message
        if (bitField.containsInterestedPieces(remoteBitField.getBitFieldString())) {
            peerHandler.sendMessage(Message.message(MessageType.INTERESTED));
        } else {
            peerHandler.sendMessage(Message.message(MessageType.NOTINTRESTED));
        }
    }
}

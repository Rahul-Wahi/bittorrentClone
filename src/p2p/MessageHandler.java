package p2p;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MessageHandler extends Thread {
    private MessageType messageType;
    private byte[] messagePayload;
    private PeerHandler peerHandler;
    private Peer currentPeer;
    private int remotePeerid;
    Logger logger = Logging.getLOGGER();
    CommonConfig commonConfig = CommonConfig.getInstance();

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
                if (receivedBitField.areAllBitsSet()) {
                    peerProcess.addPeerWithFile(remotePeerid);
                }

                currentPeer.addPeersBitField(remotePeerid, receivedBitField);
                evaluateRemoteBitField(receivedBitField);
                break;

            case HAVE: {
                logger.log(Level.INFO, "Received 'Have' Message from [" + remotePeerid + "]");
                //update remote peer bitfield
                int pieceIndex = ByteConversionUtil.bytesToInt(messagePayload);
                BitField remoteBitField = currentPeer.getPeerBitField(remotePeerid);
                remoteBitField.setBit(pieceIndex);
                logger.log(Level.INFO, "No of piece with peer [" +  remotePeerid + "] = " + remoteBitField.getNumOfSetBit());
                if (remoteBitField.getNumOfSetBit() == commonConfig.getNumOfPieces()) {
                    peerProcess.addPeerWithFile(remotePeerid);
                    peerProcess.incrementNoOfPeerWithFile();
                }
                //evaluateRemoteBitField(remoteBitField);//change and check just one bit
                evaluateRemoteBitField(pieceIndex);
                break;
            }

            case CHOKE:
                logger.log(Level.INFO, "Received 'Choke' Message from [" + remotePeerid + "]");
                peerHandler.setIsCurrentPeerChoked(true);
                currentPeer.removeRequestedPiece(peerHandler.getRequestedPieceIndex());
                break;

            case UNCHOKE:
                logger.log(Level.INFO, "Received 'Unchoke' Message from [" + remotePeerid + "]");
                peerHandler.setIsCurrentPeerChoked(false);
                //send request message for piece if any else send not interested
                peerHandler.sendRequestMessage();
                break;

            case PIECE: {
                logger.log(Level.INFO, "Received 'Piece' Message from [" + remotePeerid + "]");
                logger.log(Level.FINE, "Piece " + messagePayload.length);
                // process piece and store
                int pieceIndex = Piece.getPieceIndex(messagePayload);
                Piece.store(Piece.getPieceContent(messagePayload), pieceIndex);
                logger.log(Level.INFO, " Total Pieces " + currentPeer.getBitField().getNumOfSetBit());
                //evaluate bit field of interesting neighbours[pending]
                Set<Integer> interestingPeers = currentPeer.getInterestingPeers();
                logger.log(Level.INFO, "interesting Peer " + interestingPeers);
                logger.log(Level.INFO, "not interesting Peer " + interestingPeers.stream()
                        .filter(peerid -> !currentPeer.getBitField().containsInterestedPieces
                                (currentPeer.getPeerBitField(peerid).getBitFieldString()))
                        .collect(Collectors.toSet()));

                Set<Integer> nonInterestingPeers = interestingPeers.stream()
                        .filter(peerid -> !currentPeer.getBitField().containsInterestedPieces
                                (currentPeer.getPeerBitField(peerid).getBitFieldString()))
                        .collect(Collectors.toSet());

                currentPeer.removeInterestingPeers(nonInterestingPeers);


                new MulticastMessage(MessageType.NOTINTRESTED, nonInterestingPeers).start();

                MulticastMessage multicastMessage = new MulticastMessage(Message.haveMessage(pieceIndex));
                multicastMessage.start();
                //send request message for piece if any or not choked (if no piece send not interested) [pending]
                peerHandler.sendRequestMessage();



                if (currentPeer.getNeededPieces().size() == 0) {
                    logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() + "] has downloaded the complete file.");
                    while (!multicastMessage.hasCompleted) {
                        System.out.println("hahahaha");
                    }

                    peerProcess.incrementNoOfPeerWithFile();
                    peerProcess.addPeerWithFile(currentPeer.getPeerid());
                    currentPeer.setHasFile(true);
                }
                break;
            }

            case REQUEST:
                logger.log(Level.INFO, "Received 'Request' Message from [" + remotePeerid + "]");
                //send piece
                int requestedPieceIndex = ByteConversionUtil.bytesToInt(messagePayload);
                byte[] piece = Message.pieceMessage(requestedPieceIndex);
                logger.log(Level.INFO, "Request: index: " + requestedPieceIndex + " pieceLen " + piece.length);
                peerHandler.sendMessage(piece);
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
            peerHandler.sendInterestedMessage();
        } else {
            peerHandler.sendNotInterestedMessage();
        }
    }

    private void evaluateRemoteBitField(int bitIndex) {
        BitField bitField = currentPeer.getBitField();
        //respond interested/not-interested message
        if (bitField.getBitFieldString().charAt(bitIndex) == '0' && !currentPeer.getInterestingPeers().contains(remotePeerid)) {
            peerHandler.sendInterestedMessage();
        }
    }
}

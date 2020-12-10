package p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.DataInputStream;

/**
 * A handler thread class.  Handlers are spawned from the listening
 * loop and are responsible for dealing with a single client's requests.
 */
    class ClientHandler extends Thread implements PeerHandler {
    private final Socket connection;
    private ObjectInputStream in;    //stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private final Peer currentPeer;
    Integer remotePeerid;
    Logger logger = Logging.getLOGGER();
    CommonConfig commonConfig = CommonConfig.getInstance();
    private boolean currentPeerChoked;
    Integer requestedPieceIndex;
    int totalByteSent;
    int totalByteReceived;

    public ClientHandler(Socket connection, Peer currentPeer) {
        this.connection = connection;
        this.currentPeer = currentPeer;
    }

    public void run() {
        try {
            Message message = new Message();
            //initialize Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());

            byte[] receivedHandshakeByte = new byte[32];
            readMessage(receivedHandshakeByte);
            int remotePeerid = message.verifyHandshakeMessage(receivedHandshakeByte);
            this.remotePeerid = remotePeerid;
            logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() +"] is connected from ["
                    + remotePeerid + "]");
            logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() + "] received handhsake message : "
                    + ByteConversionUtil.bytesToString(receivedHandshakeByte));


            currentPeer.addConnection(remotePeerid, this);
            //reply with handshake message
            sendMessage(message.handshakeMessage(currentPeer.getPeerid()));

            BitField bitField = currentPeer.getBitField();


            sendMessage(Message.message(MessageType.BITFIELD, bitField.getBitFieldString()));

            while (!peerProcess.shouldTerminate()) {
                byte[] messageLengthByte = new byte[4];
                byte[] messageType = new byte[1];
                int byteRead = readMessage(messageLengthByte);

                if (byteRead == -1) {
                    //currentPeer.cleanup();
                    break;
                }

                byteRead = readMessage(messageType);

                if (byteRead == -1) {
                    //currentPeer.cleanup();
                    break;
                }

                int messageLength = ByteConversionUtil.bytesToInt(messageLengthByte);
                byte[] messagePayload = new byte[messageLength - messageType.length];
                byteRead = readMessage(messagePayload);

                if (byteRead == -1) {
                    //currentPeer.cleanup();
                    break;
                }

                //logger.log(Level.FINE, " Total Received Bytes so far : " + totalByteReceived);
                new MessageHandler(this, currentPeer, remotePeerid, ByteConversionUtil.bytesToString(messageType), messagePayload).start();
            }


        } catch (IOException ioException) {
            System.out.println("Disconnect with Client ");
        } finally {
            //Close connections
            try {
                in.close();
                out.close();
                connection.close();
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client ");
            }
        }
    }

    //send a message to the output stream
    public void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("Send message: " + msg + " to Client ");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public int readMessage(byte[] msg) {
        int bytesRead = 0;
        try {
            while (bytesRead != msg.length && bytesRead != -1) {
                bytesRead += in.read(msg, bytesRead, msg.length - bytesRead);
            }

            totalByteReceived += bytesRead;
            //logger.log(Level.INFO, "read msg " + msg.length + " Total Received Bytes " + totalByteReceived);
            return bytesRead;
        } catch (IOException ioException) {
            //ioException.printStackTrace();
        }

        if (bytesRead == 0) {
            bytesRead = -1;
        }
        return bytesRead;
    }
        @Override
    //send a message to the output stream
    synchronized public void sendMessage(byte[] msg) {
        try {
            totalByteSent += msg.length;
            //logger.log(Level.INFO, "send msg " + msg.length + " Total Sent Bytes " + totalByteSent);
            out.write(msg);
            out.flush();
        } catch (IOException ioException) {
            //ioException.printStackTrace();
        }
    }

    @Override
    public synchronized void sendInterestedMessage() {
        sendMessage(Message.message(MessageType.INTERESTED));
        currentPeer.addInterestingPeer(remotePeerid);
    }

    @Override
    public synchronized void setIsCurrentPeerChoked(boolean isCurrentPeerChoked) {
        this.currentPeerChoked = false;
    }

    @Override
    public synchronized void sendNotInterestedMessage() {
        currentPeer.removeInterestingPeer(remotePeerid);
        sendMessage(Message.message(MessageType.NOTINTRESTED));
    }

    @Override
    public boolean isCurrentPeerChoked() {
        return currentPeerChoked;
    }

    @Override
    public void sendRequestMessage() {
        Integer nextPieceIndex = currentPeer.selectPiece(this.remotePeerid);

        if (nextPieceIndex == null) {
            if (currentPeer.getInterestingPeers().contains(remotePeerid)) {
                this.sendNotInterestedMessage();
            }
            return;
        }
        requestedPieceIndex = nextPieceIndex;
        if (!this.isCurrentPeerChoked()) {
            this.sendMessage(Message.requestMessage(nextPieceIndex));
        }
    }

    @Override
    synchronized public void close() {
        try{
            in.close();
            out.close();
            connection.close();
        }
        catch(IOException ioException) {
            //ioException.printStackTrace();
        }
    }

    public Integer getRequestedPieceIndex() {
        return requestedPieceIndex;
    }
}


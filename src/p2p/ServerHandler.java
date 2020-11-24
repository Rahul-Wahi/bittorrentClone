package p2p;

import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A handler thread class.  Handlers are spawned from the listening
 * loop and are responsible for dealing with a single client's requests.
 */
    class ServerHandler extends Thread implements PeerHandler {
    Socket requestSocket;           //socket connect to the server
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;          //stream read from the socket
    String serverHostname;                // hostname of the target server
    int serverPort;                  //port name of the target server
    Peer currentPeer;
    Integer remotePeerid;
    boolean currentPeerChoked;
    int totalByteSent;
    int totalByteReceived;
    Logger logger = Logging.getLOGGER();
    public ServerHandler(Peer currentPeer, String hostName, int serverPort) {
        this.currentPeer = currentPeer;
        this.serverHostname = hostName;
        this.serverPort = serverPort;
    }

    public void run() {
        try {
            Message message = new Message();
            //create a socket to connect to the server
            requestSocket = new Socket(serverHostname, serverPort);

            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            //send handshake message
            sendMessage(message.handshakeMessage(currentPeer.getPeerid()));

            byte[] receivedHandshakeByte = new byte[32];

            //receive handshake message
            readMessage(receivedHandshakeByte);
            logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() + "] received handshake message : "
                    + ByteConversionUtil.bytesToString(receivedHandshakeByte));
            int remotePeerid = message.verifyHandshakeMessage(receivedHandshakeByte);
            this.remotePeerid = remotePeerid;
            currentPeer.addConnection(remotePeerid, this);

            BitField bitField = currentPeer.getBitField();

            sendMessage(Message.message(MessageType.BITFIELD, bitField.getBitFieldString()));

            while (!peerProcess.shouldTerminate()) {
                byte[] messageLengthByte = new byte[4];
                byte[] messageType = new byte[1];
                int byteRead = readMessage(messageLengthByte);
                logger.log(Level.INFO, " Total Received Byte fo far : " + totalByteReceived);

                if (byteRead == -1) {
                    currentPeer.cleanup();
                    break;
                }

                byteRead = readMessage(messageType);

                if (byteRead == -1) {
                    currentPeer.cleanup();
                    break;
                }

                int messageLength = ByteConversionUtil.bytesToInt(messageLengthByte);
                byte[] messagePayload = new byte[messageLength - messageType.length];
                byteRead = readMessage(messagePayload);

                if (byteRead == -1) {
                    currentPeer.cleanup();
                    break;
                }

                if (totalByteReceived == 10004864) {
                    System.out.println("here");
                }
                logger.log(Level.INFO, " Total Received Byte fo far : " + totalByteReceived);
                new MessageHandler(this, currentPeer, remotePeerid, ByteConversionUtil.bytesToString(messageType), messagePayload).start();
            }

        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch(UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            //ioException.printStackTrace();
        }
        finally {
            //Close connections
            try{
                in.close();
                out.close();
                requestSocket.close();
            }
            catch(IOException ioException) {
                //ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {
        try{
            //stream write the message
            out.writeObject(msg);
            out.flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    public int readMessage(byte[] msg) {
        int bytesRead = 0;
        try {
            while (bytesRead != msg.length && bytesRead != -1) {
                int read = 0;
                //System.out.print("msg len " + msg.length);
                read = in.read(msg, bytesRead, msg.length - bytesRead);
                bytesRead += read;
                //System.out.println(" off " + bytesRead
                  //      + " len " + (msg.length - bytesRead));
            }

            totalByteReceived += bytesRead;
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
    public synchronized void sendMessage(byte[] msg) {
        try{
            totalByteSent += msg.length;
            logger.log(Level.FINE, "send msg " + msg.length + " Total Bytes Sent so far" + totalByteSent);
            //stream write the message
            out.write(msg);
            out.flush();
        }
        catch(IOException ioException){
            //ioException.printStackTrace();
        }
    }

    @Override
    public synchronized void sendInterestedMessage() {
        sendMessage(Message.message(MessageType.INTERESTED));
        currentPeer.addInterestingPeer(remotePeerid);
    }

    @Override
    public synchronized void sendNotInterestedMessage() {
        sendMessage(Message.message(MessageType.INTERESTED));
        currentPeer.removeInterestingPeer(remotePeerid);
    }

    @Override
    public synchronized void setIsCurrentPeerChoked(boolean isCurrentPeerChoked) {
        this.currentPeerChoked = isCurrentPeerChoked;
    }

    @Override
    public void sendRequestMessage() {
        Integer nextPieceIndex = currentPeer.selectPiece(this.remotePeerid);
        if (nextPieceIndex == null) {
            this.sendNotInterestedMessage();
            return;
        }

        if (!this.isCurrentPeerChoked()) {
            this.sendMessage(Message.requestMessage(nextPieceIndex));
        }
    }

    @Override
    public boolean isCurrentPeerChoked() {
        return currentPeerChoked;
    }
}

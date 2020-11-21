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
    String MESSAGE;                //capitalized message read from the server
    String serverHostname;                // hostname of the target server
    int serverPort;                  //port name of the target server
    Peer currentPeer;
    boolean currentPeerChoked;
    public ServerHandler(Peer currentPeer, String hostName, int serverPort) {
        this.currentPeer = currentPeer;
        this.serverHostname = hostName;
        this.serverPort = serverPort;
    }

    public void run() {
        try{
            Logger logger = Logging.getLOGGER();
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
            in.read(receivedHandshakeByte);
            logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() + "] received handshake message : "
                    + ByteConversionUtil.bytesToString(receivedHandshakeByte));
            int remotePeerid = message.verifyHandshakeMessage(receivedHandshakeByte);

            currentPeer.addConnection(remotePeerid, this);

            BitField bitField = currentPeer.getBitField();

            //send bit field message
            if (bitField.getNumOfSetBit() > 0) {
                sendMessage(Message.message(MessageType.BITFIELD, bitField.getBitFieldString()));
            }

            while (!peerProcess.getTerminate()) {
                byte[] messageLengthByte = new byte[4];
                byte[] messageType = new byte[1];
                in.read(messageLengthByte);
                in.read(messageType);
                int messageLength = ByteConversionUtil.bytesToInt(messageLengthByte);
                byte[] messagePayload = new byte[messageLength - messageType.length];
                System.out.println("len " + messageLength + " t " + ByteConversionUtil.bytesToString(messageType));
                in.read(messagePayload);
                logger.log(Level.INFO, ByteConversionUtil.bytesToString(messageType));
                new MessageHandler(this, currentPeer, remotePeerid, ByteConversionUtil.bytesToString(messageType), messagePayload).start();
            }

        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch(UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally {
            //Close connections
            try{
                in.close();
                out.close();
                requestSocket.close();
            }
            catch(IOException ioException) {
                ioException.printStackTrace();
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

    @Override
    public synchronized void sendMessage(byte[] msg) {
        try{
            //stream write the message
            out.write(msg);
            out.flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    @Override
    public synchronized void setIsCurrentPeerChoked(boolean isCurrentPeerChoked) {
        this.currentPeerChoked = isCurrentPeerChoked;
    }

    @Override
    public boolean isCurrentPeerChoked() {
        return currentPeerChoked;
    }
}

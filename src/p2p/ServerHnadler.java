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
    class ServerHandler extends Thread {
    Socket requestSocket;           //socket connect to the server
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;          //stream read from the socket
    String MESSAGE;                //capitalized message read from the server
    String serverHostname;                // hostname of the target server
    int serverPort;                  //port name of the target server
    Peer currentPeer;
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
            int peerid = message.verifyHandshakeMessage(receivedHandshakeByte);

            BitField bitField = currentPeer.getBitField();

            //send bit field message
            if (bitField.getNumOfSetBit() > 0) {
                sendMessage(Message.message(MessageType.BITFIELD, bitField.getBitFieldString()));
            }

            byte[] recivedBitFieldMessage = new byte[308];
            in.read(recivedBitFieldMessage);
            byte[] receivedBitField = new byte[306];
            System.arraycopy(recivedBitFieldMessage, 2, receivedBitField, 0, 306);
            System.out.println(ByteConversionUtil.bytesToString(receivedBitField));
            System.out.println("current peer " + currentPeer.getPeerid() + " Received message " + MESSAGE);

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

    public synchronized void sendMessage(byte[] msg) {
        try{
            //stream write the message
            System.out.println("Header ");
            System.out.println(msg.length);
            //out.writeObject(msg);
            out.write(msg);
            out.flush();
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}

package p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A handler thread class.  Handlers are spawned from the listening
 * loop and are responsible for dealing with a single client's requests.
 */
    class ClientHandler extends Thread {
    //private String message;    //message received from the client
    private String MESSAGE;    //uppercase message send to the client
    private Socket connection;
    private ObjectInputStream in;    //stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private int no;        //The index number of the client
    private Peer currentPeer;
    Logger logger = Logging.getLOGGER();
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

            //message = (String) in.readObject();
            //message = "";
            byte[] receivedHandshakeByte = new byte[32];
            //in.read(receivedHandshakeByte);
            in.read(receivedHandshakeByte);
            int peerid = message.verifyHandshakeMessage(receivedHandshakeByte);
            logger.log(Level.INFO, "Peer [" + currentPeer.getPeerid() +"] is connected from ["
                    + peerid + "]");

            //reply with handshake message
            sendMessage(message.handshakeMessage(currentPeer.getPeerid()));

            BitField bitField = currentPeer.getBitField();

            //send bit field message
            if (bitField.getNumOfSetBit() > 0) {
                sendMessage(Message.message(MessageType.BITFIELD, bitField.getBitFieldString()));
            }

            byte[] recivedBitFieldMessage = new byte[307];
            byte[] receivedBitField = new byte[306];
            System.arraycopy(recivedBitFieldMessage, 1, receivedBitField, 0, 306);
            System.out.println(ByteConversionUtil.bytesToString(receivedBitField));



        } catch (IOException ioException) {
            System.out.println("Disconnect with Client ");
        } finally {
            //Close connections
            try {
                in.close();
                out.close();
                connection.close();
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client " + no);
            }
        }
    }

    //send a message to the output stream
    public void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("Send message: " + msg + " to Client " + no);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //send a message to the output stream
    public void sendMessage(byte[] msg) {
        try {
            out.write(msg);
            out.flush();
            System.out.println("Send message: " + msg + " to Client " + no);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //receive a message to the output stream
    public void receiveMessage() {
        try {
            //receive the message sent from the client
            MESSAGE = (String) in.readObject();
            logger.log(Level.INFO,"Received message");
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

}


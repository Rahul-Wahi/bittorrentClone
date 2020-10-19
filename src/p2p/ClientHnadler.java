package p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * A handler thread class.  Handlers are spawned from the listening
 * loop and are responsible for dealing with a single client's requests.
 */
    class ClientHandler extends Thread {
    private String message;    //message received from the client
    private String MESSAGE;    //uppercase message send to the client
    private Socket connection;
    private ObjectInputStream in;    //stream read from the socket
    private ObjectOutputStream out;    //stream write to the socket
    private int no;        //The index number of the client
    private Peer currentPeer;

    public ClientHandler(Socket connection, Peer currentPeer) {
        this.connection = connection;
        this.currentPeer = currentPeer;
    }

    public void run() {
        try {
            //initialize Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());

            try {
                //receive the message sent from the client
                message = (String) in.readObject();
                //show the message to the user
                System.out.println("Receive message: " + message + " from client ");

                sendMessage(Message.headerMessage(currentPeer.peerid));
            } catch (ClassNotFoundException classnot) {
                System.err.println("Data received in unknown format");
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

}


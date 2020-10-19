package p2p;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A handler thread class.  Handlers are spawned from the listening
 * loop and are responsible for dealing with a single client's requests.
 */
    class ServerHandler extends Thread {
    Socket requestSocket;           //socket connect to the server
    ObjectOutputStream out;         //stream write to the socket
    ObjectInputStream in;          //stream read from the socket
    String message;                //message send to the server
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
            //create a socket to connect to the server
            requestSocket = new Socket(serverHostname, serverPort);
            System.out.println("Connected to " + serverHostname + " in port " + serverPort);
            //initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            //get Input from standard input
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            sendMessage(Message.headerMessage(currentPeer.peerid));

            //Receive the header from the server
            MESSAGE = (String)in.readObject();

            System.out.println("current peer " + currentPeer.peerid + " Received message " + MESSAGE);

        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch ( ClassNotFoundException e ) {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost) {
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

}

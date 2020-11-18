package p2p;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message {
    private static final String HEADER = "P2PFILESHARINGPROJ";
    private static final String ENCODING = "UTF-8";
    private static final int NUM_OF_ZERO_BITS = 10;
    Logger logger = Logging.getLOGGER();

    public byte[] handshakeMessage(int peerid) {
        byte[] headerBytes = ByteConversionUtil.stringToBytes(HEADER);
        byte[] zeroBytes = new byte[NUM_OF_ZERO_BITS];
        byte[] peeridByte = ByteConversionUtil.intToBytes(peerid);
        return createMessage(headerBytes, zeroBytes, peeridByte);
    }

    public boolean verifyHandshakeMessage(byte[] receivedHandshakeMessage, int expectedPeerId) {
        int receivedPeerId = verifyHandshakeMessage(receivedHandshakeMessage);
        return receivedPeerId == expectedPeerId;
    }

    public int verifyHandshakeMessage(byte[] receivedHandshakeMessage) {
        byte[] headerByte = new byte[HEADER.length()];
        byte[] peeridByte = new byte[4];
        System.arraycopy(receivedHandshakeMessage, 0, headerByte, 0, 18);
        System.arraycopy(receivedHandshakeMessage, HEADER.length() + NUM_OF_ZERO_BITS, peeridByte, 0, 4);

        int peerid = ByteConversionUtil.bytesToInt(peeridByte);
        String header = ByteConversionUtil.bytesToString(headerByte);
        if (!header.equals(HEADER)) {
            logger.log(Level.INFO, "Received wrong header message from peer id: [" + peerid + "]");
        }
        return peerid;
    }

    //choke, unchoke, interested, not interested will use this method as they dont have payload
    public static byte[] message(MessageType messageType) {
        byte[] messageTypeByte = ByteConversionUtil.stringToBytes(Integer.toString(messageType.getValue()));
        byte[] payloadBytes = new byte[0];
        byte[] messageLengthBytes = ByteConversionUtil.intToBytes(messageTypeByte.length + payloadBytes.length);
        return createMessage(messageLengthBytes, messageTypeByte, payloadBytes);
    }

    public static byte[] message(MessageType messageType, String payload) {
        byte[] messageTypeByte = ByteConversionUtil.stringToBytes(Integer.toString(messageType.getValue()));
        byte[] payloadBytes = ByteConversionUtil.stringToBytes(payload);
        byte[] messageLengthBytes = ByteConversionUtil.intToBytes(messageTypeByte.length + payloadBytes.length);
        return createMessage(messageLengthBytes, messageTypeByte, payloadBytes);
    }

    private static byte[] createMessage(byte[] fistPart, byte[] middlePart, byte[] lastPart) {
        byte[] messageBytes = new byte[fistPart.length + middlePart.length + lastPart.length];
        System.arraycopy(fistPart, 0, messageBytes, 0, fistPart.length);
        System.arraycopy(middlePart, 0, messageBytes, fistPart.length, middlePart.length);
        System.arraycopy(lastPart, 0, messageBytes, fistPart.length + middlePart.length, lastPart.length);
        return messageBytes;
    }
}

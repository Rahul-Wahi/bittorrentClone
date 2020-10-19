package p2p;

public class Message {
    private static final String HEADER = "P2PFILESHARINGPROJ";
    private static final String ZERO_BITS = "0000000000";


    public static String headerMessage(int peerid) {
        StringBuilder headerMessageBuilder = new StringBuilder(HEADER);
        headerMessageBuilder.append(ZERO_BITS).append(peerid);
        return headerMessageBuilder.toString();
    }

    //choke, unchoke, interested, not interested will use this method as they dont have payload
    public static String message(MessageType messageType) {
        StringBuilder messageBuilder = new StringBuilder();
        int messageLength = 0;
        int type = messageType.getValue();
        messageBuilder.append(messageLength).append(type);
        return messageBuilder.toString();
    }

    public static String message(MessageType messageType, String payload) {
        StringBuilder messageBuilder = new StringBuilder();
        int messageLength = payload.length();
        int type = messageType.getValue();
        messageBuilder.append(messageLength).append(type).append(payload);
        return messageBuilder.toString();
    }
}

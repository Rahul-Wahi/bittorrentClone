package p2p;

public class Piece {
    private static CommonConfig commonConfig = CommonConfig.getInstance();
    private static Peer currentPeer = peerProcess.getCurrentPeer();

    public static byte[] get(int index) {
        int offset = index * commonConfig.getPieceSize();
        int pieceSize = Math.min(commonConfig.getPieceSize(),
                commonConfig.getFileSize() - index * commonConfig.getPieceSize());
        return currentPeer.getFileHandler().get(offset, pieceSize);
    }

    public static void store(byte[] piece, int index) {
        int offset = index * commonConfig.getPieceSize();
        currentPeer.getFileHandler().put(piece, offset);
        currentPeer.getBitField().setBit(index);
    }

    public static Integer getPieceIndex(byte[] piece) {
        byte[] indexBytes = new byte[4];
        System.arraycopy(piece, 0, indexBytes, 0, indexBytes.length);
        return ByteConversionUtil.bytesToInt(indexBytes);
    }

    public static byte[] getPieceContent(byte[] piece) {
        byte[] indexBytes = new byte[piece.length - 4];
        System.arraycopy(piece, 4, indexBytes, 0, indexBytes.length);
        return indexBytes;
    }


}

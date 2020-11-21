package p2p;

public class Piece {
    private static CommonConfig commonConfig = CommonConfig.getInstance();
    private static Peer currentPeer = peerProcess.getCurrentPeer();

    public static byte[] getPiece(int index) {
        int offset = index * commonConfig.getPieceSize();
        int pieceSize = Math.min(commonConfig.getPieceSize(),
                commonConfig.getFileSize() - index * commonConfig.getPieceSize());
        return currentPeer.getFileHandler().get(offset, pieceSize);
    }

    public static void storePiece(byte[] piece, int index) {
        int offset = index * commonConfig.getPieceSize();
        currentPeer.getFileHandler().put(piece, offset);
        currentPeer.getBitField().setBit(index);
    }


}

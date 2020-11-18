package p2p;

import java.util.Arrays;
import java.util.Set;

public class BitField {
    private Set<Integer> havePieces;
    private boolean[] bitField;
    private int numOfSetBit;
    private static CommonConfig commonConfig = CommonConfig.getInstance();

    public BitField (String bitFieldString) {
        bitField = new boolean[commonConfig.getNumOfPieces()];
        setBitField(bitFieldString);
    }

    public BitField (boolean hasFile) {
        System.out.println(commonConfig.getNumOfPieces());
        bitField = new boolean[commonConfig.getNumOfPieces()];
        setBitField(hasFile);
    }

    public boolean containsInterestedPieces (String receivedBitFieldString) {
        for (int i = 0; i < bitField.length; i++) {
            if (!bitField[i] && receivedBitFieldString.charAt(i) == '1') {
                return true;
            }
        }
        return false;
    }

    public void setBit (int pieceIndex) {
        if (!bitField[pieceIndex]) {
            numOfSetBit++;
            bitField[pieceIndex] = true;
        }
    }

    public boolean completed () {
        return numOfSetBit == bitField.length;
    }

    private void setBitField (String bitFieldString) {
        for (int i = 0; i < bitFieldString.length(); i++) {
            if (bitFieldString.charAt(i) == '1') {
                bitField[i] = true;
                numOfSetBit++;
            }
        }
    }

    private void setBitField (Boolean hasFile) {
        if (!hasFile) {
            return;
        }

        Arrays.fill(bitField, true);

        numOfSetBit = bitField.length;
    }

    public String getBitFieldString () {
        StringBuilder bitFieldString = new StringBuilder();

        for (boolean bit : bitField) {
            if (bit) {
                bitFieldString.append("1");
            } else {
                bitFieldString.append("0");
            }
        }

        return bitFieldString.toString();
    }

    public int getNumOfSetBit() {
        return numOfSetBit;
    }
}

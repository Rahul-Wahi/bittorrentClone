package p2p;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class LoadConfig {
    final static String COMMON_CONFIG_FILE = "Common.cfg";
    final static String PEER_INFO_FILE = "PeerInfo.cfg";
    final static String NUMBER_OF_PREFERRED_NEIGHBORS = "numberOfPreferredNeighbors";
    final static String UNCHOKING_INTERVAL = "unchokingInterval";
    final static String OPTIMISTIC_UNCHOKING_INTERVAL = "optimisticUnchokingInterval";
    final static String FILENAME = "fileName";
    final static String FILESIZE = "fileSize";
    final static String PIECESIZE = "pieceSize";

    public static CommonConfig loadCommonConfig () throws IOException {
        File file = new File(COMMON_CONFIG_FILE);
        CommonConfig commonConfig = new CommonConfig();
        Map<String, String> commonInfo = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(file));

        String info;
        String[] splitInfo;
        while ((info = br.readLine()) != null) {
            if (info.equals("")) {
                continue;
            }

             splitInfo = info.split(" ", 2);
             commonInfo.put(splitInfo[0], splitInfo[1]);
        }

        if (commonInfo.containsKey(NUMBER_OF_PREFERRED_NEIGHBORS)) {
            commonConfig.setNumberOfPreferredNeighbors(Integer.parseInt(commonInfo.get(NUMBER_OF_PREFERRED_NEIGHBORS)));
        } else {
            //throw exception or error message
        }

        if (commonInfo.containsKey(UNCHOKING_INTERVAL)) {
            commonConfig.setUnchokingInterval(Integer.parseInt(commonInfo.get(UNCHOKING_INTERVAL)));
        } else {
            //throw exception or error message
        }

        if (commonInfo.containsKey(OPTIMISTIC_UNCHOKING_INTERVAL)) {
            commonConfig.setOptimisticUnchokingInterval(Integer.parseInt(commonInfo.get(OPTIMISTIC_UNCHOKING_INTERVAL)));
        } else {
            //throw exception or error message
        }

        if (commonInfo.containsKey(FILENAME)) {
            commonConfig.setFileName(commonInfo.get(FILENAME));
        } else {
            //throw exception or error message
        }

        if (commonInfo.containsKey(FILESIZE)) {
            commonConfig.setFileSize(Integer.parseInt(commonInfo.get(FILESIZE)));
        } else {
            //throw exception or error message
        }

        if (commonInfo.containsKey(PIECESIZE)) {
            commonConfig.setPieceSize(Integer.parseInt(commonInfo.get(PIECESIZE)));
        } else {
            //throw exception or error message
        }

        return commonConfig;
    }

    public static List<Peer> loadPeersInfo () throws IOException {
        File file = new File(PEER_INFO_FILE);
        List<Peer> peersList = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(file));

        String info;
        String[] peerInfo;
        while ((info = br.readLine()) != null) {
            if (info.equals("")) {
                continue;
            }

            peerInfo = info.split(" ", 4);
            int peerId = Integer.parseInt(peerInfo[0]);
            String host = peerInfo[1];
            int portNo = Integer.parseInt(peerInfo[2]);
            boolean hasFile = Integer.parseInt(peerInfo[3]) == 1;
            Peer peer = new Peer(peerId, host, portNo, hasFile);
            peersList.add(peer);
        }

        return peersList;
    }

    public static Peer getCurrentPeer (int peerid) throws IOException {
        List<Peer> peers = loadPeersInfo();

        for (Peer peer : peers) {
            if (peer.getPeerID() == peerid) {
                return peer;
            }
        }

        return null;
    }
}

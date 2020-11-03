//package p2p;
import java.nio.charset.Charset;
import java.nio.*;
import java.nio.CharBuffer; 
import java.nio.charset.CharsetEncoder; 
import java.util.*;
import java.util.List;
import java.util.Arrays;
import java.nio.charset.CoderResult;
import java.util.Scanner;
import java.util.HashMap;


//import FileOperations.FileChunking;

public class TestFileOperations{

    public static void main(String args[]){

        //Creating a random string
        // choose characters randomly from string 
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                    + "0123456789"
                                    + "abcdefghijklmnopqrstuvxyz"; 
  
        // create StringBuffer size of AlphaNumericString 
        StringBuilder strb = new StringBuilder(10000232); 
  
        for (long i = 0; i < 10000232; i++) { 
            //Generate string using variable characters 
            int index = (int)(AlphaNumericString.length() * Math.random()); 
  
            // add Character one by one in end of strb 
            strb.append(AlphaNumericString.charAt(index)); 
        }
        String s1= strb.toString();
        int chunk_size = 32768;
        String[] chunks= FileChunking(s1,  chunk_size); //Chunk files into pieces

        System.out.println("There are totally " + chunks.length + " number of pieces"); 


        //Convert to map with key as index and value as the contents of the piece

        Map<Integer, String> map_chunks= ArrayToMap(chunks);
        //System.out.println(map_chunks.size());



        //Check number of pieces

        int num_of_pieces= Numberofpieces(map_chunks);
        System.out.println("There are " + num_of_pieces +" pieces");

        //check if all pieces are present
        boolean flag= verify_all_pieces(map_chunks); //insert the pieces we have
        if (flag== true){
           System.out.println("All pieces are present"); 
        }
        else{
            System.out.println("All pieces are not present. Some pieces of the file are still missing."); 
        }


    }

    public static String[] FileChunking(String s1, int chunk_size) {
        int piece_size= chunk_size;
        Charset cs = Charset.forName("UTF-8");
        CharsetEncoder coder = cs.newEncoder();
        ByteBuffer out = ByteBuffer.allocate(piece_size);  
        // output buffer of maximum chunk size
        CharBuffer in = CharBuffer.wrap(s1);
        List<String> pieces = new ArrayList<>();            
        // list of pieces
        int position = 0;
        while(true) {
            CoderResult code_res = coder.encode(in, out, true); 
            int new_position = s1.length() - in.length();
            String a = s1.substring(position, new_position);
            pieces.add(a);                           
            // add encoding to the list
            position = new_position;                               // store new input position
            out.rewind();                               // and rewind output buffer
            if (! code_res.isOverflow()) {
                break;                                  // everything has been encoded
            }
    }
    return pieces.toArray(new String[0]);


    }//end of function


    public static Map <Integer,String> ArrayToMap(String[] s1) {

        //Convert to map with key as index and value as the contents of the piece

        //final String[] fields = input.split("\\|");
        final Map<Integer, String> map_of_file_pieces = new HashMap<Integer, String>();
        for (int i=0;i<s1.length;i++)
        {
            map_of_file_pieces.put(i, s1[i]);

        }
        return map_of_file_pieces;
    } 

    public static int Numberofpieces(Map <Integer,String> s1) {
        return s1.size();
    }

    public static boolean verify_all_pieces(Map <Integer,String> s1) {
        if (s1.size()== 306)
        {return true;
        }
        else{
            return false;
        }


    }
}
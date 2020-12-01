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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Scanner; 
import java.io.File;
import java.io.FileNotFoundException;

public class FileSimilarity{

    public static void main(String args[]){
        int similar = 1;
        try{
            Scanner input1 = new Scanner(new File("peer_1001/test.txt"));//read first file
            Scanner input2 = new Scanner(new File("peer_1002/test.txt"));//read second file

            while(input1.hasNextLine() && input2.hasNextLine()){
                String first = input1.nextLine();   
                String second = input2.nextLine(); 

                if(!first.equals(second)){
                System.out.println("Differences found: "+"\n"+first+'\n'+second);
                similar = 0;
                }
                
                System.out.println("no exception");

                if(similar==1){
                    System.out.println("Both the files are same ");
                }
                else{
                System.out.println("Both the files are different ");

                }
                }
}
        catch (FileNotFoundException ex)  
        {
        ex.printStackTrace();
        System.out.println("wrong");
        }

    // optionally handle any remaining lines if the line count differs
    }
}
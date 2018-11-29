package test;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.awt.Window;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang3.StringUtils;

//import org.apache.commons.lang3.StringUtils;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
//import org.apache.commons.lang3.StringUtils;


public class Application {

    public static void main(String[] args) throws IOException {
    	//Parse the fasta file to extract gene sequence
		String file = "";
    	ArrayList<String>  gene_sequence=FastaParsing.gene_sequence(file);
    	String original_startTag = "acgcttcgca";
        String original_endTag = "gtatcctatg";
        
        List<Pair>mutatedStartTag= new ArrayList<Pair>();
        List<Pair>mutatedEndTag= new ArrayList<Pair>();
       //Run Optimal String Alignment Alogorithm  to find the mutations in start tag
        // Store the pair of distances and mutated starttag in a list  
         for (int i = 0; i <gene_sequence.size() ; ++i) {
    	    
    		int Distance=OptimalStringAlignment.editDistance(original_startTag, gene_sequence.get(i), 3);
        	if(Distance!=-1) {
        	    Pair pair=new Pair(Distance,gene_sequence.get(i));
        	    mutatedStartTag.add(pair);
     	        pair=null;
        	}      	     	       	   
               	
    }//End of StartTag Mutations
      
      //Run Optimal String Alignment Alogorithm  to find the mutations in end tag
      // Store the pair of distances and mutated End Tag in a list  
      for (int i = 0; i <gene_sequence.size() ; ++i) {
    	    
    		int Distance=OptimalStringAlignment.editDistance(original_endTag , gene_sequence.get(i), 3);
        	if(Distance!=-1) {
        	    Pair pair=new Pair(Distance,gene_sequence.get(i));
        	    mutatedEndTag.add(pair);
     	        pair=null;
        	}      	     	       	   
               	
    }//End of StartTag Mutations  
      //Sort Mutated Start Tag
      Collections.sort(mutatedStartTag,new MyComparator()); 
      //sort Mutated End Tag
      Collections.sort(mutatedEndTag,new MyComparator()); 
      String RetText=ExtractText(mutatedStartTag,mutatedEndTag);
              
      
      //Testing.print_test_results_for_n_errors(original_gene, original_gene, 4);
      
//        ArrayList<Integer> matches = Testing.test_accuracy_of_methods(original_gene, gene_sequence, mutations);
//
//
//        System.out.println("Jaro Matches: " + Integer.toString(matches.get(0)) + "/" + Integer.toString(mutations.size()));
//        System.out.println("Levenshtein Matches: " + Integer.toString(matches.get(1)) + "/" + Integer.toString(mutations.size()));
//        System.out.println("Jaccard Matches: " + Integer.toString(matches.get(2)) + "/" + Integer.toString(mutations.size()));


    }
    public static String ExtractText(List<Pair>mutatedStartTag,List<Pair>mutatedEndTag) {
    	String RetText=null;
    	String orignalText= Utilities. ExtratTextFromFSTAFile();
    	//Take the minimun of mutatedStartTag
    	int length= Math.min(mutatedStartTag.size(), mutatedEndTag.size());
    	    
    		for(int i=0;i<length;i++) {
    			String tempStartTag=mutatedStartTag.get(i).getString();
    			String tempEndTag=mutatedEndTag.get(i).getString();
    			RetText = StringUtils.substringBetween(orignalText,tempStartTag , tempEndTag);
    			//System.out.println(RetText);
    			if(RetText.length()>512) {
    				System.out.println(RetText);
    				return RetText;
    			
    			}
    		}
    	
    	
    	return RetText;
    	
    }
   
    

}

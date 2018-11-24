import java.util.ArrayList;
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

//This class is to pasre the FATSA files 
public class FastaParsing {
	
	public static ArrayList<String> gene_sequence(){
		ArrayList<String> gene_squence;
		String fileContent = Utilities.ExtratTextFromFSTAFile();
		//read the contents of FASTA file
		gene_squence=splitEqually(fileContent,10);
		return gene_squence;
	} //End of Function
    private static ArrayList<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. 
    	ArrayList<String> ret = new ArrayList<String>();
        
        for (int start = 0; (start+size<=text.length()); start ++) {
            ret.add(text.substring(start, start + size));
        }
       // System.out.println(ret);
        return ret;
    } //End of split function

}

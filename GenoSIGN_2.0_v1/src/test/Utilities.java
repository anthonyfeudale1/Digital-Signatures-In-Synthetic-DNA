import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {
 public static String ExtratTextFromFSTAFile() {
		String fileContent = "";
		//read the contents of FASTA file
		//TODO: Currently path to PASTA file is hardcoded. It needs to be passed as an argument
		try {
			fileContent = new String(Files.readAllBytes(Paths.get("src/test/111000_output.gb")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//THIS Part Extract the geneSequence between "ORIGIN" and "\\"
		String wordToFind = "ORIGIN";
		String tempSeq = null;
		String contentuptoorigin = null;
		Pattern word = Pattern.compile(wordToFind);
		Matcher match = word.matcher(fileContent);

		// Match keyword "ORIGIN"
		while (match.find()) {
			// temporary sequence
			tempSeq = fileContent.substring((match.end()), fileContent.length());
		}
     //Remove white spaces, digits and special characters
		tempSeq = tempSeq.replaceAll("\\s", "");
		String tempSeq1=tempSeq.replaceAll("\\d","");
		tempSeq1=tempSeq1.replaceAll("\\W","");
		return tempSeq1;
 }
	

}

package test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;
import javax.swing.JTextArea;

public class DisplayErrors extends JFrame {
	private JPanel contentPane;
	private String errMsg = null;
	private String correctMsg = null;
	
	private String errStart = null;
	private String correctStart = null;
	
	private String errORCID = null;
	private String correctORCID = null;
	
	private String errPlasmidID = null;
	private String correctPlasmidID = null;
	
	private String errSign = null;
	private String correctSign = null;
	
	private String errECC = null;
	private String correctECC = null;
	
	private String errEnd = null;
	private String correctEnd = null;
	
	/**
	 * Launch the application.
	 */
/*	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DisplayErrors frame = new DisplayErrors();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the frame.
	 */
	public DisplayErrors(String oldmsg, String newmsg, String oldStart, String newStart, String oldID, String newID, String oldplasmid, String newplasmid,
			String oldsign, String newsign,  String oldecc, String newecc, String oldEnd, String newEnd) {
		System.out.println("IN DISPLAY ERRORS");
		errMsg = oldmsg;
		correctMsg = newmsg;
		
		errStart = oldStart;
		correctStart = newStart;
		
		errORCID = oldID;
		correctORCID = newID;
		
		errPlasmidID = oldplasmid;
		correctPlasmidID = newplasmid;
		
		errSign = oldsign;
		correctSign = newsign;
		
		errECC = oldecc;
		correctECC = newecc;
		
		errEnd = oldEnd;
		correctEnd = newEnd;
		
		System.out.println(errMsg);
		System.out.println(correctMsg);
		System.out.println(errStart);
		System.out.println(correctStart);
		System.out.println(errORCID);
		System.out.println(correctORCID);
		System.out.println(errPlasmidID);
		System.out.println(correctPlasmidID);
		System.out.println(errSign);
		System.out.println(correctSign);
		System.out.println(errECC);
		System.out.println(correctECC);
		System.out.println(errEnd);
		System.out.println(correctEnd);
		
		List<String> msgerrList = calculateErrors(errMsg, correctMsg);
		List<String> starterrList = calculateErrors(errStart, correctStart);
		List<String> orciderrList = calculateErrors(errORCID, correctORCID);
		List<String> plasmididerrList = calculateErrors(errPlasmidID, correctPlasmidID);
		List<String> signerrList = calculateErrors(errSign, correctSign);
		List<String> enderrList = calculateErrors(errEnd, correctEnd);
		List<String> eccerrList = calculateErrors(errECC,correctECC);
		
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("ERRORS");
		setBounds(100, 100, 449, 454);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(10, 10, 413, 394);
		contentPane.add(textArea);
		
		textArea.append("ORIGINAL DNA SEQUENCE ERRORS.\n");
		if(msgerrList == null || msgerrList.isEmpty()) {
			textArea.append("NO ERRORS.");
		}
		else {
			for(String s:msgerrList) {
				textArea.append(s);
			}
		}
		
		textArea.append("\nSIGNATURE START SEQUENCE ERRORS.\n");
		if(starterrList == null || starterrList.isEmpty()) {
			textArea.append("NO ERRORS.");
		}
		else {
			for(String s:starterrList) {
				textArea.append(s);
			}
		}
		
		
		
		textArea.append("\nORCID SEQUENCE ERRORS.\n");
		if(orciderrList == null || orciderrList.isEmpty()) {
			textArea.append("NO ERRORS.");
		}
		else {
			for(String s:orciderrList) {
				textArea.append(s);
			}
		}
		
		textArea.append("\nPLASMID ID SEQUENCE ERRORS.\n");
		if(plasmididerrList == null || plasmididerrList.isEmpty()) {
			textArea.append("NO ERRORS.");
		}
		else {
			for(String s:plasmididerrList) {
				textArea.append(s);
			}
		}
		
		textArea.append("\nSIGNATURE SEQUENCE ERRORS.\n");
		if(signerrList == null || signerrList.isEmpty()) {
			textArea.append("NO ERRORS.");
		}
		else {
			for(String s:signerrList) {
				textArea.append(s);
			}
		}
		
		textArea.append("\nERROR CORRECTION SEQUENCE ERRORS.\n");
		if(eccerrList == null || eccerrList.isEmpty()) {
			textArea.append("NO ERRORS.");
		}
		else {
			for(String s:eccerrList) {
				textArea.append(s);
			}
		}
		
		textArea.append("\nSIGNATURE END SEQUENCE ERRORS.\n");
		if(enderrList == null || enderrList.isEmpty()) {
			textArea.append("NO ERRORS.");
		}
		else {
			for(String s:enderrList) {
				textArea.append(s);
			}
		}
		
		
	}
	private List<String> calculateErrors(String a, String b) {
		List<String> errList = new ArrayList<String>();
		if(a.isEmpty() || b.isEmpty() || a == null || b == null) {
			return null;
		}
		else if (a.length() != b.length()) {
			return null;
		}
		else {
			char[] arra = a.toCharArray();
			char[] arrb = b.toCharArray();
			
			for(int i=0;i<arra.length;i++) {
				if(arra[i] != arrb[i]) {
					//changing from position to subsequence
					//errList.add("Position - "+(i+1)+"\nErroneous Base is - "+arra[i]+".    Correct Base will be - "+arrb[i]+"\n");
					if(i+10 <= a.length()) {
						errList.add("\nErroneous Sequence - "+arra[i]+arra[i+1]+arra[i+2]+arra[i+3]+arra[i+4]+arra[i+5]+arra[i+6]+arra[i+7]+arra[i+8]+arra[i+9]+
								".\nCorrect Sequence will be - "+arrb[i]+arrb[i+1]+arrb[i+2]+arrb[i+3]+arrb[i+4]+arrb[i+5]+arrb[i+6]+arrb[i+7]+arrb[i+8]+arrb[i+9]+"\n");
					}
					else if((i+10 >= a.length()) && (i-10 >= 0)) {
						errList.add("\nErroneous Sequence - "+arra[i-9]+arra[i-8]+arra[i-7]+arra[i-6]+arra[i-5]+arra[i-4]+arra[i-3]+arra[i-2]+arra[i-1]+arra[i]+
								".\nCorrect Sequence will be - "+arrb[i-9]+arrb[i-8]+arrb[i-7]+arrb[i-6]+arrb[i-5]+arrb[i-4]+arrb[i-3]+arrb[i-2]+arrb[i-1]+arrb[i]+"\n");
					}
					else {
					errList.add("\nErroneous Sequence - "+arra[i-4]+arra[i-3]+arra[i-2]+arra[i-1]+arra[i]+arra[i+1]+arra[i+2]+arra[i+3]+arra[i+4]+
							".\nCorrect Sequence will be - "+arrb[i-4]+arrb[i-3]+arrb[i-2]+arrb[i-1]+arrb[i]+arrb[i+1]+arrb[i+2]+arrb[i+3]+arrb[i+4]+"\n");
					}
				}
			}
		}
		
		return errList;
	}
}

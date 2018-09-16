package test;

import java.awt.Window;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Diptendu
 *
 *         This frame is created by clicking "Sign Message" button in the main
 *         screen. User needs to provide - 1. File for sign. 2. Identity of
 *         signer. 3. Sign start sequence. 4. Sign end sequence.
 * 
 *         Creates the signature sequence from plasmid sequence and passes to
 *         SignaturePlacement.java where user provides position of signature
 *         insertion
 */
public class GenerateSignature extends JFrame {

	/**
	 * Fields and buttons. private key and modulus are passed from KEYGEN.
	 * 
	 * For a commercial app. This frame will connect to the Central Authority and
	 * get the signing token.
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField FileTextField;
	private JTextField IDTextField;
	private JLabel lblSignatureStart;
	private JTextField signStartField;
	private JLabel lblSignatureEnd;
	private JTextField signEndField;
	private JButton btnSubmit;
	private BigInteger priv = null;
	private BigInteger mod = null;
	private JLabel lblNewLabel;
	private JTextField plasmidIDField;

	/**
	 * Create the frame.
	 */

	public GenerateSignature(BigInteger d, BigInteger n) {
		priv = d;
		mod = n;
		final Window w = this;
		setTitle("SIGN MESSAGE");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 603, 419);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Choose file. Opens file system and allow user to select file.
		JButton btnSelectFile = new JButton("File");
		btnSelectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

				int returnValue = jfc.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					System.out.println(selectedFile.getAbsolutePath());
					FileTextField.setText(selectedFile.getAbsolutePath());
				}

			}
		});
		btnSelectFile.setBounds(10, 37, 89, 23);
		contentPane.add(btnSelectFile);

		FileTextField = new JTextField();
		FileTextField.setBounds(136, 38, 416, 20);
		contentPane.add(FileTextField);
		FileTextField.setColumns(10);

		// Enter ORCID of signer.
		JLabel lblEnterId = new JLabel("Enter ORCID");
		lblEnterId.setHorizontalAlignment(SwingConstants.CENTER);
		lblEnterId.setBounds(10, 97, 89, 23);
		contentPane.add(lblEnterId);

		IDTextField = new JTextField();
		IDTextField.setBounds(136, 98, 416, 20);
		contentPane.add(IDTextField);
		IDTextField.setColumns(10);

		// Enter signature start sequence
		lblSignatureStart = new JLabel("Signature Start");
		lblSignatureStart.setHorizontalAlignment(SwingConstants.CENTER);
		lblSignatureStart.setBounds(10, 217, 89, 23);
		contentPane.add(lblSignatureStart);

		signStartField = new JTextField();
		signStartField.setBounds(136, 218, 416, 20);
		contentPane.add(signStartField);
		signStartField.setColumns(10);

		// Enter signature end sequence
		lblSignatureEnd = new JLabel("Signature End");
		lblSignatureEnd.setHorizontalAlignment(SwingConstants.CENTER);
		lblSignatureEnd.setBounds(10, 276, 89, 20);
		contentPane.add(lblSignatureEnd);

		signEndField = new JTextField();
		signEndField.setBounds(136, 276, 416, 20);
		contentPane.add(signEndField);
		signEndField.setColumns(10);

		// SUBMIT button
		btnSubmit = new JButton("SUBMIT");
		btnSubmit.setBounds(259, 320, 89, 23);
		contentPane.add(btnSubmit);

		lblNewLabel = new JLabel("ID format : xxxx-xxxx-xxxx-xxxx");
		lblNewLabel.setBounds(136, 128, 315, 14);
		contentPane.add(lblNewLabel);

		// Enter Plasmid ID
		JLabel lblPlasmidID = new JLabel("Enter PLASMID ID");
		lblPlasmidID.setBounds(20, 161, 102, 33);
		contentPane.add(lblPlasmidID);

		plasmidIDField = new JTextField();
		plasmidIDField.setBounds(136, 167, 416, 20);
		contentPane.add(plasmidIDField);
		plasmidIDField.setColumns(10);

		// Call this when SUBMIT is clicked.
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// error checks - if fields are empty
				if (FileTextField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "NO FILE SELECTED", "alert", JOptionPane.ERROR_MESSAGE);
				} else if (IDTextField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "NO IDENTITY PROVIDED", "alert", JOptionPane.ERROR_MESSAGE);
				} else if (plasmidIDField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "NO PLASMID ID PROVIDED", "alert", JOptionPane.ERROR_MESSAGE);
				} else if (signStartField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "NO START SEQUENCE PROVIDED", "alert",
							JOptionPane.ERROR_MESSAGE);
				} else if (signEndField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "NO END SEQUENCE PROVIDED", "alert", JOptionPane.ERROR_MESSAGE);
				}
				else if(!getFileExtension(FileTextField.getText().trim()).contains("gb")) {
					JOptionPane.showMessageDialog(null, "EXPECTED GENBANK FILE .GB"
							+ "\n PROVIDED FILE HAS EXTENSION ."+getFileExtension(FileTextField.getText().trim()), "alert", JOptionPane.ERROR_MESSAGE);
				}
				// no error - proceed
				else {
					String identity = IDTextField.getText().trim();
					String fileContent = "";
					String start_seq = signStartField.getText().trim().toLowerCase();
					String end_seq = signEndField.getText().trim().toLowerCase();
					String plasmidID = plasmidIDField.getText().trim();

					// try to read the genebank file.
					try {
						fileContent = new String(Files.readAllBytes(Paths.get(FileTextField.getText().trim())));
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "CANNOT READ THE FILE", "alert", JOptionPane.ERROR_MESSAGE);
					}

					// file read and content is not empty
					if (!fileContent.isEmpty()) {

						// content contains the keyword ORIGIN
						if (fileContent.contains("ORIGIN")) {

							// extractDNASequence method to extract plasmid sequence and descriptions from
							// file.

							String[] fileParts = extractDNASequence(fileContent);
							String contentUptoOrigin = fileParts[0];
							String sourceDNASequence = fileParts[1];

							// provided signature start sequence is not present within plasmid sequence
							if (sourceDNASequence.contains(start_seq)) {
								JOptionPane.showMessageDialog(null,
										"START SEQUENCE IS ALREADY PRESENT. CHOOSE DIFFERENT", "alert",
										JOptionPane.ERROR_MESSAGE);
							}
							// provided signature end sequence is not present within plasmid sequence
							else if (sourceDNASequence.contains(end_seq)) {
								JOptionPane.showMessageDialog(null, "END SEQUENCE IS ALREADY PRESENT. CHOOSE DIFFERENT",
										"alert", JOptionPane.ERROR_MESSAGE);
							}
							// proceed if above passed
							else {
								// start the signature algorithm

								MessageDigest digest;
								try {

/*									// check for plasmid id - numbers only
									int plasmididint = Integer.parseInt(plasmidID);
									// specify the hash function "SHA-256"
									digest = MessageDigest.getInstance("SHA-256");
									// hash of the sequence
									byte[] msgHash = digest.digest(sourceDNASequence.getBytes());
									// hash of the ORCID
									byte[] idHash = digest.digest(identity.getBytes());

									BigInteger msgHashInt = new BigInteger(1, msgHash);
									BigInteger idHashInt = new BigInteger(1, idHash);

									// extract the token for signing from provided ORCID. ID ^ d mod n
									// This step is done by CA. The user will receive the token from CA.
									BigInteger extractedPrivKey = idHashInt.modPow(priv, mod);

									// signing step - ( ID ^ d ) ^ H(m) mod n
									BigInteger signatureInt = extractedPrivKey.modPow(msgHashInt, mod);

									// Convert to ACGT
									String binarySignatureString = signatureInt.toString(2);

									// padding extra 0 bits in front if needed
									if (binarySignatureString.length() < mod.bitLength()) {
										StringBuilder sb = new StringBuilder();
										for (int i = 0; i < (mod.bitLength() - binarySignatureString.length()); i++) {
											sb.append("0");
										}
										String padding = sb.toString();
										binarySignatureString = padding.concat(binarySignatureString);
									}

									// convertSignaturetoACGT converts the signbature bits to ACGT
									String dnaSignatureString = convertSignaturetoACGT(binarySignatureString);
									System.out.println("SIGNATURE SEQUENCE= " + dnaSignatureString);
									System.out.println("SIGNATURE SEQUENCE LENGTH = " + dnaSignatureString.length());

									// Convert ORCID to ACGT
									String identitySequence = convertIdentitytoACGT(identity);
									// Convert PLASMID ID to ACGT
									String plasmidIDSequence = convertPlasmidIDtoACGT(plasmidID);

									// debug purpose
									System.out.println("ORC ID Sequence = " + identitySequence);
									System.out.println("ORC ID Sequence length = " + identitySequence.length());
									System.out.println("PLASMID ID Sequence = " + plasmidIDSequence);
									System.out.println("PLASMID ID Sequence length = " + plasmidIDSequence.length());

									// create the final string by combining ORCID + PLASMID ID + SIGNATURE SEQUENCE
									String dnaIdSignatureString = identitySequence.concat(plasmidIDSequence)
											.concat(dnaSignatureString);

									// debug purpose
									System.out.println(
											"ORC ID + PLASMID ID + SIGNATURE SEQUENCE= " + dnaIdSignatureString);
									System.out.println("ORC ID + PLASMID ID + SIGNATURE SEQUENCE LENGTH = "
											+ dnaIdSignatureString.length());*/

									String inputfile = FileTextField.getText().trim();

									// Opens the Signature Placement frame where user will provide position
									// auto open, no clicks needed.
									// pass sourceDNASequence, start_seq, dnaIdSignatureString,
									// end_seq, contentUptoOrigin, identity, inputfile to the frame
									SignaturePlacement sp = new SignaturePlacement(sourceDNASequence, start_seq,
											plasmidID, end_seq, contentUptoOrigin, identity, inputfile,priv,mod);
									sp.setVisible(true);
									// close the current sign message frame.
									w.dispose();

								}
								// cannot load SHA-256
							/*	catch (NoSuchAlgorithmException e1) {
									e1.printStackTrace();
								}*/
								// alert if provided plasmid id is not a number.
								catch (NumberFormatException nfe) {
									JOptionPane.showMessageDialog(null, "PLASMID ID IS 6 DIGIT NUMBERS ONLY", "alert",
											JOptionPane.ERROR_MESSAGE);
								}

							}
						}
						// content present but keyword ORIGIN absent
						else {
							JOptionPane.showMessageDialog(null, "WRONG FILE .. DOES NOT CONTAIN KEYWORD ORIGIN ",
									"alert", JOptionPane.ERROR_MESSAGE);
						}
					}
					// file read successfully but no content / empty file.
					else {
						JOptionPane.showMessageDialog(null, "EMPTY FILE", "alert", JOptionPane.ERROR_MESSAGE);
					}

				}

			}

		


		});

	}

	/**
	 * Extracts the dna sequence from file along with descriptions
	 * 
	 * @param fileContent
	 *            - contents of the input genebank file.
	 * 
	 * @return two content strings - 1. content upto the word "ORIGIN" which are
	 *         descriptions. 2. content after the word "ORIGIN" which is the actual
	 *         plasmid sequence.
	 * 
	 */
	private static String[] extractDNASequence(String fileContent) {

		String wordToFind = "ORIGIN";
		String tempSeq = null;
		String contentuptoorigin = null;
		Pattern word = Pattern.compile(wordToFind);
		Matcher match = word.matcher(fileContent);

		// Match keyword "ORIGIN"
		while (match.find()) {
			System.out.println("Found ORIGIN at index " + match.start() + " - " + (match.end() - 1));
			// temporary main sequence
			tempSeq = fileContent.substring((match.end()), fileContent.length());
			// descriptions
			contentuptoorigin = fileContent.substring(0, match.start());

		}

		// Reformat main sequence to just ACGT.
		tempSeq = tempSeq.replaceAll("\\s", "");
		
		// only take the string between "ORIGIN" and "//"
		if(!tempSeq.contains("//")) {
			JOptionPane.showMessageDialog(null, "DOES NOT CONTAIN END OD FILE DELIMITER //. EXITING",
					"alert", JOptionPane.ERROR_MESSAGE);
			
		}
		String validSeq = StringUtils.substringBefore(tempSeq, "//");
		
		
		char[] seqarray = validSeq.toCharArray();

		StringBuilder sb = new StringBuilder();

		for (char c : seqarray) {
			if (c == 'a' || c == 'c' || c == 'g' || c == 't' || c == 'A' || c == 'C' || c == 'G' || c == 'T') {
				sb.append(c);
			}
		}
		String sequence = sb.toString().trim();
		System.out.println("EXTRACTED ORIGINAL SEQUENCE = " + sequence);
		
		String[] output = new String[2];
		output[0] = contentuptoorigin;
		output[1] = sequence;

		return output;
	}
	 private static String getFileExtension(String fileName) {
	        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
	        return fileName.substring(fileName.lastIndexOf(".")+1);
	        else return "";
	    }
}

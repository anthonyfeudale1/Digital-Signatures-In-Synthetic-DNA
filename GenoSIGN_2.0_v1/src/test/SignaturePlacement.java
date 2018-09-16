package test;

import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;
import javax.swing.JTextPane;

/**
 * @author Diptendu
 * 
 *         This frame is called from GenerateSignature after the signature
 *         sequence is generated. Contains only 1 field where user provides the
 *         position where to put the signature.
 * 
 *         Puts the signature in the provided location within the plasmid.
 *         Updates description, final sequence and outputs a genebank file.
 * 
 */
public class SignaturePlacement extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private String originalDNASequence = null;
	private String signStartDelim = null;
	private String signEndDelim = null;
	private String contentuptoorigin = null;
	private JTextField signLocationField;
	private JTextField numofErrorField;
	private String plasmidid = null;
	private String orcid = null;
	private String dnaIdSignatureString = null;

	/**
	 * Create the frame.
	 */
	public SignaturePlacement(String origDNA, String start, String plasmidID, String end, String contentfp,
			String signerID, String inputFile,BigInteger privateKey, BigInteger modulus) {

		System.out.println("IN SIGN PLACEMENT"); // debug
		originalDNASequence = origDNA;
		plasmidid = plasmidID;
		signStartDelim = start.toLowerCase();
		signEndDelim = end.toLowerCase();
		contentuptoorigin = contentfp;
		orcid = signerID;

		// re-check if all values are received correctly
		System.out.println("ORIGINAL SEQ = " + originalDNASequence);
		System.out.println("ORIGINAL SEQ LENGTH = " + originalDNASequence.length());
		
		System.out.println("SIGN START DELIM = " + signStartDelim);
		System.out.println("SIGN START DELIM LENGTH = " + signStartDelim.length());
		
		System.out.println("SIGN END DELIM = " + signEndDelim);
		System.out.println("SIGN END DELIM LENGTH = " + signEndDelim.length());
		
		System.out.println("PLASMID ID = "+plasmidid);
		
		System.out.println("SIGNER'S ID = "+orcid);
		
		System.out.println("CONTENT UPTO ORIGIN = " + contentuptoorigin);
		
		final Window spw = this;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("SIGNATURE PLACEMENT AND ERROR TOLERANCE");
		setBounds(100, 100, 449, 454);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("Enter the location where you want to put the signature.");
		lblNewLabel.setBounds(10, 11, 395, 42);
		contentPane.add(lblNewLabel);

		// this content is dynamic -
		// displays base pair count present in the source genebank file

		JLabel lblNewLabel_1 = new JLabel(
				"The original sequence contains " + originalDNASequence.length() + " base pairs.");
		lblNewLabel_1.setBounds(10, 48, 395, 14);
		contentPane.add(lblNewLabel_1);

		// instructions for user
		JTextPane signaturePositionText = new JTextPane();
		signaturePositionText.setBackground(UIManager.getColor("Button.background"));
		signaturePositionText.setEditable(false);
		signaturePositionText.setContentType("text/html");
		signaturePositionText.setText("<html>Enter <b>1</b> to put signature at START of original sequence.<br>"
				+ "Enter <b>" + (originalDNASequence.length() + 1)
				+ "</b> to put signature at END of original sequence. <b> OR </b><br>"
				+ "Enter any number between <b>1 and " + (originalDNASequence.length() + 1) + "</b>.</html>");
		signaturePositionText.setBounds(10, 84, 414, 65);
		contentPane.add(signaturePositionText);

		signLocationField = new JTextField();
		signLocationField.setBounds(154, 160, 86, 20);
		contentPane.add(signLocationField);
		signLocationField.setColumns(10);

		JTextPane errorCorrectionText = new JTextPane();
		errorCorrectionText.setBackground(UIManager.getColor("Button.background"));
		errorCorrectionText.setEditable(false);
		errorCorrectionText.setContentType("text/html");
		errorCorrectionText.setText("<html><b>Enter the number of bases that you want to be corrected."
				+ "The error tolerance limit is up to this number of bases.</b></html>");
		errorCorrectionText.setBounds(10, 213, 414, 92);
		contentPane.add(errorCorrectionText);

		numofErrorField = new JTextField();
		numofErrorField.setBounds(154, 316, 86, 20);
		contentPane.add(numofErrorField);
		numofErrorField.setColumns(10);

		JButton btnSubmit = new JButton("SUBMIT");
		btnSubmit.setBounds(154, 382, 89, 23);
		contentPane.add(btnSubmit);

		// call this when submit is clicked
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// error check position not provided
				if (signLocationField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "POSITION CANNOT BE EMPTY", "alert", JOptionPane.ERROR_MESSAGE);
				} else if (numofErrorField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"NUMBER OF ERRORS CANNOT BE EMPTY\n. ENTER 0 TO NOTIFY NO ERROR TOLERANCE", "alert",
							JOptionPane.ERROR_MESSAGE);
				}
				// position provided
				else {
					String location = signLocationField.getText().trim();
					String numberofErr = numofErrorField.getText().trim();
					// position is integer number
					try {
						int signatureLocation = Integer.parseInt(location);

						try {
							int errornum = Integer.parseInt(numberofErr);
							// position is within the range of bases in the plasmid
							if (signatureLocation > 0 && signatureLocation <= originalDNASequence.length() + 1) {

								if (errornum == 0) {
									JOptionPane.showMessageDialog(null,
											"You have selected 0 error tolerance. Click OK to proceed.", "info",
											JOptionPane.INFORMATION_MESSAGE);
								}

								if (errornum <= originalDNASequence.length()) {
									// get feature location from description
									// feature location pattern - 12..240 i.e. int..int
									String[] linesuptoOrigin = contentuptoorigin.split("\n");
									ArrayList<String> linesasList = new ArrayList<String>(
											Arrays.asList(linesuptoOrigin));
									int featureLocation = 0;
									
									// removing the lines which contains the keyword "source"
									ArrayList<String> linesasListwosource = new ArrayList<String>();
									
									for(String s:linesasList) {
										if(!s.contains("source")) {
											linesasListwosource.add(s);
										}
									}
									
									// print to check the updated content
									
									String annotationsWOsource = String.join("\n", linesasListwosource);
									
									System.out.println("\n *** ANNOTATIONS WITHOUT KEYWORD source *** \n");
									System.out.println(annotationsWOsource);
									
									
									Pattern pattern = Pattern.compile("[0-9]+\\.\\.[0-9]+");
									List<String> listFeaturesAll = new ArrayList<String>();
									Matcher m = pattern.matcher(annotationsWOsource);
									while (m.find()) {
										listFeaturesAll.add(m.group());
									}
									System.out.println(listFeaturesAll);
									
									// de-duplicating feature list
									
									Set<String> hs1 = new LinkedHashSet<>(listFeaturesAll);
							        List<String> listFeatures = new ArrayList<>(hs1);
							 
							        System.out.println("After deuplicate removal: \n" + listFeatures);
									
									
									
									boolean isLocationCollision = false;
									boolean isLocationSameAsStart = false;
									
									//isolate start and end location of each feature for collision detection with
									 //provided position
									 
									for (String f : listFeatures) {
										String[] feature = f.trim().split("\\..");
										int start = Integer.parseInt(feature[0]);
										int end = Integer.parseInt(feature[1]);
										if ((start == 1 && end == originalDNASequence.length())
												|| (start == originalDNASequence.length() && end == 1)) {
											// skip the total no. of bases feature
											continue;
										} else {
											if (signatureLocation > start && signatureLocation <= end) {
												isLocationCollision = true;
											} else if (signatureLocation == start) {
												isLocationSameAsStart = true;
											}
										}
									}
									// position colliding with a feature location
									if (isLocationCollision) {
										JOptionPane.showMessageDialog(null,
												"THE PROVIDED POSITION IS COLLIDING WITH A FEATURE", "alert",
												JOptionPane.ERROR_MESSAGE);
									} else {
										// no collision ... proceed
										if (isLocationSameAsStart) {
											JOptionPane.showMessageDialog(null,
													"THE PROVIDED POSITION IS SAME AS START OF A FEATURE\n CLICK OK TO PROCEED.",
													"alert", JOptionPane.ERROR_MESSAGE);
										}
										
										// GENERATE ERROR CORRECTION SEQUENCE
										// ADD THE ECC SEQUENCE WITHIN THE SIGNATURE PART 
										// NOW THE SIGN SEQUENCE CONTAIN - ORCID + PLASMID ID + SIGNATURE + ECC
										// ALL THIS WILL BE PLACED WITHIN THE DELIMITERS 
										
										String finalSignwithDelims = null;
										String eccACGTString = null;
										String sequencetoSign = null;
										
										// shifting the original sequence for signature generation
										// this is due to the circular rotation property
										
										// Convert ORCID to ACGT
										String identitySequence = convertIdentitytoACGT(orcid).trim();
										// Convert PLASMID ID to ACGT
										String plasmidIDSequence = convertPlasmidIDtoACGT(plasmidid).trim();
										
										
										// if sig is placed at start or end of original sequence, no shift is needed
										// concating plasmid ID , because if only plasmid ID sequence is corrupt it needs to be detected
										
										if(signatureLocation == 1 || signatureLocation == (originalDNASequence.length() + 1)) {
											sequencetoSign = originalDNASequence.concat(plasmidIDSequence);
										}
										// shift according to provided position
										else {
											String part1 = originalDNASequence.substring(signatureLocation - 1, originalDNASequence.length());
											String part2 = originalDNASequence.substring(0,signatureLocation - 1);
											sequencetoSign = (part1.concat(part2)).concat(plasmidIDSequence).trim();
										}
										
										// CHECK 
										
										System.out.println("ORIGINAL MSG = "+originalDNASequence.concat(plasmidIDSequence));
										System.out.println("ORIGINAL LENGTH = "+originalDNASequence.concat(plasmidIDSequence).length());
										System.out.println("SHIFTED MSG = "+sequencetoSign);
										System.out.println("SHIFTED LENGTH = "+sequencetoSign.length());
										
										
										// generating signature on the shifted sequence
										
										MessageDigest digest;
										try {
											// check for plasmid id - numbers only
											int plasmididint = Integer.parseInt(plasmidID);
											
											// specify the hash function "SHA-256"
											digest = MessageDigest.getInstance("SHA-256");
											
											// hash of the sequence
											byte[] msgHash = digest.digest(sequencetoSign.getBytes());
											
											// hash of the ORCID
											byte[] idHash = digest.digest(orcid.getBytes());

											BigInteger msgHashInt = new BigInteger(1, msgHash);
											BigInteger idHashInt = new BigInteger(1, idHash);

											// extract the token for signing from provided ORCID. ID ^ d mod n
											// This step is done by CA. The user will receive the token from CA.
											
											BigInteger extractedPrivKey = idHashInt.modPow(privateKey, modulus);

											// signing step - ( ID ^ d ) ^ H(m) mod n
											BigInteger signatureInt = extractedPrivKey.modPow(msgHashInt, modulus);

											// Convert to ACGT
											String binarySignatureString = signatureInt.toString(2);

											// padding extra 0 bits in front if needed
											if (binarySignatureString.length() < modulus.bitLength()) {
												StringBuilder sb = new StringBuilder();
												for (int i = 0; i < (modulus.bitLength() - binarySignatureString.length()); i++) {
													sb.append("0");
												}
												String padding = sb.toString();
												binarySignatureString = padding.concat(binarySignatureString);
											}

											// convertSignaturetoACGT converts the signbature bits to ACGT
											String dnaSignatureString = convertSignaturetoACGT(binarySignatureString);
											System.out.println("SIGNATURE SEQUENCE = " + dnaSignatureString);
											System.out.println("SIGNATURE SEQUENCE LENGTH = " + dnaSignatureString.length());


											// debug purpose
											System.out.println("ORC ID Sequence = " + identitySequence);
											System.out.println("ORC ID Sequence length = " + identitySequence.length());
											System.out.println("PLASMID ID Sequence = " + plasmidIDSequence);
											System.out.println("PLASMID ID Sequence length = " + plasmidIDSequence.length());

											// create the final string by combining ORCID + PLASMID ID + SIGNATURE SEQUENCE
											dnaIdSignatureString = identitySequence.concat(plasmidIDSequence)
													.concat(dnaSignatureString);

											// debug purpose
											System.out.println(
													"ORC ID + PLASMID ID + SIGNATURE SEQUENCE= " + dnaIdSignatureString);
											System.out.println("ORC ID + PLASMID ID + SIGNATURE SEQUENCE LENGTH = "
													+ dnaIdSignatureString.length());

										}
										// cannot load SHA-256
										catch (NoSuchAlgorithmException e1) {
											e1.printStackTrace();
										}
										// alert if provided plasmid id is not a number.
										catch (NumberFormatException nfe) {
											JOptionPane.showMessageDialog(null, "PLASMID ID IS 6 DIGIT NUMBERS ONLY", "alert",
													JOptionPane.ERROR_MESSAGE);
										}
										
										

										
										
										
										
										if(errornum !=0) {
											// upto half of parity bytes can be corrected. 
											// user provides num of errors - parity = 2 * errors
											int numofparityShorts = (2 * errornum);
											List<short[]> paramlist = rs65537.InitGF(numofparityShorts);
											
											// generate ECC on original + ORCID + PLASMID ID + SIGNATURE
											System.out.println("MSG STRING ="+originalDNASequence);
											
											// delete this line
											String combinedSignwithDelims = signStartDelim.concat(dnaIdSignatureString).concat(signEndDelim);
											
											String msgSeq = sequencetoSign.replace(plasmidIDSequence, "").trim();
											
											String dataString = msgSeq.concat(combinedSignwithDelims);
											
											System.out.println("DATA STRING - "+dataString);
											
											System.out.println("ECC INPUT LENGTH= "+dataString.length());
											
											short[] rsEncodedData = rs65537.Encode(numofparityShorts, dataString, paramlist);
											
											
											System.out.println("ECC OUTPUT short ARRAY length = "+rsEncodedData.length);
											
											short[] eccShorts = new short[numofparityShorts];
											
											for(int i=0;i < numofparityShorts;i++) {
												eccShorts[i] = rsEncodedData[dataString.length() + i];
											}
											
											System.out.println("ECC ONLY SHORTS - "+Arrays.toString(eccShorts));
											System.out.println("ECC ONLY SHORTS LENGTH - "+eccShorts.length);
											
											String binaryECCString = ShorttoBinary(eccShorts);
											eccACGTString = convertBinarytoACGT(binaryECCString);
											finalSignwithDelims = signStartDelim.concat(dnaIdSignatureString).concat(eccACGTString).concat(signEndDelim);
											
											
										}
										else {
											finalSignwithDelims = signStartDelim.concat(dnaIdSignatureString).concat(signEndDelim);
										}
										String signplussourceSeq = null;
										// insert the signature sequence within the original plasmid sequence
										if (signatureLocation == 1) {
											signplussourceSeq = finalSignwithDelims.concat(originalDNASequence);
										} else if (signatureLocation == originalDNASequence.length()) {
											signplussourceSeq = originalDNASequence.concat(finalSignwithDelims);
										} else {
											String origpart1 = originalDNASequence.substring(0,
													(signatureLocation - 1));
											String origpart2 = originalDNASequence.substring((signatureLocation - 1),
													originalDNASequence.length());
											signplussourceSeq = origpart1.concat(finalSignwithDelims)
													.concat(origpart2);
										}
										// for check - debug
										System.out.println("FINAL SEQUENCE = " + signplussourceSeq);

										// now organizing the output genebank file accordingly
										// update descriptors, format final sequence

										// Formatting the second part i.e. ORIGIN - END
										String[] formattedtempSign = formatSignatureOutput(signplussourceSeq);
										String[] formattedfinalDNASignatureString = new String[formattedtempSign.length];
										int signlinenum = 1;

										for (int i = 0; i < formattedtempSign.length; i++) {
											formattedfinalDNASignatureString[i] = String.format("%9s", signlinenum)
													+ " " + formattedtempSign[i];
											signlinenum = signlinenum + 60;
										}

										String finalOutputSignature = String.join("\n",
												formattedfinalDNASignatureString);

										String combinedSecondPart = "ORIGIN\n".concat(finalOutputSignature)
												.concat("\n//");
										// second part formatted check
										System.out.println(combinedSecondPart);

										// Updating the descriptions, new feature locations
										String originalDNASeqLength = String.valueOf(originalDNASequence.length());
										String outputDNASeqLength = String.valueOf(signplussourceSeq.length());
										String signwithdelimLength = String.valueOf(finalSignwithDelims.length());
										
										
										
										for (String s : linesasList) {
											if (s.contains("FEATURES")) {
												featureLocation = linesasList.indexOf(s);
											}

											if (s.contains(originalDNASeqLength) && s.contains("bp")) {
												String temp = s.replace(originalDNASeqLength, outputDNASeqLength);
												linesasList.set(linesasList.indexOf(s), temp);
											} else if (s.contains(originalDNASeqLength) && s.contains("base")) {
												String temp = s.replace(originalDNASeqLength, outputDNASeqLength);
												linesasList.set(linesasList.indexOf(s), temp);
											}

											for (String range : listFeatures) {
												if (s.contains(range)) {
													String[] limits = range.trim().split("\\..");
													try {
														int startlimit = Integer.parseInt(limits[0].trim());
														int endlimit = Integer.parseInt(limits[1].trim());
														// update the new total base pairs
														if ((startlimit == 1)
																&& (endlimit == originalDNASequence.length())) {
															String newrange = "1.." + outputDNASeqLength;
															String temp = s.replace(range, newrange);
															linesasList.set(linesasList.indexOf(s), temp);
														} else if ((startlimit < originalDNASequence.length())
																&& (endlimit <= originalDNASequence.length())) {

															// IF provided location > feature position - skip the
															// feature
															// no need to update

															if ((signatureLocation > startlimit)
																	&& (signatureLocation > endlimit)) {
																continue;
															}
															// update those feature location which are after provided
															// position
															else if ((startlimit >= signatureLocation)
																	&& (endlimit > signatureLocation)) {
																String newstart = String.valueOf(startlimit
																		+ Integer.parseInt(signwithdelimLength));
																String newend = String.valueOf(endlimit
																		+ Integer.parseInt(signwithdelimLength));
																String temp = s.replace(range,
																		newstart + ".." + newend);
																linesasList.set(linesasList.indexOf(s), temp);
															}
															// can never go here still adding as safety
															else {
																JOptionPane.showMessageDialog(null,
																		"THIS IS UNEXPECTED. SIGN LOCATION COLLIDE WITH FEATURE LOCATION ",
																		"alert", JOptionPane.ERROR_MESSAGE);
															}
														}
													} catch (NumberFormatException nfe) {
														nfe.printStackTrace();
													}
												}
											}
										}

										// insert the descriptions for signature, sig-start, sig-end
										int insertLocation = 0;
										if (featureLocation != 0) {
											if (linesasList.get(featureLocation + 1).contains("source")) {
												for (int i = featureLocation + 2; i < linesasList.size(); i++) {
													String content = linesasList.get(i).trim();
													if (content.indexOf('/') != 0) {
														insertLocation = i;
														break;
													}
												}
											}
										}

										if (insertLocation != 0) {
											// sig-start
											linesasList.add(insertLocation, "     misc_feature    " + signatureLocation
													+ ".." + (signatureLocation + signStartDelim.length() - 1));
											linesasList.add(insertLocation + 1,
													"                     /label=sig-start");
											linesasList.add(insertLocation + 2,
													"                     /note=\"start of the signature delimiter\"");

											// orcid sequence
											linesasList.add(insertLocation + 3,
													"     misc_feature    "
															+ (signatureLocation + signStartDelim.length()) + ".."
															+ (signatureLocation + signStartDelim.length()
																	+ 32 - 1));
											linesasList.add(insertLocation + 4,
													"                     /label=orcid");
											linesasList.add(insertLocation + 5,
													"                     /note=\"Signer's ORCID sequence\"");
											
											// plasmid ID sequence
											linesasList.add(insertLocation + 6,
													"     misc_feature    "
															+ (signatureLocation + signStartDelim.length()
															+ 32) + ".."
															+ (signatureLocation + signStartDelim.length()
																	+ 32 + 12 - 1));
											linesasList.add(insertLocation + 7,
													"                     /label=plasmid id");
											linesasList.add(insertLocation + 8,
													"                     /note=\"Plasmid ID sequence\"");
											
											// Signature Sequence
											linesasList.add(insertLocation + 9,
													"     misc_feature    "
															+ (signatureLocation + signStartDelim.length()
															+ 32 + 12) + ".."
															+ (signatureLocation + signStartDelim.length()
																	+ 32 + 12 + 512 - 1));
											linesasList.add(insertLocation + 10,
													"                     /label=signature");
											linesasList.add(insertLocation + 11,
													"                     /note=\"This file was signed by "+ signerID+"\"");
											
											if(errornum != 0) {
											// ECC Sequence
											linesasList.add(insertLocation + 12,
													"     misc_feature    "
															+ (signatureLocation + signStartDelim.length()
															+ 32 + 12 + 512) + ".."
															+ (signatureLocation + signStartDelim.length()
																	+ 32 + 12 + 512 + eccACGTString.length() - 1));
											linesasList.add(insertLocation + 13,
													"                     /label=error correction code");
											linesasList.add(insertLocation + 14,
													"                     /note=\"Error correction code sequence\"");

											// sig-end
											linesasList.add(insertLocation + 15, "     misc_feature    "
													+ (signatureLocation + signStartDelim.length()
													+ 32 + 12 + 512 + eccACGTString.length())
													+ ".." + (signatureLocation + signStartDelim.length()
													+ 32 + 12 + 512 + eccACGTString.length() + signEndDelim.length() - 1));
											linesasList.add(insertLocation + 16, "                     /label=sig-end");
											linesasList.add(insertLocation + 17,
													"                     /note=\"end of signature delimiter\"");
											}
											else if (errornum == 0) {
												// no ecc only sig-end
												// sig-end
												linesasList.add(insertLocation + 12, "     misc_feature    "
														+ (signatureLocation + signStartDelim.length()
														+ 32 + 12 + 512)
														+ ".." + (signatureLocation + signStartDelim.length()
														+ 32 + 12 + 512 + signEndDelim.length() - 1));
												linesasList.add(insertLocation + 13, "                     /label=sig-end");
												linesasList.add(insertLocation + 14,
														"                     /note=\"end of signature delimiter\"");
												
											}

											// System.out.println(linesasList);
											String combinedfirstPart = String.join("\n", linesasList);

											// this is the file genebank output file
											String finalGeneBankFile = combinedfirstPart.concat("\n")
													.concat(combinedSecondPart);
											System.out.println(finalGeneBankFile);

											// Detect OS, filepaths are different for windows and linux/mac
											String OS = System.getProperty("os.name").toLowerCase();
											System.out.println("Detected OS = " + OS);

											String part1 = null;
											String filename = null;
											if (OS.contains("nux") || OS.contains("nix") || OS.contains("aix")
													|| OS.contains("NUX") || OS.contains("NIX") || OS.contains("AIX")) {
												part1 = inputFile.substring(0, inputFile.lastIndexOf('/'));
												filename = inputFile.substring(inputFile.lastIndexOf('/'),
														inputFile.lastIndexOf('.'));
											} else if (OS.contains("win") || OS.contains("WIN")) {
												part1 = inputFile.substring(0, inputFile.lastIndexOf('\\'));
												filename = inputFile.substring(inputFile.lastIndexOf('\\'),
														inputFile.lastIndexOf('.'));
											} else if (OS.contains("mac") || OS.contains("MAC") || OS.contains("osx")
													|| OS.contains("OSX")) {
												part1 = inputFile.substring(0, inputFile.lastIndexOf('/'));
												filename = inputFile.substring(inputFile.lastIndexOf('/'),
														inputFile.lastIndexOf('.'));
											} else {
												JOptionPane.showMessageDialog(null, "CANNOT DETECT OPERATING SYSTEM",
														"alert", JOptionPane.ERROR_MESSAGE);
											}

											// outputfilename = inputfilename_output.gb
											// save in same directory as input file
											String outputfile = part1.concat(filename).concat("_output.gb");
											try {
												File file = new File(outputfile);
												FileWriter fileWriter = new FileWriter(file);
												fileWriter.write(finalGeneBankFile);
												fileWriter.flush();
												fileWriter.close();
											} catch (IOException ex) {
												ex.printStackTrace();
											}
											// message to user about completion and output file path
											JOptionPane.showMessageDialog(null,
													"SIGNATURE GENERATED\n OUTPUT FILE - " + outputfile);
											spw.dispose();

										} else {
											JOptionPane.showMessageDialog(null,
													"COULD NOT FIND KEYWORD FEATURE.\n CANNOT INSERT SIG_START, SIG, SIG_END TAGS",
													"alert", JOptionPane.ERROR_MESSAGE);
										}

									}
								}

								// number not within limits of the plasmid bases
								else {
									JOptionPane.showMessageDialog(null,
											"NUMBER OF ERROR TOLERANCE MUST BE WITHIN 0 and " + (originalDNASequence.length()),
											"alert", JOptionPane.ERROR_MESSAGE);
								}
							} else {
								JOptionPane.showMessageDialog(null,
										"SIGNATURE POSITION MUST BE WITHIN 1 and " + (originalDNASequence.length() + 1),
										"alert", JOptionPane.ERROR_MESSAGE);
							}

						} // num of errors is not integer number
						catch (NumberFormatException nfe) {
							JOptionPane.showMessageDialog(null,
									"ENTER A NUMBER FOR ERROR TOLERANCE\nENTER 0 TO NOTIFY NO ERROR TOLERANCE", "alert",
									JOptionPane.ERROR_MESSAGE);
						}
					}

					// position is not integer number
					catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "ENTER A NUMBER FOR SIGNATURE PLACEMENT LOCATION", "alert",
								JOptionPane.ERROR_MESSAGE);
					}

				}
			}

			/**
			 * Format a string of dna sequence into genebank file format
			 * 
			 * @param sequence
			 *            string
			 * 
			 * @return formatted string
			 */
			private String[] formatSignatureOutput(String sequence) {
				// TODO Auto-generated method stub
				char[] temp = sequence.toCharArray();

				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < temp.length; i++) {

					if (i != 0 && ((i % 60) == 0)) {
						sb.append("\n");
					} else if (i != 0 && ((i % 10) == 0)) {
						sb.append(" ");
					}
					sb.append(temp[i]);

				}

				String[] output = sb.toString().split("\n");

				return output;
			}
			
			public String ShorttoBinary( short[] shorts )
			{
			    StringBuilder sb = new StringBuilder(shorts.length * Short.SIZE);
			    for( int i = 0; i < Short.SIZE * shorts.length; i++ )
			        sb.append((shorts[i / Short.SIZE] << i % Short.SIZE & 0x8000) == 0 ? '0' : '1');
			    return sb.toString();
			}
			
			/**
			 *	Convert 6 digit plasmid id to ACGT
			 *  input - 123456 output - acagatcacccg
			 * @param plasmid id
			 * @return plasmid ID in ACGT format
			 * 
			 */
			private String convertPlasmidIDtoACGT(String pid) {
				StringBuilder sb = new StringBuilder();
				String temp = pid;
				if (temp.length() == 6) {
					char[] id = temp.toCharArray();
					for (char c : id) {
						if (c == '0') {
							sb.append("ac");
						} else if (c == '1') {
							sb.append("ag");
						} else if (c == '2') {
							sb.append("at");
						} else if (c == '3') {
							sb.append("ca");
						} else if (c == '4') {
							sb.append("cg");
						} else if (c == '5') {
							sb.append("ct");
						} else if (c == '6') {
							sb.append("ga");
						} else if (c == '7') {
							sb.append("gc");
						} else if (c == '8') {
							sb.append("gt");
						} else if (c == '9') {
							sb.append("ta");
						}
					}
					return sb.toString();
				} else {
					JOptionPane.showMessageDialog(null, "ERROR IN PLASMID ID - ID NOT 6 digits ", "alert",
							JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
			
			/**
			 * Convert 16 digit ORCID to ACGT
			 * input - 1111-2222-3333-4444 output - acacacacagagagagatatatatcacacaca
			 * @param identity
			 * @return idenitity in ACGT format
			 * 
			 */
			private String convertIdentitytoACGT(String identity) {
				StringBuilder sb = new StringBuilder();
				String temp = identity.replaceAll("-", "");
				if (temp.length() == 16) {
					char[] id = temp.toCharArray();
					for (char c : id) {
						if (c == '0') {
							sb.append("ac");
						} else if (c == '1') {
							sb.append("ag");
						} else if (c == '2') {
							sb.append("at");
						} else if (c == '3') {
							sb.append("ca");
						} else if (c == '4') {
							sb.append("cg");
						} else if (c == '5') {
							sb.append("ct");
						} else if (c == '6') {
							sb.append("ga");
						} else if (c == '7') {
							sb.append("gc");
						} else if (c == '8') {
							sb.append("gt");
						} else if (c == '9') {
							sb.append("ta");
						}
					}
					return sb.toString();
				} else {
					JOptionPane.showMessageDialog(null, "ERROR IN ORCID - ID NOT 16 digits ", "alert",
							JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}

			

			
			private String convertBinarytoACGT(String binaryString) {
				char[] binsign = binaryString.toCharArray();
				StringBuilder dnaformatsign = new StringBuilder();

				for (int i = 0; i < binsign.length; i = i + 2) {
					if (binsign[i] == '0' && binsign[i + 1] == '0') {
						dnaformatsign.append("a");
					} else if (binsign[i] == '0' && binsign[i + 1] == '1') {
						dnaformatsign.append("c");
					} else if (binsign[i] == '1' && binsign[i + 1] == '0') {
						dnaformatsign.append("g");
					} else if (binsign[i] == '1' && binsign[i + 1] == '1') {
						dnaformatsign.append("t");
					}
				}

				return dnaformatsign.toString();
			}
			

			/**
			 *  Convert binary string into sequence 
			 *  input - 00100111.... output - agct....
			 * @param binarySignatureString
			 * @return signature string in ACGT format
			 * 
			 */
			private String convertSignaturetoACGT(String binarySignatureString) {
				char[] binsign = binarySignatureString.toCharArray();
				StringBuilder dnaformatsign = new StringBuilder();

				for (int i = 0; i < binsign.length; i = i + 2) {
					if (binsign[i] == '0' && binsign[i + 1] == '0') {
						dnaformatsign.append("a");
					} else if (binsign[i] == '0' && binsign[i + 1] == '1') {
						dnaformatsign.append("c");
					} else if (binsign[i] == '1' && binsign[i + 1] == '0') {
						dnaformatsign.append("g");
					} else if (binsign[i] == '1' && binsign[i + 1] == '1') {
						dnaformatsign.append("t");
					}
				}

				return dnaformatsign.toString();
			}
			
			
			
		});

	}
}

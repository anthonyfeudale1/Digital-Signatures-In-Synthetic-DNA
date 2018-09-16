package test;

import java.awt.Window;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Diptendu
 * 
 *         This frame is created by clicking "Verify Signature" button in the
 *         main screen. User needs to provide the signed file.
 * 
 *         Verifies that signature extracted from the sequence is indeed signed
 *         by the sender. Extracts signers ORCID from sequence, extracts
 *         signature sequence, original sequence and invokes verification
 *         algorithm
 *
 */
public class VerifySignature extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Only one field and button. public key and modulus are passed from KEYGEN.
	 * 
	 * For a commercial app. This frame will connect to the Central Authority and
	 * get the public key and modulus.
	 */

	private JPanel contentPane;
	private BigInteger pub = null;
	private BigInteger mod = null;
	private JTextField FileTextField;
	private JButton btnVerifySignature;
	private String startTag = "acgcttcgca";
	private String endTag = "gtatcctatg";
	/**
	 * Create the frame.
	 */
	public VerifySignature(BigInteger e, BigInteger n) {
		pub = e;
		mod = n;
		final Window w = this;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("VERIFY SIGNATURE");
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Choose file. Opens file system and allow user to select file.
		JButton btnFile = new JButton("Signed File");
		btnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

				int returnValue = jfc.showOpenDialog(null);
				// int returnValue = jfc.showSaveDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					System.out.println(selectedFile.getAbsolutePath());
					FileTextField.setText(selectedFile.getAbsolutePath());
				}

			}
		});
		btnFile.setBounds(10, 105, 110, 31);
		contentPane.add(btnFile);

		FileTextField = new JTextField();
		FileTextField.setBounds(130, 105, 294, 31);
		contentPane.add(FileTextField);
		FileTextField.setColumns(10);

		btnVerifySignature = new JButton("Verify Signature");
		// Call this when SUBMIT is clicked.
		btnVerifySignature.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (FileTextField.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "NO FILE SELECTED", "alert", JOptionPane.ERROR_MESSAGE);
				} else if (!getFileExtension(FileTextField.getText().trim()).contains("fa")) {
					JOptionPane.showMessageDialog(null,
							"EXPECTED FASTA FILE" + "\n PROVIDED FILE HAS EXTENSION ."
									+ getFileExtension(FileTextField.getText().trim()),
							"alert", JOptionPane.ERROR_MESSAGE);
				} else {
					List<String> fileContentList = new ArrayList<String>();
					// try to read the file
					try {
						fileContentList = Files.readAllLines(Paths.get(FileTextField.getText().trim()));
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "CANNOT READ THE FILE", "alert", JOptionPane.ERROR_MESSAGE);
					}

					// file read and content is not empty
					if (!fileContentList.isEmpty()) {
						if (fileContentList.get(0).trim().startsWith(">")) {
							System.out.println("FASTA FILE START SYMBOL OK !");	
							StringBuilder sb = new StringBuilder();
							for (String s:fileContentList) {
								if(!s.trim().startsWith(">")) {
									sb.append(s.trim());
								}
							}
							String filecontent = sb.toString().toLowerCase();
							System.out.println("FILE CONTENT");
							System.out.println(filecontent);
							System.out.println("LENGTH = "+filecontent.length());
							boolean isRevComp = false;
							boolean isNormal = false;
							String signatureSeq = null;
							String orcidSeq = null;
							String plasmidIDSeq = null;
							String originalSeq = null;
							String eccSeq = null;
							String filecontentRevComp = generateReverseComplement(filecontent);
							
							if(filecontent.contains(startTag) || filecontent.contains(endTag)) {								
								isNormal = true;
							}
							
							if(filecontentRevComp.contains(startTag) || filecontentRevComp.contains(endTag)) {
								isRevComp = true;
							}
							
							if(isRevComp || isNormal) {
								if(isNormal) {
									String repeatMsg = filecontent.concat(filecontent).concat(filecontent).trim();
									if(repeatMsg.indexOf(startTag)!= repeatMsg.lastIndexOf(startTag)) {
										
										String temp = StringUtils.substringBetween(repeatMsg, startTag, startTag);
										orcidSeq = temp.substring(0, 32);
										plasmidIDSeq = temp.substring(32, 44);
										signatureSeq = temp.substring(44, 556);
										eccSeq = StringUtils.substringBetween(temp, signatureSeq, endTag);
										originalSeq = StringUtils.substringAfterLast(temp, endTag);
									}
									else {
										JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , ONLY ONE INSTANCE OF START IN COMBINED MSG",
												"alert", JOptionPane.ERROR_MESSAGE);
									}
								}
								else if(isRevComp) {
									String repeatMsg = filecontentRevComp.concat(filecontentRevComp).concat(filecontentRevComp).trim();
									if(repeatMsg.indexOf(startTag)!= repeatMsg.lastIndexOf(startTag)) {
										
										String temp = StringUtils.substringBetween(repeatMsg, startTag, startTag);
										orcidSeq = temp.substring(0, 32);
										plasmidIDSeq = temp.substring(32, 44);
										signatureSeq = temp.substring(44, 556);
										eccSeq = StringUtils.substringBetween(temp, signatureSeq, endTag);
										originalSeq = StringUtils.substringAfterLast(temp, endTag);
									}
									else {
										JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , ONLY ONE INSTANCE OF START IN COMBINED MSG",
												"alert", JOptionPane.ERROR_MESSAGE);
									}
								}
								
								if(!orcidSeq.isEmpty() && !plasmidIDSeq.isEmpty() && !signatureSeq.isEmpty() && !originalSeq.isEmpty()) {
									String identity = extractIdentity(orcidSeq);
									String plasmidID =extractPlasmidID(plasmidIDSeq);
									
									System.out.println("Identity = " + identity);
									System.out.println("PLASMID ID = " + plasmidID);
									System.out.println("Signature = " + signatureSeq);
									System.out.println("Signature length = " + signatureSeq.length());
									if (eccSeq != null && !eccSeq.trim().isEmpty()) {
										System.out.println("ECC = " + eccSeq);
										System.out.println("ECC LENGTH = " + eccSeq.length());

									}
									
									String signedSeq = originalSeq.concat(plasmidIDSeq);
									
									if (signatureSeq.length() == 512) {
										
										try {
											// specify the hash function "SHA-256"
											MessageDigest digest = MessageDigest.getInstance("SHA-256");
											// hash of the original sequence (total - sign)
											byte[] msgHash = digest.digest(signedSeq.getBytes());
											// hash of extracted ORCID
											byte[] idHash = digest.digest(identity.getBytes());

											BigInteger msgHashInt = new BigInteger(1, msgHash);
											BigInteger idHashInt = new BigInteger(1, idHash);

											// lhs of verify . extracted ID ^ H(m)
											BigInteger lhsVerify = idHashInt.modPow(msgHashInt, mod);

											// convert signature string from ACGT to binary
											String binarySignString = convertACGTtoBinary(signatureSeq);

											// from binary to Bigint
											BigInteger signatureInt = new BigInteger(binarySignString, 2);

											// rhs of verify. Sign ^ public key
											BigInteger rhsVerify = signatureInt.modPow(pub, mod);

											// check - debug
											System.out.println(rhsVerify);
											System.out.println(lhsVerify);
											
											
											// if sign valid then lhs == rhs , inform user about success
											if (lhsVerify.compareTo(rhsVerify) == 0) {
												JOptionPane.showMessageDialog(null, "EXTRACTED IDENTITY = " + identity
														+ "\nEXTRACTED PLASMID ID = " + plasmidID
														+ "\nSIGNATURE VALID ! \n THIS FILE WAS SIGNED BY " + identity,
														"SUCCESS", JOptionPane.INFORMATION_MESSAGE);
												// close frame
												w.dispose();
											}
											
											// rest remains
											else if (lhsVerify.compareTo(rhsVerify) != 0
													&& (eccSeq == null || eccSeq.trim().isEmpty())) {
												JOptionPane.showMessageDialog(null, "EXTRACTED IDENTITY = " + identity
														+ "\nEXTRACTED PLASMID ID = " + plasmidID
														+ "\nSIGNATURE INVALID ! \n THIS FILE WAS NOT SIGNED BY "
														+ identity + "\n THERE IS NO ERROR CORRECTION SEQUENCE PRESENT."
														+ "\nCANNOT ATTEMPT TO CORRECT AND RE VERIFY.", "ALERT",
														JOptionPane.ERROR_MESSAGE);
											} else if (lhsVerify.compareTo(rhsVerify) != 0
													&& !eccSeq.trim().isEmpty()) {
												JOptionPane.showMessageDialog(null, "EXTRACTED IDENTITY = " + identity
														+ "\nEXTRACTED PLASMID ID = " + plasmidID
														+ "\nSIGNATURE INVALID ! \n THIS FILE WAS NOT SIGNED BY "
														+ identity
														+ "\n THE ERROR CORRECTION CODE PRESENT IN THE SEQUENCE CAN CORRECT UPTO "
														+ (eccSeq.length() / 16)
														+ " ERRORS. \nCLICK OK TO PROCEED.", "ALERT",
														JOptionPane.ERROR_MESSAGE);

												System.out.println("INVOKING REED - SOLOMON ECC :D");
												
												int numofparityShorts = (eccSeq.length() / 8);
												String eccBinaryString = convertACGTtoBinary(eccSeq);
												short[] eccShorts = BinarytoShort(eccBinaryString);
												System.out.println("ECC ONLY SHORTS - " + Arrays.toString(eccShorts));
												List<short[]> paramlist = rs65537.InitGF(numofparityShorts);
												String errorSequence = originalSeq.concat(startTag).concat(orcidSeq)
														.concat(plasmidIDSeq).concat(signatureSeq).concat(endTag).trim();


												System.out.println("ERROR SEQ = " + errorSequence);
												System.out.println("LENGTH of err seq - " + errorSequence.length());

												short[] modifiedDatashorts = new short[errorSequence.length()];

												byte[] modifiedDataBytes = errorSequence.getBytes();

												for (int i = 0; i < errorSequence.length(); i++) {
													modifiedDatashorts[i] = modifiedDataBytes[i];
												}

												short[] modifiedDatapluseccshorts = concatenateShortArrays(
														modifiedDatashorts, eccShorts);

												short[] modifiedDatapluseccshortsCopy = new short[modifiedDatapluseccshorts.length];

												for (int i = 0; i < modifiedDatapluseccshorts.length; i++) {
													modifiedDatapluseccshortsCopy[i] = modifiedDatapluseccshorts[i];
												}

												System.out.println(
														"INPUT TO DECODE - " + modifiedDatapluseccshorts.length);

												short[] correctedShorts = rs65537.Decode(modifiedDatapluseccshorts,
														numofparityShorts, paramlist);

												System.out.println(
														Arrays.equals(modifiedDatapluseccshortsCopy, correctedShorts));

												if (correctedShorts == null || Arrays
														.equals(modifiedDatapluseccshortsCopy, correctedShorts)) {
													JOptionPane.showMessageDialog(null,
															"CANNOT CORRECT ERROR SEQUENCE. TOO MANY ERRORS ", "alert",
															JOptionPane.ERROR_MESSAGE);
												} else {
													System.out.println("INVOKING RE VERIFICATION PROCESS");
													// System.out.println(Arrays.toString(correctedShorts));
													// extract corrected sequence
													byte[] correctedByteSequence = new byte[correctedShorts.length - numofparityShorts];
													for (int i = 0; i < correctedByteSequence.length; i++) {
														correctedByteSequence[i] = (byte) correctedShorts[i];
													}

													String correctedTotalString = new String(correctedByteSequence);

													String correctedMessageSequence = correctedTotalString.substring(0,originalSeq.length());
													String correctedIDSignSequence = correctedTotalString.substring(originalSeq.length(),correctedTotalString.length());
													String correctedstartSequence = correctedIDSignSequence.substring(0,startTag.length());
													String correctedORCIDSequence = correctedIDSignSequence.substring(startTag.length(),32+startTag.length());
													String correctedPlasmidIDSequence = correctedIDSignSequence.substring(startTag.length()+32, startTag.length()+44);
													String correctedSignatureSequence = correctedIDSignSequence.substring(startTag.length()+44, startTag.length()+556);
													String correctedendSequence = correctedIDSignSequence.substring(startTag.length()+556, startTag.length()+556+endTag.length());
													
													short[] correctedECCShorts = new short[numofparityShorts];
													
													for(int i = 0;i<correctedECCShorts.length;i++) {
														correctedECCShorts[i] = correctedShorts[i+(correctedShorts.length - numofparityShorts)];
													}
													
													System.out.println("CORRECTED ECC SHORTS - "+Arrays.toString(correctedECCShorts));
													
													String correctedECCBinary = ShorttoBinary(correctedECCShorts);
													
													String correctedeccSequence = convertBinarytoACGT(correctedECCBinary);
													
													System.out.println("PREV ECC SEQ - "+eccSeq);
													System.out.println("CORRECT ECC SEQ - "+correctedeccSequence);
													

													// convert the extracted ORCID from ACGT to OCRID format
													String correctedidentity = extractIdentity(correctedORCIDSequence);
													// convert the extracted plasmid id from ACGT to 6 numbers
													String correctedplasmidID = extractPlasmidID(
															correctedPlasmidIDSequence);
													
													String correctedsignedSeq = correctedMessageSequence.concat(correctedPlasmidIDSequence);
													
													if (correctedSignatureSequence.length() == 512) {
														try {
															MessageDigest newdigest = MessageDigest
																	.getInstance("SHA-256");
															// hash of the original sequence (total - sign)
															byte[] correctedmsgHash = newdigest
																	.digest(correctedsignedSeq.getBytes());
															// hash of extracted ORCID
															byte[] correctedIDHash = digest
																	.digest(correctedidentity.getBytes());

															BigInteger correctedmsgHashInt = new BigInteger(1,
																	correctedmsgHash);
															BigInteger correctedIDHashInt = new BigInteger(1,
																	correctedIDHash);

															// lhs of verify . extracted ID ^ H(m)
															BigInteger newlhsVerify = correctedIDHashInt
																	.modPow(correctedmsgHashInt, mod);

															// convert signature string from ACGT to binary
															String correctedbinarySignString = convertACGTtoBinary(
																	correctedSignatureSequence);

															// from binary to Bigint
															BigInteger correctedsignatureInt = new BigInteger(
																	correctedbinarySignString, 2);

															// rhs of verify. Sign ^ public key
															BigInteger newrhsVerify = correctedsignatureInt.modPow(pub,
																	mod);

															// check - debug
															System.out.println(newrhsVerify);
															System.out.println(newlhsVerify);

															if (newlhsVerify.compareTo(newrhsVerify) == 0) {
																JOptionPane.showMessageDialog(null,
																		"EXTRACTED IDENTITY = " + correctedidentity
																				+ "\nEXTRACTED PLASMID ID = "
																				+ correctedplasmidID
																				+ "\nSIGNATURE VALID ON CORRECTED SEQUENCE ! \n THIS FILE WAS SIGNED BY "
																				+ correctedidentity,
																		"SUCCESS", JOptionPane.INFORMATION_MESSAGE);
																// close frame
																w.dispose();
																int response = JOptionPane.showConfirmDialog(null,
																		"Do you want to see where the ERROR was ?",
																		"Confirm", JOptionPane.YES_NO_OPTION,
																		JOptionPane.QUESTION_MESSAGE);

																if (response == JOptionPane.NO_OPTION) {

																	System.out.println("No button clicked");
																} else if (response == JOptionPane.YES_OPTION) {
																	System.out.println("Yes button clicked");
																	DisplayErrors displaywindow = new DisplayErrors(
																			originalSeq, correctedMessageSequence,
																			startTag, correctedstartSequence,
																			orcidSeq, correctedORCIDSequence,
																			plasmidIDSeq,correctedPlasmidIDSequence, 
																			signatureSeq, correctedSignatureSequence,
																			eccSeq, correctedeccSequence,
																			endTag, correctedendSequence);
																	displaywindow.setVisible(true);

																} else if (response == JOptionPane.CLOSED_OPTION) {
																	System.out.println("JOptionPane closed");
																}

															} else {
																JOptionPane.showMessageDialog(null,
																		"CANNOT VALIDATE SIGNATURE AFTER ERROR CORRECTION",
																		"alert", JOptionPane.ERROR_MESSAGE);
															}

														} catch (NoSuchAlgorithmException e1) {
															// TODO Auto-generated catch block
															e1.printStackTrace();
														}
													} else {
														JOptionPane.showMessageDialog(null,
																"ERROR IN CORRECTING SIGNATURE SEQUENCE. NOT 512 BP",
																"alert", JOptionPane.ERROR_MESSAGE);
													}

												}
												
												
											}
											
											
											
										}
										
										catch (NoSuchAlgorithmException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										
										
									}
									else {
										JOptionPane.showMessageDialog(null,
												"ERROR IN RETRIEVING SIGNATURE SEQUENCE. NOT 512 BP", "alert",
												JOptionPane.ERROR_MESSAGE);
									}
									
								}
								else {
									JOptionPane.showMessageDialog(null, "ANY ONE OF THE SEQUENCE IS MISSING FOR VALIDATION",
											"alert", JOptionPane.ERROR_MESSAGE);
								}
								
							}
							else {
								
								JOptionPane.showMessageDialog(null, "START TAG "+startTag+" and END TAG "+endTag+" NOT FOUND !",
										"alert", JOptionPane.ERROR_MESSAGE);
							}
						} else {
							JOptionPane.showMessageDialog(null, "WRONG FILE .. FILE DOES NOT START WITH SYMBOL > ",
									"alert", JOptionPane.ERROR_MESSAGE);
						}
						// empty file content
					} else {
						JOptionPane.showMessageDialog(null, "EMPTY FILE / CANNOT PARSE FILE ", "alert",
								JOptionPane.ERROR_MESSAGE);
					}

				}
			}
		});
		btnVerifySignature.setBounds(141, 208, 136, 23);
		contentPane.add(btnVerifySignature);
	}
	
	private String generateReverseComplement(String filecontent) {
		// TODO Auto-generated method stub
		StringBuilder binaryString = new StringBuilder();
		char[] dnasign = filecontent.toCharArray();
		for (int i = 0; i < dnasign.length; i++) {
			if (dnasign[i] == 'a' || dnasign[i] == 'A') {
				binaryString.append("t");
			} else if (dnasign[i] == 'c' || dnasign[i] == 'C') {
				binaryString.append("g");
			} else if (dnasign[i] == 'g' || dnasign[i] == 'G') {
				binaryString.append("c");
			} else if (dnasign[i] == 't' || dnasign[i] == 'T') {
				binaryString.append("a");
			}

		}
		String complement = binaryString.toString();
		
		return StringUtils.reverse(complement);
	}
	
	
	

	/**
	 * Convert 32 base pairs of ACGT to ORCID input -
	 * acacacacagagagagatatatatcacacaca output - 1111-2222-3333-4444
	 * 
	 * @param identity
	 *            in ACGT format
	 * @return idenitity in ORCID format
	 * 
	 */
	private String extractIdentity(String idSeq) {
		StringBuilder sb = new StringBuilder();
		// System.out.println(temp);
		if (idSeq.length() == 32) {
			char[] id = idSeq.toCharArray();
			for (int i = 0; i < id.length; i = i + 2) {
				if (id[i] == 'a' && id[i + 1] == 'c') {
					sb.append("0");
				} else if (id[i] == 'a' && id[i + 1] == 'g') {
					sb.append("1");
				} else if (id[i] == 'a' && id[i + 1] == 't') {
					sb.append("2");
				} else if (id[i] == 'c' && id[i + 1] == 'a') {
					sb.append("3");
				} else if (id[i] == 'c' && id[i + 1] == 'g') {
					sb.append("4");
				} else if (id[i] == 'c' && id[i + 1] == 't') {
					sb.append("5");
				} else if (id[i] == 'g' && id[i + 1] == 'a') {
					sb.append("6");
				} else if (id[i] == 'g' && id[i + 1] == 'c') {
					sb.append("7");
				} else if (id[i] == 'g' && id[i + 1] == 't') {
					sb.append("8");
				} else if (id[i] == 't' && id[i + 1] == 'a') {
					sb.append("9");
				}
			}
			String tempID = sb.toString();
			char[] tempidchars = tempID.toCharArray();
			StringBuilder sb2 = new StringBuilder();
			for (int i = 0; i < tempidchars.length; i++) {
				if (i != 0 && i % 4 == 0) {
					sb2.append("-");
				}
				sb2.append(tempidchars[i]);
			}
			return sb2.toString();
		} else {
			JOptionPane.showMessageDialog(null, "ERROR IN EXTRACTING ORCID - ID SEQUENCE NOT 32 base pairs ", "alert",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * Convert 12 bp ACGT to 6 digit plasmid id input - acagatcacccg output - 123456
	 * 
	 * @param plasmid
	 *            id in ACGT format
	 * @return plasmid ID in number format
	 * 
	 */
	private String extractPlasmidID(String pidSeq) {
		StringBuilder sb = new StringBuilder();
		// System.out.println(temp);
		if (pidSeq.length() == 12) {
			char[] id = pidSeq.toCharArray();
			for (int i = 0; i < id.length; i = i + 2) {
				if (id[i] == 'a' && id[i + 1] == 'c') {
					sb.append("0");
				} else if (id[i] == 'a' && id[i + 1] == 'g') {
					sb.append("1");
				} else if (id[i] == 'a' && id[i + 1] == 't') {
					sb.append("2");
				} else if (id[i] == 'c' && id[i + 1] == 'a') {
					sb.append("3");
				} else if (id[i] == 'c' && id[i + 1] == 'g') {
					sb.append("4");
				} else if (id[i] == 'c' && id[i + 1] == 't') {
					sb.append("5");
				} else if (id[i] == 'g' && id[i + 1] == 'a') {
					sb.append("6");
				} else if (id[i] == 'g' && id[i + 1] == 'c') {
					sb.append("7");
				} else if (id[i] == 'g' && id[i + 1] == 't') {
					sb.append("8");
				} else if (id[i] == 't' && id[i + 1] == 'a') {
					sb.append("9");
				}
			}

			return sb.toString();
		} else {
			JOptionPane.showMessageDialog(null, "ERROR IN EXTRACTING PLASMID ID - ID SEQUENCE NOT 12 base pairs ",
					"alert", JOptionPane.ERROR_MESSAGE);
			return null;
		}
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
		// TODO Auto-generated method stub

		String wordToFind = "ORIGIN";
		String tempSeq = null;
		String contentuptoorigin = null;
		Pattern word = Pattern.compile(wordToFind);
		Matcher match = word.matcher(fileContent);
		// int count = 0;

		while (match.find()) {
			// count++;
			System.out.println("Found ORIGIN at index " + match.start() + " - " + (match.end() - 1));
			tempSeq = fileContent.substring((match.end()), fileContent.length());
			contentuptoorigin = fileContent.substring(0, match.start());

		}
		// System.out.println(count);
		System.out.println(contentuptoorigin);
		tempSeq = tempSeq.replaceAll("\\s", "");
		// System.out.println(tempSeq);
		char[] seqarray = tempSeq.toCharArray();

		StringBuilder sb = new StringBuilder();

		for (char c : seqarray) {
			if (c == 'a' || c == 'c' || c == 'g' || c == 't' || c == 'A' || c == 'C' || c == 'G' || c == 'T') {
				sb.append(c);
			}
		}
		String sequence = sb.toString().trim();
		System.out.println("EXTRACT = " + sequence);
		// System.out.println(fileContent);
		String[] output = new String[2];
		output[0] = contentuptoorigin;
		output[1] = sequence;

		return output;
	}

	private static String convertACGTtoBinary(String acgtString) {
		StringBuilder binaryString = new StringBuilder();
		char[] dnasign = acgtString.toCharArray();
		// convert signature string from ACGT to binary
		for (int i = 0; i < dnasign.length; i++) {
			if (dnasign[i] == 'a' || dnasign[i] == 'A') {
				binaryString.append("00");
			} else if (dnasign[i] == 'c' || dnasign[i] == 'C') {
				binaryString.append("01");
			} else if (dnasign[i] == 'g' || dnasign[i] == 'G') {
				binaryString.append("10");
			} else if (dnasign[i] == 't' || dnasign[i] == 'T') {
				binaryString.append("11");
			}

		}
		return binaryString.toString();
	}

	public static byte[] fromBinary(String s) {
		int sLen = s.length();
		byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
		char c;
		for (int i = 0; i < sLen; i++)
			if ((c = s.charAt(i)) == '1')
				toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
			else if (c != '0')
				throw new IllegalArgumentException();
		return toReturn;
	}

	public static short[] BinarytoShort(String s) {
		int sLen = s.length();
		short[] toReturn = new short[(sLen + Short.SIZE - 1) / Short.SIZE];
		char c;
		for (int i = 0; i < sLen; i++)
			if ((c = s.charAt(i)) == '1')
				toReturn[i / Short.SIZE] = (short) (toReturn[i / Short.SIZE] | (0x8000 >>> (i % Short.SIZE)));
			else if (c != '0')
				throw new IllegalArgumentException();
		return toReturn;
	}
	
	public String ShorttoBinary( short[] shorts )
	{
	    StringBuilder sb = new StringBuilder(shorts.length * Short.SIZE);
	    for( int i = 0; i < Short.SIZE * shorts.length; i++ )
	        sb.append((shorts[i / Short.SIZE] << i % Short.SIZE & 0x8000) == 0 ? '0' : '1');
	    return sb.toString();
	}

	public short[] concatenateShortArrays(short[] a, short[] b) {
		short[] result = new short[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	private static String getFileExtension(String fileName) {
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		else
			return "";
	}
	
	private String convertBinarytoACGT(String binarySignatureString) {
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
}

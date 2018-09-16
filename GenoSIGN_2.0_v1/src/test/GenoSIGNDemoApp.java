package test;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.awt.event.ActionEvent;

/**
 * @author Diptendu
 * 
 *         Main Application. 
 *         
 *         Contains 3 buttons - Keygen, Sign Message and Verify Signature
 */
public class GenoSIGNDemoApp {

	public BigInteger rsa_private = null;
	public BigInteger rsa_modulus = null;
	public BigInteger rsa_public = null;

	private JFrame frmSampleIbsScheme;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		System.out.println("START"); // for logging and error tracing
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GenoSIGNDemoApp window = new GenoSIGNDemoApp();
					window.frmSampleIbsScheme.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

	}

	/**
	 * Create the application.
	 */
	public GenoSIGNDemoApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame. 3 buttons "KEYGEN", "SIGN" and "VERIFY".
	 */
	private void initialize() {
		frmSampleIbsScheme = new JFrame();
		frmSampleIbsScheme.setTitle("GenoSIGN 2.0 V4");
		frmSampleIbsScheme.setBounds(100, 100, 469, 310);
		frmSampleIbsScheme.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSampleIbsScheme.getContentPane().setLayout(null);

		// KEYGEN SIMULATES THE CENTRAL AUTHORITY. PARAMETERS ARE FIXED FOR POC PURPOSE.
		JButton btnKeygen = new JButton("KeyGen");
		btnKeygen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("IN KEY GEN");
				rsa_private = new BigInteger(
						"98313288378302689336224250224305834295711030536397186205419539895341644016019218846968030677734800709931093051888915258997863014712754096443031223215707058044308768171118738747739029745645513116496882037373373805599567375723624229442965947566573617090703944240419928624037907096630037076663658909878655549793");
				rsa_modulus = new BigInteger(
						"104113337110959237162332816581299994542190702392525973440593678475277208478134821212774194915274951267277801850849042422016982005546161736395742773420285601534181180010281900398524120396443965495944281024612149551776548954532322684219653660227782088264518361091627695478941575247895947070486034173711599208419");
				rsa_public = new BigInteger("65537");
				JOptionPane.showMessageDialog(frmSampleIbsScheme.getComponent(0),
						"PARAMETERS ARE FIXED FOR THIS DEMO. PARAMS LOADED.");
			}
		});
		btnKeygen.setBounds(10, 98, 128, 81);
		frmSampleIbsScheme.getContentPane().add(btnKeygen);

		// OPENS A SEPARATE SIGN FRAME. FIELDS SPECIFIED IN GenerateSignature.java.
		// KEYGEN PARAMS ARE PASSED. KEYGEN BUTTON MUST BE PRESSED BEFORE THIS.
		JButton btnSignMessage = new JButton("Sign Message");
		btnSignMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rsa_private == null) {
					JOptionPane.showMessageDialog(frmSampleIbsScheme.getComponent(0), "NEED KEY PAIR TO PROCEED");
				} else {
					System.out.println("IN SIGN"); // for logging and error tracing
					GenerateSignature sf = new GenerateSignature(rsa_private, rsa_modulus);
					sf.setVisible(true);
				}
			}
		});
		btnSignMessage.setBounds(162, 98, 128, 81);
		frmSampleIbsScheme.getContentPane().add(btnSignMessage);

		// OPENS A SEPARATE VERIFY FRAME. FIELDS SPECIFIED IN VerifySignature.java.
		// KEYGEN PARAMS ARE PASSED. KEYGEN BUTTON MUST BE PRESSED BEFORE THIS.
		JButton btnNewButton = new JButton("Verify Signature");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rsa_public == null) {
					JOptionPane.showMessageDialog(frmSampleIbsScheme.getComponent(0), "NEED KEY PAIR TO PROCEED");
				} else {
					VerifySignature vf = new VerifySignature(rsa_public, rsa_modulus);
					vf.setVisible(true);
				}
			}
		});
		btnNewButton.setBounds(312, 98, 128, 81);
		frmSampleIbsScheme.getContentPane().add(btnNewButton);
	}
}

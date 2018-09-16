package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*DO NOT CHANGE ANY PARAMETER VALUES*/

public class rs65537 {
	
	static final int POLY = 0x1100b; // GF(65536) polynomial
	static final short ALPHA = 0x02; // alpha for GF(65536)
	static final short FCR = 0x01; // first consecutive root (1 or ALPHA)
	
/*	static final int POLY = 0x11d; // GF(256) polynomial
	static final byte ALPHA = 0x02; // alpha for GF(256)
	static final byte FCR = 0x01; // first consecutive root (1 or ALPHA)
*/
	public static void main(String args[]) {
		
		long starttime = System.currentTimeMillis();

		List<short[]> paramlist = new ArrayList<>();

		String data = "AACCGGTTACGT";

		// NUMBER OF PARITY BYTES
		// CORRECTS MAX ERR UPTO NPARITY/2
		int NPARITY = 10;

		paramlist = InitGF(NPARITY);
		// check parameters
		for (short[] b : paramlist) {
			System.out.println(Arrays.toString(b));
		}
		
		
		
		short[] encodedBytes = Encode(NPARITY, data, paramlist);
		
		System.out.println("\nRS ENCODED BYTES\n");
		System.out.println(Arrays.toString(encodedBytes));

		//System.out.println("\nRS ENCODED DATA STRING -" + new String(encodedBytes));

		encodedBytes[0] = 60;
		encodedBytes[1] = 61;
		encodedBytes[12] = 62;
		encodedBytes[3] = 63;
		encodedBytes[4] = 64;
		//encodedBytes[5] = 75;
		
		System.out.println("\nRS MODIFIED BYTES\n");
		System.out.println(Arrays.toString(encodedBytes));
		//System.out.println("\nMODIFIED DATA STRING - " + new String(encodedBytes));

		short[] reconstructedBytes = Decode(encodedBytes, NPARITY, paramlist);
		
		if(reconstructedBytes !=null) {
		System.out.println("\nRS DECODED BYTES");
		System.out.println(Arrays.toString(reconstructedBytes));
		//System.out.println("\nRS DECODED DATA STRING -" + new String(reconstructedBytes));
		}
		else {
			System.out.println("CANNOT CORRECT ERRORS.");
		}
		
		long endtime = System.currentTimeMillis();
		System.out.println("Time to complete - "+(endtime - starttime)+" ms");
	}

	// ------------------------------------------------------------------//
	// InitGF Initialize Galios Stuff //
	// ------------------------------------------------------------------//
	public static List<short[]> InitGF(int NPARITY) {
		List<short[]> paramlist = new ArrayList<>();
		int i;
		short b;
		int NP1 = NPARITY + 1;
		short[] abExp = new short[2 * 65536];
		short[] abLog = new short[65536];
		short[] abGenRoots = new short[NPARITY]; // generator poly roots
		short[] abGenPoly = new short[NP1];
		b = 1;
		for (i = 0; i < (2 * 65536); i++) { // init abExp[]
			abExp[i] = b;
			b = GFMpy0(b, ALPHA);
		}
		abLog[0] = (short) 0xffff; // init abLog[]
		for (i = 0; i < 65535; i++) {
			abLog[(int) abExp[i] & 0xffff] = (short) i;
		}
		b = FCR; // init generator poly roots
		for (i = 0; i < NPARITY; i++) {
			abGenRoots[i] = b;
			b = GFMpy(b, ALPHA, abExp, abLog);
		}

		//System.out.println("abExp - " + Arrays.toString(abExp));
		//System.out.println("abLog - " + Arrays.toString(abLog));
		//System.out.println("abGenRoots - " + Arrays.toString(abGenRoots));
		abGenPoly = Root2Poly(abGenPoly, abGenRoots, abExp, abLog, NPARITY); // init generator poly
		//System.out.println("abGenPoly - " + Arrays.toString(abGenPoly));
		paramlist.add(abExp);
		paramlist.add(abLog);
		paramlist.add(abGenRoots);
		paramlist.add(abGenPoly);

		return paramlist;

	}

	// ------------------------------------------------------------------//
	// GFMpy0(b0,b1) b0*b1 using low level math //
	// ------------------------------------------------------------------//
	public static short GFMpy0(short b0, short b1) {
		int i;
		int product;
		product = 0;
		for (i = 0; i < 16; i++) {
			product <<= 1;
			if ((product & 0x10000) != 0)
				product ^= POLY;
			if ((b0 & 0x8000) != 0) {
				product ^= b1;
			}
			b0 <<= 1;
		}
		return ((short) product);
	}

	// ------------------------------------------------------------------//
	// GFMpy(b0, b1) b0*b1 using logs //
	// ------------------------------------------------------------------//
	static short GFMpy(short b0, short b1, short[] abExp, short[] abLog) {
		if (b0 == 0 || b1 == 0)
			return ((short) 0);
		return (abExp[(abLog[b0 & 0xffff] & 0xffff) + (abLog[b1 & 0xffff] & 0xffff)]);
	}

	// ------------------------------------------------------------------//
	// Root2Poly(pPDst, pVSrc) convert roots into polynomial //
	// ------------------------------------------------------------------//
	static short[] Root2Poly(short[] abGenPoly, short[] abGenRoots, short[] abExp, short[] abLog, int NPARITY) {
		int i, j;
		Arrays.fill(abGenPoly, (short) 0);
		abGenPoly[0] = 1;
		for (j = 0; j < NPARITY; j++) {
			for (i = j; i >= 0; i--) {
				abGenPoly[i + 0x1] = GFSub(abGenPoly[i + 0x1], GFMpy(abGenPoly[i], abGenRoots[j], abExp, abLog));
			}
		}

		return abGenPoly;
	}

	// ------------------------------------------------------------------//
	// GFAdd(b0, b1) b0+b1 //
	// ------------------------------------------------------------------//
	static short GFAdd(short b0, short b1) {
		return ((short) (b0 ^ b1));
	}

	// ------------------------------------------------------------------//
	// GFSub(b0, b1) b0-b1 //
	// ------------------------------------------------------------------//
	static short GFSub(short b0, short b1) {
		return ((short) (b0 ^ b1));
	}

	// ------------------------------------------------------------------//
	// Encode //
	// ------------------------------------------------------------------//
	static short[] Encode(int NPARITY, String data, List<short[]> paramlist) {
		int i, j;
		int NDATA = data.length();
		short bQuot; // quotient byte
		short[] abCdWrd = new short[NPARITY + NDATA];
		
		
		byte[] tempDataba = data.getBytes();
		
		for (int d = 0; d < NDATA; d++) {
			abCdWrd[d] = tempDataba[d];
		}

		short[] abParities = new short[NPARITY];
		short bRem0, bRem1; // partial remainders
		Arrays.fill(abParities, (short) 0); // generate parities
		short[] abGenPoly = paramlist.get(3);

		for (j = 0; j < NDATA; j++) {
			bQuot = GFAdd(abCdWrd[j], abParities[0]);
			bRem0 = 0;
			for (i = NPARITY; i != 0;) {
				bRem1 = GFSub(bRem0, GFMpy(bQuot, abGenPoly[i], paramlist.get(0), paramlist.get(1)));
				i--;
				bRem0 = abParities[i];
				abParities[i] = bRem1;
			}
		}
		for (i = 0; i < NPARITY; i++) { // append parities
			abCdWrd[NDATA + i] = GFSub((short) 0, abParities[i]);
		}

		return abCdWrd;
	}

	// ------------------------------------------------------------------//
	// Decode //
	// ------------------------------------------------------------------//
	static short[] Decode(short[] abCdWrd, int NPARITY, List<short[]> paramlist) {
		int i, j;
		int NCDWRD = abCdWrd.length;
		short[] abSyndromes = GenSyndromes(abCdWrd, NPARITY, paramlist); // generate syndromes

		EuclidReturnObject65537 ero = Euclid(NPARITY, abSyndromes, paramlist, NCDWRD);// Euclid

		if (ero == null)
			return null;

		GenLambdaReturnObject65537 gro = GenLambda(ero.getnErr(), ero.getAbLambdaR(), NPARITY);
		short[] abForney = Forney(paramlist, NPARITY, ero, gro); // Forney

		if (abForney == null || abForney.length == 0)
			return null;

		int nErr = ero.getnErr();
		short[] abLog = paramlist.get(1);
		short[] abErrLoc = ero.getAbErrLoc();
		
		for (j = 0; j < nErr; j++) { // fix errors
			i = NCDWRD - 1 - (abLog[abErrLoc[j] & 0xffff] & 0xffff);
			abCdWrd[i] = GFSub(abCdWrd[i], abForney[j]);
		}
		// if(nErr < nErrMin) // update min and max errors
		// nErrMin = nErr;
		// if(nErr > nErrMax)
		// nErrMax = nErr;
		return abCdWrd;
	}

	// ------------------------------------------------------------------//
	// Forney generate error values using Forney //
	// ------------------------------------------------------------------//
	static short[] Forney(List<short[]> paramlist, int NPARITY, EuclidReturnObject65537 ero, GenLambdaReturnObject65537 gro) {
		int i, j;
		short bDvnd;
		int nErr = ero.getnErr();
		short[] abErrLoc = ero.getAbErrLoc();
		short[] abOmega = ero.getAbOmega();
		int nDLambda = gro.getnDLambda();
		short[] abDLambda = gro.getAbDLambda();
		int NP1 = NPARITY + 1;
		short bDvsr;
		short[] abLog = paramlist.get(1);
		short[] abForney = new short[NP1];
		short fcorr = (short) (1 - abLog[FCR & 0xffff]); // correction value for FCR
		short bILoc; // inverse of locator
		Arrays.fill(abForney, (short) 0);
		for (j = 0; j < nErr; j++) {
			bDvsr = bDvnd = 0;
			bILoc = GFDiv((short) 1, abErrLoc[j], paramlist.get(0), paramlist.get(1));
			for (i = nErr; i != 0;) {
				i--;
				bDvnd = GFAdd(bDvnd,
						GFMpy(abOmega[i], GFPow(bILoc, (short) ((nErr - 1) - i), paramlist.get(0), paramlist.get(1)),
								paramlist.get(0), paramlist.get(1)));
			}
			bDvnd = GFMpy(bDvnd, GFPow(abErrLoc[j], fcorr, paramlist.get(0), paramlist.get(1)), paramlist.get(0),
					paramlist.get(1));
			for (i = nDLambda; i != 0;) {
				i--;
				bDvsr = GFAdd(bDvsr,
						GFMpy(abDLambda[i],
								GFPow(bILoc,
										GFMpy((short) 2, (short) (nDLambda - 1 - i), paramlist.get(0), paramlist.get(1)),
										paramlist.get(0), paramlist.get(1)),
								paramlist.get(0), paramlist.get(1)));
			}
			if (bDvsr == 0) {
				System.out.println("Forney divide by 0");
				return null;
			}
			abForney[j] = GFSub((short) 0, GFDiv(bDvnd, bDvsr, paramlist.get(0), paramlist.get(1)));
		}
		return abForney;
	}

	// ------------------------------------------------------------------//
	// GFPow(b0, b1) b0^b1 //
	// ------------------------------------------------------------------//
	static short GFPow(short b0, short b1, short[] abExp, short[] abLog) {
		short b;
		b = 1;
		while (b1 != 0) {
			if ((b1 & 1) != 0)
				b = GFMpy(b, b0, abExp, abLog);
			b0 = GFMpy(b0, b0, abExp, abLog);
			b1 = (short) ((b1 & 0xffff) >> 1);
		}
		return (b);
	}

	// ----------------------------------------------------------------------//
	// GenLambda //
	// ----------------------------------------------------------------------//
	static GenLambdaReturnObject65537 GenLambda(int nErr, short[] abLambdaR, int NPARITY) {
		int i, j;
		int NP1 = NPARITY + 1;
		short[] abLambda = new short[NP1];
		short[] abDLambda = new short[NP1];
		// Lambda = reverse of LambdaR
		for (i = 0; i <= nErr; i++) {
			abLambda[i] = abLambdaR[nErr - i];
		}
		// generate DLambda from Lambda (copy odd terms)
		// example: derivative of a x^3 + b x^2 + cx + d
		// (a+a+a) x^2 + (b+b) x + (c)
		// a x^2 + 0 x + c
		int nDLambda = (nErr + 1) / 2;
		j = nDLambda - 1;
		for (i = nErr - 1; i >= 0;) {
			abDLambda[j] = abLambda[i];
			j -= 1;
			i -= 2;
		}
		GenLambdaReturnObject65537 gro = new GenLambdaReturnObject65537();
		gro.setnDLambda(nDLambda);
		gro.setAbDLambda(abDLambda);
		return gro;
	}

	// ------------------------------------------------------------------//
	// GenSyndromes generate standard RS syndromes //
	// ------------------------------------------------------------------//

	static short[] GenSyndromes(short[] abCdWrd, int NPARITY, List<short[]> paramlist) {
		short[] abSyndromes = new short[NPARITY];
		int i, j;
		short[] abGenRoots = paramlist.get(2);
		int NCDWRD = abCdWrd.length;
		for (j = 0; j < NPARITY; j++) {
			abSyndromes[j] = abCdWrd[0]; // generate a syndrome
			for (i = 1; i < NCDWRD; i++) {
				abSyndromes[j] = GFAdd(abCdWrd[i],
						GFMpy(abGenRoots[j], abSyndromes[j], paramlist.get(0), paramlist.get(1)));
			}
		}
		return abSyndromes;
	}

	// ------------------------------------------------------------------//
	// Euclid extended Euclid division algorithm //
	// generates a series of polynomials: //
	// A[i]S(x) + B[i](x^t) = R[i] //
	// where the degree of R[i] decreases with each iteration //
	// until degree <= MAXERR, then A[i] = Lambda, R[i] = Omega //
	// abE0, abE1: left side contains R[] //
	// abE0, abE1: right side contains reversed A[] //
	// iE0, iE1: index to end of R[], start of A[] //
	// ------------------------------------------------------------------//
	static EuclidReturnObject65537 Euclid(int NPARITY, short[] abSyndromes, List<short[]> paramlist, int NCDWRD) {
		int i;
		short bQuot; // quotient
		// E0.R[-1] = x^MAXERR, E0.A[0] = 1
		int NP1 = NPARITY + 1;
		int NP2 = NPARITY + 2;
		int iE0 = NP1;
		short[] abET;
		int iET;
		short[] abLambdaR = new short[NP1];
		short[] abOmega = new short[NP1];
		int MAXERR = NPARITY / 2;
		short[] abErrLoc = new short[NPARITY];
		short[] abE0 = new short[NP2];
		short[] abE1 = new short[NP2];

		Arrays.fill(abE0, (short) 0);
		abE0[0] = 1;
		abE0[iE0] = 1;
		// E1.R[0] = syndrome polynomial, E1.A[-1] = 0
		int iE1 = NP1;
		Arrays.fill(abE1, (short) 0);
		for (i = 1; i < iE1; i++) {
			abE1[i] = abSyndromes[NPARITY - i];
		}
		// abE1[0] = 0;
		// abE1[iE1] = 0;

		while (true) { // while degree of E1.R[] > max error
			while ((abE1[0] == 0) && // shift E1 left until E1.R[] msb!=0
					(iE1 != 0)) { // or fully shifted left
				iE1--;
				for (i = 0; i < NP1; i++)
					abE1[i] = abE1[i + 1];
				abE1[NP1] = 0;
			}
			if (iE1 <= MAXERR) { // if degree of E1.R[] <= MAXERR, break
				break;
			}
			while (true) { // while more divide/multiply sub-steps
				if (abE0[0] != 0) { // if E0.R[] msb!=0
					bQuot = GFDiv(abE0[0], abE1[0], paramlist.get(0), paramlist.get(1)); // Q=E0.R[msb]/E1.R[msb]
					for (i = 0; i < iE1; i++) { // E0.R[]=E0.R[]-Q*E1.R[]
						abE0[i] = GFSub(abE0[i], GFMpy(bQuot, abE1[i], paramlist.get(0), paramlist.get(1)));
					}
					for (i = iE0; i < NP2; i++) { // E1.A[]=E1.A[]-Q*E0.A[]
						abE1[i] = GFSub(abE1[i], GFMpy(bQuot, abE0[i], paramlist.get(0), paramlist.get(1)));
					}
				}
				if (iE0 == iE1) { // if sub-steps done, break
					break;
				}
				iE0--; // shift E0 left
				for (i = 0; i < NP1; i++)
					abE0[i] = abE0[i + 1];
				abE0[NP1] = 0;
			}
			abET = abE0; // swap E0, E1
			abE0 = abE1;
			abE1 = abET;
			iET = iE0;
			iE0 = iE1;
			iE1 = iET;
		}

		int nErr = NP1 - iE0; // number of errors
		if (iE1 > nErr) { // if degree E1.R[] too high
			System.out.println("degree E1.R[] too high");
			return null;
		}
		while (iE1 < nErr) { // right shift E1 if Omega
			iE1++; // has leading zeroes
			for (i = nErr; i != 0;) {
				i--;
				abE1[i + 1] = abE1[i];
			}
			abE1[0] = (short) 0;
		}
		bQuot = abE0[iE0]; // bQuot = lsb of Lambda
		if (bQuot == 0) {
			System.out.println("lsb of Lambda == 0");
			return null;
		}
		// LambdaR = E0.A[] / bQuot (without unreversing E0.A[])
		for (i = 0; i <= nErr; i++)
			abLambdaR[i] = GFDiv(abE0[i + iE0], bQuot, paramlist.get(0), paramlist.get(1));
		// Omega = E1.R[] / bquot
		for (i = 0; i <= nErr; i++)
			abOmega[i] = GFDiv(abE1[i], bQuot, paramlist.get(0), paramlist.get(1));
		// Find roots of LambdaR, roots == error locators
		abErrLoc = Poly2Root(abErrLoc, abLambdaR, nErr, NCDWRD, paramlist);
		if (abErrLoc == null || abErrLoc.length == 0) {
			System.out.println("poly2root(LambdaR) failed");
			return null;
		}
		EuclidReturnObject65537 ero = new EuclidReturnObject65537();
		ero.setnErr(nErr);
		ero.setAbErrLoc(abErrLoc);
		ero.setAbLambdaR(abLambdaR);
		ero.setAbOmega(abOmega);
		return ero;
	}

	// ------------------------------------------------------------------//
	// GFDiv(b0, b1) b0/b1 //
	// ------------------------------------------------------------------//
	static short GFDiv(short b0, short b1, short[] abExp, short[] abLog) {
		if (b1 == 0) {
			System.out.println("divide by zero");
			return ((short) 0);
		}
		if (b0 == 0)
			return ((short) 0);
		return (abExp[(abLog[b0 & 0xffff] & 0xffff) - (abLog[b1 & 0xffff] & 0xffff) + 0xffff]);
	}

	// ------------------------------------------------------------------//
	// Poly2Root //
	// ------------------------------------------------------------------//
	static short[] Poly2Root(short[] abDst, short[] abSrc, int n, int NCDWRD, List<short[]> paramlist) {
		int i, j;
		short bLoc; // current locator
		short bSum; // current sum
		short iDst; // index to abDst
		if (n == 0)
			return null;
		iDst = 0;
		bLoc = 1;
		for (j = 0; j < NCDWRD; j++) {
			bSum = 0; // sum up terms
			for (i = 0; i <= n; i++) {
				bSum = GFMpy(bSum, bLoc, paramlist.get(0), paramlist.get(1));
				bSum = GFAdd(bSum, abSrc[i]);
			}
			if (bSum == 0) { // if a root
				if (iDst > n) { // exit if too many roots
					return (null);
				}
				abDst[iDst] = bLoc; // append locator
				iDst++;
			}
			bLoc = GFMpy(bLoc, ALPHA, paramlist.get(0), paramlist.get(1));
		} // advance locator
		if (iDst != n) // exit if not enough roots
			return (null);
		return abDst; // indicate success
	}

}

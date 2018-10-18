package test;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.max;


/**
 * Implementation of the OSA which is similar to the Damerau-Levenshtein in that it allows for transpositions to
 * count as a single edit distance, but is not a true metric and can over-estimate the cost because it disallows
 * substrings to edited more than once.  See wikipedia for more discussion on OSA vs DL
 * <p/>
 * See Algorithms on Strings, Trees and Sequences by Dan Gusfield for more information.
 * <p/>
 * This also has a set of local buffer implementations to avoid allocating new buffers each time, which might be
 * a premature optimization
 * <p/>
 * https://www.javacodegeeks.com/2013/11/java-implementation-of-optimal-string-alignment.html
 * Open source version of the OSA used under public domain with credit given to author Steve Ash
 * @author Steve Ash
 */
public class OptimalStringAlignment {

    private static final int threadLocalBufferSize = 64;

    private static final ThreadLocal<short[]> costLocal = new ThreadLocal<short[]>() {
        @Override
        protected short[] initialValue() {
            return new short[threadLocalBufferSize];
        }
    };

    private static final ThreadLocal<short[]> back1Local = new ThreadLocal<short[]>() {
        @Override
        protected short[] initialValue() {
            return new short[threadLocalBufferSize];
        }
    };

    private static final ThreadLocal<short[]> back2Local = new ThreadLocal<short[]>() {
        @Override
        protected short[] initialValue() {
            return new short[threadLocalBufferSize];
        }
    };

    public static int editDistance(CharSequence s1, CharSequence s2, int threshold) {


        if (s1.length() + 1 > threadLocalBufferSize || s2.length() + 1 > threadLocalBufferSize)
            return editDistanceWithNewBuffers(s1, s2, (short) threshold);

        short[] cost = costLocal.get();
        short[] back1 = back1Local.get();
        short[] back2 = back2Local.get();
        return editDistanceWithBuffers(s1, s2, (short) threshold, back2, back1, cost);
    }


    static int editDistanceWithNewBuffers(CharSequence s1, CharSequence s2, short threshold) {
        int slen = s1.length();
        short[] back1 = new short[slen + 1];    // "up 1" row in table
        short[] back2 = new short[slen + 1];    // "up 2" row in table
        short[] cost = new short[slen + 1];     // "current cost"

        return editDistanceWithBuffers(s1, s2, threshold, back2, back1, cost);
    }

    private static int editDistanceWithBuffers(CharSequence s1, CharSequence s2, short threshold,
                                               short[] back2, short[] back1, short[] cost) {

        short slen = (short) s1.length();
        short tlen = (short) s2.length();

        // if one string is empty, the edit distance is necessarily the length of the other
        if (slen == 0) {
            return tlen <= threshold ? tlen : -1;
        } else if (tlen == 0) {
            return slen <= threshold ? slen : -1;
        }

        // if lengths are different > k, then can't be within edit distance
        if (abs(slen - tlen) > threshold)
            return -1;

        if (slen > tlen) {
            // swap the two strings to consume less memory
            CharSequence tmp = s1;
            s1 = s2;
            s2 = tmp;
            slen = tlen;
            tlen = (short) s2.length();
        }

        initMemoiseTables(threshold, back2, back1, cost, slen);

        for (short j = 1; j <= tlen; j++) {
            cost[0] = j; // j is the cost of inserting this many characters

            // stripe bounds
            int min = max(1, j - threshold);
            int max = min(slen, (short) (j + threshold));

            // at this iteration the left most entry is "too much" so reset it
            if (min > 1) {
                cost[min - 1] = Short.MAX_VALUE;
            }

            iterateOverStripe(s1, s2, j, cost, back1, back2, min, max);

            // swap our cost arrays to move on to the next "row"
            short[] tempCost = back2;
            back2 = back1;
            back1 = cost;
            cost = tempCost;
        }

        // after exit, the current cost is in back1
        // if back1[slen] > k then we exceeded, so return -1
        if (back1[slen] > threshold) {
            return -1;
        }
        return back1[slen];
    }

    private static void iterateOverStripe(CharSequence s1, CharSequence s2, short j,
                                          short[] cost, short[] back1, short[] back2, int min, int max) {

        // iterates over the stripe
        for (int i = min; i <= max; i++) {

            if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                cost[i] = back1[i - 1];
            } else {
                cost[i] = (short) (1 + min(cost[i - 1], back1[i], back1[i - 1]));
            }
            if (i >= 2 && j >= 2) {
                // possible transposition to check for
                if ((s1.charAt(i - 2) == s2.charAt(j - 1)) &&
                        s1.charAt(i - 1) == s2.charAt(j - 2)) {
                    cost[i] = min(cost[i], (short) (back2[i - 2] + 1));
                }
            }
        }
    }

    private static void initMemoiseTables(short threshold, short[] back2, short[] back1, short[] cost, short slen) {
        // initial "starting" values for inserting all the letters
        short boundary = (short) (min(slen, threshold) + 1);
        for (short i = 0; i < boundary; i++) {
            back1[i] = i;
            back2[i] = i;
        }
        // need to make sure that we don't read a default value when looking "up"
        Arrays.fill(back1, boundary, slen + 1, Short.MAX_VALUE);
        Arrays.fill(back2, boundary, slen + 1, Short.MAX_VALUE);
        Arrays.fill(cost, 0, slen + 1, Short.MAX_VALUE);
    }

    private static short min(short a, short b) {
        return (a <= b ? a : b);
    }

    private static short min(short a, short b, short c) {
        return min(a, min(b, c));
    }
}
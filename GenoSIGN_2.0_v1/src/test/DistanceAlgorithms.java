package test;

public class DistanceAlgorithms {
    public static double jaro(String s1, String s2) {

        if (s1.length() == 0 && s2.length() == 0)
            return 1;
        int max_matching_distance = Math.max(s1.length(), s2.length()) / 2;
        max_matching_distance = max_matching_distance - 1;
        int matches = 0;
        int transpositions = 0;


        boolean[] s1_matches = new boolean[s1.length()];
        boolean[] s2_matches = new boolean[s2.length()];

        for (int i = 0; i < s1.length(); ++i) {
            int start = Integer.max(0, i - max_matching_distance);
            int end = Integer.min(i + max_matching_distance + 1, s2.length());

            for (int j = start; j < end; j++) {
                if (s2_matches[j]) continue;
                if (s1.charAt(i) != s2.charAt(j)) continue;
                s1_matches[i] = true;
                s2_matches[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) return 0;

        int k = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (!s1_matches[i]) continue;
            while (!s2_matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        return (((double) matches / s1.length()) +
                ((double) matches / s2.length()) +
                (((double) matches - transpositions / 2.0) / matches)) / 3.0;
    }

    //returns -1 if length_of_prefix_l is negative or greater than four
    //returns -1 if weight_of_scaling_factor_p is zero or greater than 0.25
    public static double jaro_winkler(String s1, String s2, int length_of_prefix_l, double weight_of_scaling_factor_p) {
        if (length_of_prefix_l > 4 || length_of_prefix_l < 1)
            return -1;
        if (weight_of_scaling_factor_p > 0.25 || weight_of_scaling_factor_p < 0)
            return -1;

        double jaro = jaro(s1, s2);
        return jaro + (length_of_prefix_l * weight_of_scaling_factor_p * (1 - jaro));


    }
}

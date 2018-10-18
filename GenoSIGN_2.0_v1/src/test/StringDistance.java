package test;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.ArrayList;

import static test.VerifySignature.extractIdentity;

public class StringDistance {


    static ArrayList<String> getComponents(String orcidSeq, String plasmidIDSeq, String signatureSeq, String eccSeq,
                                           String originalSeq, String filecontent, String startTag, String endTag) {
        ArrayList<String> components = new ArrayList<>();

        boolean isRevComp = false;
        boolean isNormal = false;
        String filecontentRevComp = VerifySignature.generateReverseComplement(filecontent);

        if (filecontent.contains(startTag) || filecontent.contains(endTag)) {
            isNormal = true;
        }

        if (filecontentRevComp.contains(startTag) || filecontentRevComp.contains(endTag)) {
            isRevComp = true;
        }

        if (isRevComp || isNormal) {
            if (isNormal) {
                String repeatMsg = filecontent.concat(filecontent).concat(filecontent).trim();
                if (repeatMsg.indexOf(startTag) != repeatMsg.lastIndexOf(startTag)) {

                    String temp = StringUtils.substringBetween(repeatMsg, startTag, startTag);
                    orcidSeq = temp.substring(0, 32);
                    plasmidIDSeq = temp.substring(32, 44);
                    signatureSeq = temp.substring(44, 556);
                    eccSeq = StringUtils.substringBetween(temp, signatureSeq, endTag);
                    originalSeq = StringUtils.substringAfterLast(temp, endTag);
                } else {
                    JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , ONLY ONE INSTANCE OF START IN COMBINED MSG",
                            "alert", JOptionPane.ERROR_MESSAGE);
                }
            } else if (isRevComp) {
                String repeatMsg = filecontentRevComp.concat(filecontentRevComp).concat(filecontentRevComp).trim();
                if (repeatMsg.indexOf(startTag) != repeatMsg.lastIndexOf(startTag)) {

                    String temp = StringUtils.substringBetween(repeatMsg, startTag, startTag);
                    orcidSeq = temp.substring(0, 32);
                    plasmidIDSeq = temp.substring(32, 44);
                    signatureSeq = temp.substring(44, 556);
                    eccSeq = StringUtils.substringBetween(temp, signatureSeq, endTag);
                    originalSeq = StringUtils.substringAfterLast(temp, endTag);
                } else {
                    JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , ONLY ONE INSTANCE OF START IN COMBINED MSG",
                            "alert", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (!orcidSeq.isEmpty() && !plasmidIDSeq.isEmpty() && !signatureSeq.isEmpty() && !originalSeq.isEmpty()) {
                String identity = extractIdentity(orcidSeq);
                String plasmidID = VerifySignature.extractPlasmidID(plasmidIDSeq);

                System.out.println("Identity = " + identity);
                System.out.println("PLASMID ID = " + plasmidID);
                System.out.println("Signature = " + signatureSeq);
                System.out.println("Signature length = " + signatureSeq.length());
                if (eccSeq != null && !eccSeq.trim().isEmpty()) {
                    System.out.println("ECC = " + eccSeq);
                    System.out.println("ECC LENGTH = " + eccSeq.length());

                }


            }
        }
        return components;
    }
}

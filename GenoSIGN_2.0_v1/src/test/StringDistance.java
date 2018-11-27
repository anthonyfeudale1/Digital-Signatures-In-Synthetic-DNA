

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import static VerifySignature.extractIdentity;

public class StringDistance {


    static ArrayList<String> getComponents(String orcidSeq, String plasmidIDSeq, String signatureSeq, String eccSeq,
                                           String originalSeq, String filecontent, String startTag, String endTag) {
        ArrayList<String> components = new ArrayList<>();

        boolean isRevComp = false;
        boolean isNormal = false;
        boolean normEndTagMissing= false;
        boolean normStartTagMissing=false;
        boolean normStEndTagMissing= false;
        boolean revComEndTagMissing= false;
        boolean revComStatTagMissing=false;
        boolean revComStEndTagMissing=false;
        String filecontentRevComp = VerifySignature.generateReverseComplement(filecontent);
       //case 1
       if (filecontent.contains(startTag) && filecontent.contains(endTag)) {
    	  isNormal = true;
    	          
        }
        //case 2
        else if (filecontentRevComp.contains(startTag) && filecontentRevComp.contains(endTag)) {
            isRevComp = true;
       }
        //case 3
        //if normal file contains Start Tag and Not End tag
        //Then End Tag is probably mutated
        else if (filecontent.contains(startTag) && !filecontent.contains(endTag)) {
        	normEndTagMissing= true;
          	 
        }//end of case 3
        
        //case 4
        //if normal file does not contain start tag and contains only end tag
        //Then start tag  is probably mutated
        else if(!filecontent.contains(startTag) && filecontent.contains(endTag)) {
        	normStartTagMissing= true;
        	
        }//End of CAse 4
        
        //case 5
        //if normal file does not contain both start and end tags
        //both probably are mutated
        else if(!filecontent.contains(startTag) && !filecontent.contains(endTag)) {
        	//Find the mutations for start tag and End Tag
        	normStEndTagMissing= true;
        	
        }
        //case 6
      //If reverse compliment contains start tag and not end tag
        //End tag is mutated 
        else if(filecontentRevComp.contains(startTag) && !filecontentRevComp.contains(endTag)) {
        	revComEndTagMissing= true;
      
          } // End of case 6
        
        //case 7
        else if (!filecontentRevComp.contains(startTag) && filecontentRevComp.contains(endTag)) {
        	revComStatTagMissing= true;
        } //End of case 7
       
        //case 8
        else if (!filecontentRevComp.contains(startTag) && !filecontentRevComp.contains(endTag)) {
            //Find mutations in Start Tag
        	revComStEndTagMissing=true;
        }       
        
                      
        if (isNormal || isRevComp||normEndTagMissing ||normStartTagMissing||normStEndTagMissing ||revComEndTagMissing||
        		revComStatTagMissing || revComStEndTagMissing ) {
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
            } //isNr
            else if (isRevComp) {
                String repeatMsg = filecontentRevComp.concat(filecontentRevComp).concat(filecontentRevComp).trim();
                if (repeatMsg.indexOf(startTag) != repeatMsg.lastIndexOf(startTag)) {

                    String temp = StringUtils.substringBetween(repeatMsg, startTag, endTag);
                    orcidSeq = temp.substring(0, 32);
                    plasmidIDSeq = temp.substring(32, 44);
                    signatureSeq = temp.substring(44, 556);
                    eccSeq = StringUtils.substringBetween(temp, signatureSeq, endTag);
                    originalSeq = StringUtils.substringAfterLast(temp, endTag);
                } else {
                    JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , ONLY ONE INSTANCE OF START IN COMBINED MSG",
                            "alert", JOptionPane.ERROR_MESSAGE);
                }
            }//end if rev
            //normEndTag missing: Case 3
            else if(normEndTagMissing) {
            	  //Find the mutations for end tag
            	//Group filecontent in to array of 10 bytes.
            	ArrayList<String>  gene_sequence = FastaParsing.gene_sequence(filecontent);
            	//Run String optimal Algorithm to find mutations in End Tag
            	int indexStartTag, indexEndTag;
            	boolean found= false;
              	//Find the index of Start Tag
            	indexStartTag=filecontent.indexOf(startTag);
            	List<Pair>mutatedEndTag= new ArrayList<Pair>();
            	 for (int i = 0; i <gene_sequence.size() ; ++i) {
             	    
             		int Distance=OptimalStringAlignment.editDistance(endTag, gene_sequence.get(i), 3);
                 	if(Distance!=-1) {
                 	    Pair pair=new Pair(Distance,gene_sequence.get(i));
                 	   mutatedEndTag.add(pair);
              	        pair=null;
                 	}     
            	 }//end of for
            	 
            	 Collections.sort(mutatedEndTag,new MyComparator()); 
            	 ///Find the distance between start tag and muend tag
            	 //TODO: What if multiple End tags with distance is grater than 512 bytes??
            	 for(int i=0;i<mutatedEndTag.size();i++) {
            		 String tempEndTag=mutatedEndTag.get(i).getString();
            		 indexEndTag=filecontent.indexOf(tempEndTag);
            		 if((indexEndTag-indexStartTag)>512) {
            			 found=true;
            			 components= extractData(filecontent,startTag,tempEndTag );
            			 break;
            		 }
            		 
            	 }//end for
            	 if(false==found) {
            		 JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , INVALID END TAG",
                             "alert", JOptionPane.ERROR_MESSAGE);
            	 }
            
            	
            } //end of case 3: normEnd tag
            //Normal Start Tag Missing: Case 4
            else if(normStartTagMissing) {
            //Find the mutations for start tag
          	  //Group filecontent in to array of 10 bytes.
            ArrayList<String>  gene_sequence = FastaParsing.gene_sequence(filecontent);
          	//Run String optimal Algorithm to find mutations in End Tag
          	int indexStartTag, indexEndTag;
          	boolean found=false;
          
          	//Find the index of End  Tag
          	indexEndTag=filecontent.indexOf(endTag);
          	List<Pair>mutatedStartTag= new ArrayList<Pair>();
          	 for (int i = 0; i <gene_sequence.size() ; ++i) {
           	    
           		int Distance=OptimalStringAlignment.editDistance(startTag, gene_sequence.get(i), 3);
               	if(Distance!=-1) {
               	    Pair pair=new Pair(Distance,gene_sequence.get(i));
               	   mutatedStartTag.add(pair);
            	        pair=null;
               	}     
          	 }//end of for
          	 
          	 Collections.sort(mutatedStartTag,new MyComparator()); 
          	 ///Find the distance between start tag and muend tag
          	 for(int i=0;i<mutatedStartTag.size();i++) {
          		 String tempStartTag=mutatedStartTag.get(i).getString();
          		 indexStartTag=filecontent.indexOf(tempStartTag);
          		 if((indexEndTag-indexStartTag)>512) {
          			 found=true;
          			 components= extractData(filecontent,tempStartTag,endTag );
          			 break;
          		 }
          		 
          	 }//end for
          	 if(false==found) {
          		 JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , INVALID START TAG",
                           "alert", JOptionPane.ERROR_MESSAGE);
          	 }
            	
            } //end of case 4
            
            //case 5: Normal StartTag and End Tag both missing
            else if(normStEndTagMissing) {
            	//Find the mutations for start tag
            	//Group filecontent in to array of 10 bytes.
              ArrayList<String>  gene_sequence = FastaParsing.gene_sequence(filecontent);
            	//Run String optimal Algorithm to find mutations in End Tag
            	int indexStartTag, indexEndTag;
            	boolean found=false;
            
            	           	
            	List<Pair>mutatedStartTag= new ArrayList<Pair>();
            	List<Pair>mutatedEndTag=new ArrayList<Pair>();
            	//Find the mutations for start tag
            	 for (int i = 0; i <gene_sequence.size() ; ++i) {
             	    
             		int Distance=OptimalStringAlignment.editDistance(startTag, gene_sequence.get(i), 3);
                 	if(Distance!=-1) {
                 	    Pair pair=new Pair(Distance,gene_sequence.get(i));
                 	   mutatedStartTag.add(pair);
              	        pair=null;
                 	}     
            	 }//end of for
            	 
            	 //Find the mutations for end tag
            	//Find the mutations for start tag
            	 for (int i = 0; i <gene_sequence.size() ; ++i) {
             	    
             		int Distance=OptimalStringAlignment.editDistance(endTag, gene_sequence.get(i), 3);
                 	if(Distance!=-1) {
                 	    Pair pair=new Pair(Distance,gene_sequence.get(i));
                 	   mutatedEndTag.add(pair);
              	        pair=null;
                 	}     
            	 }//end of for
            	 
            	 Collections.sort(mutatedStartTag,new MyComparator()); 
            	 Collections.sort(mutatedEndTag,new MyComparator()); 
            	 
            	 int length= Math.min(mutatedStartTag.size(), mutatedEndTag.size());
            	 
            	 ///Find the distance between start tag and muend tag
            	 for(int i=0;i<length;i++) {
            		 String tempStartTag=mutatedStartTag.get(i).getString();
            		 String tempEndTag=mutatedEndTag.get(i).getString();
            		 indexStartTag=filecontent.indexOf(tempStartTag);
            		 indexEndTag=filecontent.indexOf(tempEndTag);
            		 if((indexEndTag-indexStartTag)>512) {
            			 found=true;
            			 components= extractData(filecontent,tempStartTag,tempEndTag );
            			 break;
            		 }
            		 
            	 }//end for
            	 if(false==found) {
            		 JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , INVALID START TAG",
                             "alert", JOptionPane.ERROR_MESSAGE);
            	 }
              	
            	//TODO Add the code here
            } //end of case 5
            
            //case 6: RevComp start tag is missing
            else if(revComEndTagMissing) {
                //Find mutations in End Tag
            	ArrayList<String>  gene_sequence = FastaParsing.gene_sequence(filecontentRevComp);
            	int indexStartTag, indexEndTag;
            	boolean found=false;
                /*	String repeatMsg = //filecontent.concat(filecontent).concat(filecontent).trim();
                	//indexStartTag=repeatMsg.indexOf(startTag);*/
                	//Find the index of Start Tag
                	indexStartTag=filecontentRevComp.indexOf(startTag);
                	List<Pair>mutatedEndTag= new ArrayList<Pair>();
                	 for (int i = 0; i <gene_sequence.size() ; ++i) {
                 	    
                 		int Distance=OptimalStringAlignment.editDistance(endTag, gene_sequence.get(i), 3);
                     	if(Distance!=-1) {
                     	    Pair pair=new Pair(Distance,gene_sequence.get(i));
                     	   mutatedEndTag.add(pair);
                  	        pair=null;
                     	}     
                	 }//end of for
                	 
                	 Collections.sort(mutatedEndTag,new MyComparator()); 
                	 ///Find the distance between start tag and muend tag
                	 for(int i=0;i<mutatedEndTag.size();i++) {
                		 String tempEndTag=mutatedEndTag.get(i).getString();
                		 indexEndTag=filecontentRevComp.indexOf(tempEndTag);
                		 if((indexEndTag-indexStartTag)>512) {
                			 found=true;
                			 components= extractData(filecontentRevComp,startTag,tempEndTag );
                			 break;
                		 }
                	 }//End of far 
                	 if(false==found) {
                		 JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , INVALID START TAG",
                                 "alert", JOptionPane.ERROR_MESSAGE);
                	 }
            	
            } //end of case 6
            
            //case 7: revComp Start Tag missing
            else if(revComStatTagMissing) {
            	//Find mutations in Start Tag
                //Find mutations in End Tag
            	ArrayList<String>  gene_sequence = FastaParsing.gene_sequence(filecontentRevComp);
            	int indexStartTag, indexEndTag;
            	boolean found=false;
                /*	String repeatMsg = //filecontent.concat(filecontent).concat(filecontent).trim();
                	//indexStartTag=repeatMsg.indexOf(startTag);*/
                	//Find the index of Start Tag
            	indexEndTag=filecontentRevComp.indexOf(endTag);
                	List<Pair>mutatedStartTag= new ArrayList<Pair>();
                	 for (int i = 0; i <gene_sequence.size() ; ++i) {
                 	    
                 		int Distance=OptimalStringAlignment.editDistance(startTag, gene_sequence.get(i), 3);
                     	if(Distance!=-1) {
                     	    Pair pair=new Pair(Distance,gene_sequence.get(i));
                     	   mutatedStartTag.add(pair);
                  	        pair=null;
                     	}     
                	 }//end of for
                	 
                	 Collections.sort(mutatedStartTag,new MyComparator()); 
                	 ///Find the distance between start tag and muend tag
                	 for(int i=0;i<mutatedStartTag.size();i++) {
                		 String tempStartTag=mutatedStartTag.get(i).getString();
                		 indexStartTag=filecontentRevComp.indexOf(tempStartTag);
                		 if((indexEndTag-indexStartTag)>512) {
                			 found=true;
                			 components= extractData(filecontentRevComp,startTag,endTag );
                			 break;
                		 }
                	 }//End of far 
                	 if(false==found) {
                		 JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , INVALID START TAG",
                                 "alert", JOptionPane.ERROR_MESSAGE);
                	 }            	   
            } //end of case 7
            
            //case 8
            else if(revComStEndTagMissing) {
            	//Find the mutations for start tag
            	//Group filecontent in to array of 10 bytes.
              ArrayList<String>  gene_sequence = FastaParsing.gene_sequence(filecontentRevComp);
            	//Run String optimal Algorithm to find mutations in End Tag
            	int indexStartTag, indexEndTag;
            	boolean found=false;
            
            	           	
            	List<Pair>mutatedStartTag= new ArrayList<Pair>();
            	List<Pair>mutatedEndTag=new ArrayList<Pair>();
            	//Find the mutations for start tag
            	 for (int i = 0; i <gene_sequence.size() ; ++i) {
             	    
             		int Distance=OptimalStringAlignment.editDistance(startTag, gene_sequence.get(i), 3);
                 	if(Distance!=-1) {
                 	    Pair pair=new Pair(Distance,gene_sequence.get(i));
                 	   mutatedStartTag.add(pair);
              	        pair=null;
                 	}     
            	 }//end of for
            	 
            	 //Find the mutations for end tag
            	//Find the mutations for start tag
            	 for (int i = 0; i <gene_sequence.size() ; ++i) {
             	    
             		int Distance=OptimalStringAlignment.editDistance(endTag, gene_sequence.get(i), 3);
                 	if(Distance!=-1) {
                 	    Pair pair=new Pair(Distance,gene_sequence.get(i));
                 	   mutatedEndTag.add(pair);
              	        pair=null;
                 	}     
            	 }//end of for
            	 
            	 Collections.sort(mutatedStartTag,new MyComparator()); 
            	 Collections.sort(mutatedEndTag,new MyComparator()); 
            	 
            	 int length= Math.min(mutatedStartTag.size(), mutatedEndTag.size());
            	 
            	 ///Find the distance between start tag and muend tag
            	 for(int i=0;i<length;i++) {
            		 String tempStartTag=mutatedStartTag.get(i).getString();
            		 String tempEndTag=mutatedEndTag.get(i).getString();
            		 indexStartTag=filecontentRevComp.indexOf(tempStartTag);
            		 indexEndTag=filecontentRevComp.indexOf(tempEndTag);
            		 if((indexEndTag-indexStartTag)>512) {
            			 found=true;
            			 components= extractData(filecontentRevComp,tempStartTag,tempEndTag );
            			 break;
            		 }
            		 
            	 }//end for
            	 if(false==found) {
            		 JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , INVALID START TAG",
                             "alert", JOptionPane.ERROR_MESSAGE);
            	 }
              	
 
            }

          /*  if (!orcidSeq.isEmpty() && !plasmidIDSeq.isEmpty() && !signatureSeq.isEmpty() && !originalSeq.isEmpty()) {
                String identity = VerifySignature.extractIdentity(orcidSeq);
                String plasmidID = VerifySignature.extractPlasmidID(plasmidIDSeq);
                components.add(orcidSeq);
                components.add(plasmidIDSeq);
                components.add(signatureSeq);
                components.add(eccSeq);
                components.add(originalSeq);
                components.add(filecontent);
                System.out.println("Identityyyyy = " + identity);
                System.out.println("PLASMID ID = " + plasmidID);
                System.out.println("Signature = " + signatureSeq);
                System.out.println("Signature length = " + signatureSeq.length());
                if (eccSeq != null && !eccSeq.trim().isEmpty()) {
                    System.out.println("ECC = " + eccSeq);
                    System.out.println("ECC LENGTH = " + eccSeq.length());

                }*/


            }//end if normal or reverse
      //  }
        return components;
    }
    private static ArrayList<String> extractData(String filecontent, String startTag, String EndTag){
    	
    	String repeatMsg = filecontent.concat(filecontent).concat(filecontent).trim();
    	ArrayList<String> returnData= new ArrayList<String>() ;
        if (repeatMsg.indexOf(startTag) != repeatMsg.lastIndexOf(startTag)) {

            String temp = StringUtils.substringBetween(repeatMsg, startTag, startTag);
            String orcidSeq = temp.substring(0, 32);
            String plasmidIDSeq = temp.substring(32, 44);
            String signatureSeq = temp.substring(44, 556);
            String eccSeq = StringUtils.substringBetween(temp, signatureSeq, EndTag);
            String originalSeq = StringUtils.substringAfterLast(temp, EndTag);
            returnData.add(orcidSeq);
            returnData.add(plasmidIDSeq);
            returnData.add(signatureSeq);
            returnData.add(eccSeq);
            returnData.add(originalSeq);
            returnData.add(filecontent);
			return returnData;
            
        } else {
            JOptionPane.showMessageDialog(null, "CANNOT EXTRACT PARTS , ONLY ONE INSTANCE OF START IN COMBINED MSG",
                    "alert", JOptionPane.ERROR_MESSAGE);
            return null;
    	
    }
		
    }//ENd of Function
}

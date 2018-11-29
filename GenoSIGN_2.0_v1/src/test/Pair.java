package test;
import java.util.Comparator;

//import java.util.*
public class Pair  {
	
	    private int distance = 0;
	    private String Tag = new String();

	    public Pair(int dist, String tag)
	    {
	        this.distance = dist;
	        this.Tag = tag;
	    }

	    public Pair()
	    {

	    }

	    public int getDist() {
	        return distance;
	    }

	    public void setDist(int dist) {
	        this.distance = dist;
	    }

	    public String getString() {
	        return Tag;
	    }

	    public void setString(String tag) {
	        this.Tag = tag;
	    }


	}

class MyComparator implements Comparator<Pair>{
	  public int compare(Pair ob1, Pair ob2){
	   return ob1.getDist() - ob2.getDist() ;
	  }
	}



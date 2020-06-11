package org.jmq.dar.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.jmq.dar.A;
import org.jmq.dar.FWindow;
import org.jmq.dar.S;


/**
 * Classes and indexes of roomA
 */
public class UciReaderA extends UciReader implements ReaderInstance{

	public void filterAct(String id, long t0, long tN, List<A> ret){
		System.out.println("\t filter for:"+id);

	    if(id.equals("Snack"))
	    	ret.add(new A(t0,tN,"#A#1"));
	
	    else if(id.equals("Toileting"))
	    	ret.add(new A(t0,tN,"#A#2"));
	    
	    else if(id.equals("Showering"))
	    	ret.add(new A(t0,tN,"#A#3"));
		
	    else if(id.equals("Breakfast"))
	    	ret.add(new A(t0,tN,"#A#4"));


	    else if(id.equals("Lunch"))
	    	ret.add(new A(t0,tN,"#A#5"));
	
	    else if(id.equals("Grooming"))
	    	ret.add(new A(t0,tN,"#A#6"));
	    

	    else if(id.equals("Leaving"))
	    	ret.add(new A(t0,tN,"#A#7"));
	    
	    else if(id.equals("Spare_Time_TV"))
	    	ret.add(new A(t0,tN,"#A#8"));

	    else if(id.equals("Sleeping"))    	
	    	ret.add(new A(t0,tN,"#A#9"));
	    
	    else 
	    	System.exit(1);
	
	}
	
	
	public String getKey(String id) throws Exception{
		
	     if(id.equals("Snack"))
	    	return "#A#1";
	    
		    else if(id.equals("Toileting"))
		    	return "#A#2";
		
		    else if(id.equals("Showering"))
		    	return "#A#3";
		    
		    else if(id.equals("Breakfast"))
		    	return "#A#4";
		    else if(id.equals("Lunch"))
		    	return "#A#5";
		    
		    else if(id.equals("Grooming"))
		    	return "#A#6";



	     
		    else if(id.equals("Leaving"))
	    	return "#A#7";
		
	 	
		    else if(id.equals("Spare_Time_TV"))
		    	return "#A#8";
	
	
	    else if(id.equals("Sleeping"))
	    	return "#A#9";




	




		
		throw new Exception("Bad activity");
	}
	
	

	
	
	public int getNumber(String id) throws Exception{
		if(id.equals("#A#1"))
	    	return 1;
		
	    else if(id.equals("#A#2"))
	    	return 2;
	
	    else if(id.equals("#A#3"))
	    	return 3;
	
	
	    else if(id.equals("#A#4"))
	    	return 4;

	    else if(id.equals("#A#5"))
	    	return 5;


	    else if(id.equals("#A#6"))
	    	return 6;

	
	    else if(id.equals("#A#7"))
	    	return 7;
	
	    else if(id.equals("#A#8"))
	    	return 8;

	    else if(id.equals("#A#9"))
	    	return 9;
		
	    else if(id.equals("#A#0"))
	    	return 10;
		
		throw new Exception("Bad activity");
	}	
	

}

package org.jmq.dar.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jmq.dar.A;
import org.jmq.dar.FWindow;
import org.jmq.dar.S;

/**
 * 
 * Operations to handle Ordonez Dataset
 */
public abstract class  UciReader {
	public List<S> readAllData(String file ) throws Exception{

		List<S> ret=new LinkedList<S>();
		
		InputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		
		BufferedReader br = new BufferedReader(isr);
		String line=null;
		int count=0;

		 while ((line = br.readLine()) != null) {
			 	String [] codes=line.split("\t");
			 	if(skipHead(line)){
			 		long t0=D2long(codes[0]);
			 		long tN=D2long(codes[1]);
			 		if(tN<t0){

			 			
		 					
				 		
				 		long aux=tN;
				 		tN=t0;
				 		t0=aux;
			 		}
			 		

			 		String name="#S#"+codes[4]+"_"+codes[3]+"_"+codes[2];
		 			ret.add(new S(t0,tN,name));

		 			count++;
			 		}
		    }
		 br.close();
		return ret;
	}
	
	static SimpleDateFormat d = new SimpleDateFormat("dd/MM/yyyy");
	static public String long2Day(long day) throws ParseException{
		return d.format(day*1000L);
	}		
	static SimpleDateFormat d2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	public String long2Day2(long day) throws ParseException{
		return d2.format(day*1000L);
	}			
	static public String long2DayTime(long day) throws ParseException{
		return d2.format(day*1000L);
	}		
	public List<String> getDays(List<A>  actStream) throws ParseException{
		List<String> ret=new LinkedList<String>();
		for(A a:actStream){
			if(!ret.contains(long2Day(a.t0)))
				ret.add(long2Day(a.t0));
		}
		return ret;
	}
	

	public List<A> readAllAct(String file) throws Exception{

		LinkedList<A> ret=new LinkedList<A>();
		
		InputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		
		BufferedReader br = new BufferedReader(isr);
		String line=null;
		int count=0;
		long lastT=-1L;
		 while ((line = br.readLine()) != null) {
			 	String [] codes=line.split("\t");
			 	if(skipHead(line)){
			 		System.out.println(line);
			 		long t0=D2long(codes[0]);
			 		long tN=D2long(codes[1]);
			 		if(tN<t0){
			 			
			 			System.out.println("Error  1 in "+codes[0]+","+codes[1]);
			 			System.exit(1);
				 		System.out.println("#"+count);
				 		System.out.println(codes[0]+":"+t0);
				 		System.out.println(codes[1]+":"+tN);
				 		System.out.println(codes[2]);
				 		
				 		long aux=tN;
				 		tN=t0;
				 		t0=aux;
			 		}
			 		
			 		if(lastT!=-1L) {
		 				if(t0<lastT) {
		 					System.out.println("Error 2 in "+codes[0]+","+codes[1]);
		 				//	System.exit(1);
		 				}
		 			}
			 		lastT=tN;
			 		//ret.add(new V(t0,tN,codes[2]));
			 		filterAct(codes[2], t0, tN, ret);
			 		}
	 			count++;
		    }
//		 System.out.println("#"+count);
		 br.close();
		 ret.sort((A s1, A s2)->s1.value.compareTo(s2.value));
		return ret;
	}	
	static boolean begin=false;
	static public boolean skipHead(String line)throws Exception{
		if(line.startsWith("-")){
			begin=true;
			return false;
		}
		if(line.startsWith("Start time")){
			begin=false;
			return false;
		}
		return begin;
	}


	public String idleId(){
		return "#A#0";
	}

	abstract public void filterAct(String id, long t0, long tN, List<A> ret);
	
	
	abstract public String getKey(String id) throws Exception;
	
	

	
	abstract public int getNumber(String id) throws Exception;

	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static public long D2long(String day) throws ParseException{
		return df.parse(day).getTime()/1000L;
	}	
	static public String long2HHMM2(long t) throws ParseException{
		return df.format(t*1000L);
	}
	
	public String getIdSensorCommand(String s) {
		return s.substring(1);
	}	
}

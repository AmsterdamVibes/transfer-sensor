package org.jmq.dar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



/**
Activity class
 */
public class A {

	public long t0;
	public long tN;
	public String value;
	
	public A(long t0,long tN, String value){
		this.t0=t0;
		this.tN=tN;
		
		if(this.t0>this.tN){
			System.out.println("Error"+this.toString());
			System.exit(1);
			long aux=this.t0;
			this.t0=this.tN;
			this.tN=aux;
		}
		
		//this.t0+=(this.tN-this.t0)/2l;
		
		this.value=value;
	}


	@Override
	public String toString(){
		try {
			return "["+A.long2HHMM(t0)+"-"+A.long2HHMM(tN)+"]:"+value;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	static public HashMap<String, Integer> hAct=new HashMap<String, Integer> ();
	static public List<String> allAct=new LinkedList<String>();
	static public int calculateIndexOfAct(List<A> streamAct) throws Exception{
		hAct=new HashMap<String, Integer> ();
		allAct=new LinkedList<String>();
		for(A s:streamAct)
			if(!allAct.contains(s.value)){
				allAct.add(s.value);
			}
		Collections.sort(allAct);
		int i=0; 
		for(String s:allAct){
				System.out.println("\t a:"+s+"\t"+i);
				hAct.put(s, i);
				i++;
			}
		return i;
	}
	

	
	public float isOn(long t0, long tN) throws ParseException{
		//Se pasan de los limites de la ventana
		if(tN<this.t0) return 0f;
		if(this.tN<t0) return 0f;
		
		//Sensor dentro de la ventana
		if(t0<this.t0&& this.tN<tN) return 1f;

		//Ventana dentro del sesnor
		if(this.t0<=t0&& tN<=this.tN) return 1f;
		//Si hay cambio de true a false
		if(t0<this.tN&&this.tN<tN) return 1f;
		//Si hay cambio de false a true
		if(t0<=this.t0&&this.t0<tN) return 1f;
		return 0f;
		
	}
	// INDICE de la actvidad
	static public int indexOfhAct(String code) throws Exception{
//		System.out.println("Hpos:"+code);
		return hAct.get(code);
	}	
	
	static public int indexOfhAct2(String code){
		int i=0;
			for(String c:	hAct.keySet())
				if(c.equals(code))
					return i;
				else
					i++;
			return -1;
	}	
	

	
	static public int sA=7;
	
	static SimpleDateFormat hf = new SimpleDateFormat("HH");
	static public int long2H(long t) throws ParseException{
		return Integer.parseInt(hf.format(t*1000L));
	}	
	static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	static public long D2long(String day) throws ParseException{
		return df.parse(day).getTime()/1000L;
	}	
	static public String long2HHMM(long t) throws ParseException{
		return df.format(t*1000L);
	}	
	

	//Para ordenar las actividades
	static public class VComparator implements Comparator<A> {
	    @Override
	    public int compare(A o1, A o2) {
	        return (int)(o1.t0-o2.t0);
	    }
	}	
	

}

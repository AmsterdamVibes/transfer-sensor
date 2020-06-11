package org.jmq.dar;

import java.text.ParseException;

import org.jmq.dar.reader.UciReader;

public class FWindow {
	
	long l1;
	long u1;
	long u2;
	long l2;
	
	public FWindow(long l1, long u1, long u2, long l2){
		this.l1=l1;
		this.u1=u1;
		this.l2=l2;
		this.u2=u2;
	}
	
	public float getMembeship(long t0, long tN){
//	System.out.println("\t"+t0+"\t"+tN+"->\t"+l1+"\t"+u1+"\t"+u2+"\t"+l2);
		//		System.out.println("\t1");
		if(tN<=l1) return 0f;
		//		System.out.println("\t2");
		if(tN<=u1) return (float)(tN-l1)/(float)(u1-l1);
		//	System.out.println("\t3");
		if(t0<u2&&tN<=u2) return 1f;
		//	System.out.println("\t4");
		if(t0<u2) return 1f;
		//System.out.println("\t6");
		if(t0<l2) return (float)(l2-t0)/(float)(l2-u2);
		//System.out.println("\t7");
		if(t0>l1&&tN<l2) return 1f;
		//System.out.println("\t5");
		if(t0>=2) return 0f;
		//System.out.println("\t8");
		
		return 0f;
	}

	static public void main(String [] args) throws Exception{
		FWindow fw=new FWindow(1322472210L,1322472210L,1322472270L,1322472270L);
		System.out.println(fw.getMembeship(1322472104L, 1322472212L));
//		System.out.println(fw.getMembeship(-5, 0));
//		System.out.println(fw.getMembeship(-1, 1));
//		System.out.println(fw.getMembeship(-1, 2));
//		System.out.println(fw.getMembeship(0, 4));
//		System.out.println(fw.getMembeship(3, 4));
//		System.out.println(fw.getMembeship(4, 6));
//		System.out.println(fw.getMembeship(6, 8));
//		System.out.println(fw.getMembeship(7, 8));
//		System.out.println(fw.getMembeship(11, 12));
//		
//		FWindow fw2=new FWindow(-2,-2,10,10);
//		System.out.println(fw2.getMembeship(10, 12));
//		
		int i=1;
		for(;i<sTA;i++)
			System.out.println(fib(i)+"->"+fib(i+1)+"->"+fib(i+2)+"->"+fib(i+3));
		i=1;
		for(;i<sTB;i++)
			System.out.println(fib(i)+"->"+fib(i+1)+"->"+fib(i+2)+"->"+fib(i+3));
//		
		long t0=1322472270L;
		long tI=1322472104L;
		long tE=1322472212L;
		
		System.out.println("t0:"+UciReader.long2HHMM2(t0)+" -> ["+UciReader.long2HHMM2(tI)+","+UciReader.long2HHMM2(tE)+"]");
		for(FWindow f:getFTW_A(
				
				t0)) {
			//	System.out.println("\t:["+(f.l1)+","+(f.u1)+","+(f.u2)+","+(f.l2)+"]");
				System.out.println("\t:["+UciReader.long2HHMM2(f.l1)+","+UciReader.long2HHMM2(f.u1)+","+UciReader.long2HHMM2(f.u2)+","+UciReader.long2HHMM2(f.l2)+"]");
			
			System.out.println("\t:"+f.getMembeship(
					tI
					,
					tE
					));
		}
	}
	
	
   //DEFINITION OF FUZZY TEMPORAL WINDOWS

	static public long getIncT(){
		return 60L*1L;
	}
	
	static int fib(int n) 
    { 
    if (n <= 1) 
       return n; 
    return fib(n-1) + fib(n-2); 
    } 
	
	static public int sTA=4;
	static public FWindow [] getFTW_A(long t0){


		FWindow [] fw=new FWindow [sTA];
		fw[0]=new FWindow(t0-60L,t0-60L,t0,t0);
		int i=1;
		for(;i<sTA;i++)
		fw[i]=new FWindow(
				t0-60L*fib(i+3),
				t0-60L*fib(i+2),
				t0-60L*fib(i+1),
				t0-60L*fib(i+0));
	
		i--;
	//	System.out.println("Fib #"+fib(i+3));
		return fw;
	}		
	static public int sTB=4;
	static public FWindow [] getFTW_B(long t0){


		FWindow [] fw=new FWindow [sTB];
		fw[0]=new FWindow(t0,t0,t0+60L,t0+60L);
		int i=1;
		for(;i<sTB;i++)
		fw[i]=new FWindow(
				t0+60L*fib(i+0),
				t0+60L*fib(i+1),
				t0+60L*fib(i+2),
				t0+60L*fib(i+3));
	
	//	System.out.println("Fib #"+fib(i+3));
		return fw;
	}		
}

package org.jmq.dar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
/**
Class to eval a timeline file of an activity
 */
public class EvalTimeLine {
	static float sigmod(float x){
		return 1f/(1f+(float)Math.exp(-x));
	}
	
	
	static public List<Float> getFileColumnAct(String file, int pos) throws Exception{
		List<Float> ret=new LinkedList<Float>();
		
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
		    String line = null;

		    while ((line = br.readLine()) != null) {
		    	String [] codes=line.trim().split("\t");
		        ret.add(Float.parseFloat(codes[pos]));
		    }
		} finally {
		    br.close();
		}
		return ret;
	}
	
	static public List<String> getFileColumnActS(String file, int pos) throws Exception{
		List<String> ret=new LinkedList<String>();
		
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
		    String line = null;

		    while ((line = br.readLine()) != null) {
		    	String [] codes=line.trim().split("\t");
		        ret.add((codes[pos]));
		    }
		} finally {
		    br.close();
		}
		return ret;
	}

	
	static float max(List<Float> ps){
		float max=Float.MIN_VALUE;
		for(float f:ps)
			if(f>max)
				max=f;
		return max;
	}
	
	static float min(List<Float> ps){
		float min=Float.MAX_VALUE;
		for(float f:ps)
			if(f<min)
				min=f;
		return min;
	}
	
	static List<Float> alphacut(List<Float> ps, float alpha){
		List<Float> ps2=new LinkedList<Float>();
		for(float f:ps)
			if(f>alpha)
				ps2.add(1f);
			else
				ps2.add(0f);
		return ps2;
	}
	
	static List<Float> max(List<Float> ps1, List<Float> ps0){
		List<Float> ps2=new LinkedList<Float>();
		for(int t=0;t<ps1.size();t++)
			if(ps1.get(t)>ps0.get(t))
				ps2.add(1f);
			else
				ps2.add(0f);
		return ps2;
	}
	
	
	static List<Float> not(List<Float> ps){
		List<Float> ps2=new LinkedList<Float>();
		for(float f:ps)
			if(f==0)
				ps2.add(1f);
			else
				ps2.add(0f);
		return ps2;
	}

	static List<Float> alphacut_low(List<Float> ps, float alpha){
		List<Float> ps2=new LinkedList<Float>();
		for(float f:ps)
			if(f<alpha)
				ps2.add(1f);
			else
				ps2.add(0f);
		return ps2;
	}	
	
	static public class Eval{
		public int trues;
		public int all;
		Eval(){
			trues=0;
			all=0;
		}
		void add(int t){
			trues+=t;
			all++;
		}
		float score(){
			if(this.all==0) return 0;
			return (100f*(float)this.trues/(float)this.all);
		}

		public String toString(){
			return (100f*(float)this.trues/(float)this.all)+"\t"+this.all;
		}
		

		public String toString2(){
			return (100f*(float)this.trues/(float)this.all)+"";
		}		
	}
	
	static public Eval evalP(List<Float> one, List<Float> compare){
		Eval evalP=new Eval();
		boolean track=false;
		int t0=-1;
		int tN=-1;
		boolean oneIn=false;
		for(int t=0;t<compare.size();t++){
			if(compare.get(t)>0f){
				if(!track){
					t0=t;
					track=true;
					oneIn=false;
				}
			}else if(compare.get(t)==0f){
				if(track){
					tN=t;
					track=false;
					//System.out.println("\t track:["+t0+","+tN+"]");
				}
			}
			if(track){
				if(one.get(t)>0f){
					evalP.add(1);
					oneIn=true;
				}else{
					evalP.add(0);
				}
					
			}
		}
		return evalP;
	}
	
	static public Eval evalT(List<Float> one, List<Float> compare){
		Eval evalT=new Eval();
		boolean track=false;
		int t0=-1;
		int tN=-1;
		boolean oneIn=false;
		for(int t=0;t<compare.size();t++){
			if(compare.get(t)>0f){
				if(!track){
					t0=t;
					track=true;
					oneIn=false;
				}
			}else if(compare.get(t)==0f){
				if(track){
					tN=t;
					track=false;
					if(oneIn)
						evalT.add(1);
					else
						evalT.add(0);
				//	System.out.println("\t track:["+t0+","+tN+"]");
				}
			}
			if(track){
				if(one.get(t)>0f){
					oneIn=true;
				}else{
				}
					
			}
		}
		return evalT;		
	}	
	
	
	
	
	static public Eval evalT(List<Float> one, List<Float> compare, int minD){
		Eval evalT=new Eval();
		boolean track=false;
		int t0=-1;
		int tN=-1;
		boolean oneIn=false;
		for(int t=0;t<compare.size();t++){
			if(compare.get(t)>0f){
				if(!track){
					t0=t;
					track=true;
					oneIn=false;
				}
			}else if(compare.get(t)==0f){
				if(track){
					tN=t;
					track=false;
					if(oneIn)
						evalT.add(1);
					else
						evalT.add(0);
				//	System.out.println("\t track:["+t0+","+tN+"]");
				}
			}
			if(track){
				if(!oneIn&&minDistance(t,one)<minD){
					oneIn=true;
				}else{
				}
					
			}
		}
		return evalT;		
	}	
	static public int minDistance(int t0, List<Float> one){
		
		int dist=Integer.MAX_VALUE;
		for(int t=0;t<one.size();t++)
			if(one.get(t)>0f&&Math.abs(t0-t)<dist)
				dist=Math.abs(t0-t);
		
		return dist;
	}
	
	static public List<Float> offT(List<Float> source , int offT){
		List<Float> ret=new LinkedList<Float>();
		for(int t=0;t<source.size();t++)
			ret.add(0f);
		boolean track=false;
		int t0=-1;
		int tN=-1;
		for(int t=0;t<source.size();t++){
			if(source.get(t)>0f){
				if(!track){
					t0=t;
					track=true;
					for(int t2=0;t2<=offT;t2++)
						if(t-t2>=0)
							ret.set(t-t2, 1f);
				}
			}else if(source.get(t)==0f){
				if(track){
					tN=t;
					track=false;
					for(int t2=0;t2<=offT;t2++)
						if(t+t2<source.size())
							ret.set(t+t2, 1f);
				//	System.out.println("\t track:["+t0+","+tN+"]");
				}
			}
			if(track){
				ret.set(t, 1f);
				}
					
			
		}
	//	for(int t=0;t<source.size();t++)
	//		System.out.println("\t"+ret.get(t)+"\t"+source.get(t));

		return ret;		
		
	}	
	static public List<String> getFileColumnAct(String file) throws Exception{
		List<String> ret=new LinkedList<String>();
		
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
		    String line = null;

		    while ((line = br.readLine()) != null) {
		        ret.add(line.trim().equals("Act")?"1":"0");
		    }
		} finally {
		    br.close();
		}
		return ret;
	}		
	static public List<String> getFileColumns(String file) throws Exception{
		List<String> ret=new LinkedList<String>();
		
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
		    String line = null;

		    while ((line = br.readLine()) != null) {
		        ret.add(line.trim());
		    }
		} finally {
		    br.close();
		}
		return ret;
	}	
}

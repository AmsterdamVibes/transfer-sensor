package org.jmq.dar;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jmq.dar.reader.UciReader;

public class MainEval2 {
	
	static public void main(String [] args) throws Exception{
		System.out.println("Day 0");
		
		History historyA=new History();
		historyA.readFile("./roomB_1/TimeRawJoined.tsv");
		
		History historyA_1=new History();
		historyA_1.readFile("./roomB_1/results/TimeLine.csv");	
		List<Long> ts=new LinkedList<Long>(historyA.getTimeLine());
		List<Long> ts2=new LinkedList<Long>(historyA_1.getTimeLine());
		System.out.println(ts.size()+"vs"+ts2.size());
		
		float p=0;
		float r=0;
		float t=0;
		int L=0;
		for(int c:historyA.getCols(Arrays.asList("#S#"))) {
	//		if(c!=1+9) continue;
			String col=historyA.cols.get(c);
			float p1=computeSim(col,ts,historyA,historyA_1,L)*100f;
			float r1=computeSim(col,ts,historyA_1,historyA,L)*100f;
			System.out.println(col+"\t"+p1+"\t"+r1);
			p+=p1;
			r+=r1;
			t++;			
		//	break;
		}
		System.out.println("TOTAL:\t"+p/t+"\t"+r/t+"\t"+(2*(p/t)*(r/t))/((p/t)+(r/t)));
	}

	
	static public float computeSim(String col, Collection<Long> ts, History history1, History history2, int minOff) throws Exception{
		int sim=0;
		int total=0;
		
		int indexCol1=history1.getCol1(col);
		int indexCol2=history2.getCol1(col);
		System.out.println("col1:"+indexCol1);
		System.out.println("col2:"+indexCol2);
		for(long t:ts) {
			if(history1.data.get(t)==null) {
				System.out.println("Error 1:"+History.long2D(t));
			}
			if(history2.data.get(t)==null) {
				System.out.println("Error 2:"+History.long2D(t)+",t:"+t);
			}
			if(history2.data.get(t).data.get(indexCol2)>0) {
				if(closerOn(t, indexCol1, history1, ts)<=minOff)
					sim++;
				else{
					System.out.println("Error L:"+col+" t:"+History.long2D(t)+",t:"+t);
		//			System.exit(1);
				}
				total++;
			}
		}
		return (float)sim/(float)total;
	}
	
	static public int closerOn(long t, int col, History history2, Collection<Long> ts) throws Exception{
		int distMin=Integer.MAX_VALUE;
		for(long t2:history2.getTimeLine()) {
			if(history2.data.get(t2)==null) {
				System.out.println("Error 3:"+History.long2D(t)+",t:"+t);
			}
			if(history2.data.get(t2).data.get(col)>0 && (int)(Math.abs(t-t2)/60L)<distMin)
				distMin=(int)(Math.abs(t-t2)/60L);
				if(distMin==0) return 0;
		}
//		if("02/12/2011 12:38:30".equals(History.long2D(t))) {
//			System.out.println("\t"+history2.cols.get(col)+" #"+A.long2HHMM(t)+"->"+distMin);
//			System.in.read();
//		}
		return distMin;
	}
	
}

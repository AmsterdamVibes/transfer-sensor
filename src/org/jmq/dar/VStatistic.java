package org.jmq.dar;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * Statistics of activities
 */
public class VStatistic {
		public float minV;
		public float maxV;
		public float avgV;
		public float desV;
		public int size;
		public float w;
		String label;
		public VStatistic(String label){
			this.label=label;
			this.minV=Float.MAX_VALUE;
			this.maxV=Float.MIN_VALUE;
			this.avgV=0;
			this.desV=0;
			this.size=0;
		}		
		@Override
		public String toString(){
			return label+":["+"max:"+maxV+"min:"+minV+"avg:"+avgV+"des:"+desV+"#"+size+"]";
		}		
		
		
		static public List<String> addList(List<String> init, String adds){
			for(String s0:adds.split("\t"))
				init.add(s0);
			return init;
		}
		
		
		
		
		static public VStatistic getStatistic(String label, List<VStatistic>  statisticList){
			for(VStatistic st:statisticList)
				if(st.label.equals(label))
					return st;
			return null;
		}


			
		static public List<VStatistic> getStatisticByActivity(List<A> streamAct){
			HashMap<String,List<Long>> durationHash=new HashMap<String,List<Long>>();
			
			for(A a:streamAct){
				
				
				if(durationHash.containsKey(a.value)){
					durationHash.get(a.value).add(a.tN-a.t0);
				}
				else{
					List<Long> l=new LinkedList<Long>();
					l.add(a.tN-a.t0);
					durationHash.put(a.value,l);
				}
			}
			List<VStatistic> statisticList=new LinkedList<VStatistic>();
			for (Map.Entry<String, List<Long>> entry : durationHash.entrySet()) {
				VStatistic statistic=new VStatistic(entry.getKey());
			    for(Long d:entry.getValue()){
			    	statistic.maxV=Math.max(statistic.maxV,d);
			    	statistic.minV=Math.min(statistic.minV,d);
			    	statistic.size++;
			    	statistic.avgV+=d;
			    }
			    statistic.avgV/=(float)statistic.size;
			    for(Long d:entry.getValue())
			    	statistic.desV+=(d- statistic.avgV)*(d- statistic.avgV);
			    statistic.desV/=(float)statistic.size;
			    statistic.desV=(float)Math.sqrt(statistic.desV);
			    
			    statisticList.add(statistic);
			}
			
			Collections.sort(statisticList, new Comparator<VStatistic>() {
			    @Override
			    public int compare(VStatistic o1, VStatistic o2) {
			        return o1.label.compareTo(o2.label);
			    }
			});			
			return statisticList;
		}
				
}
package org.jmq.dar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.jmq.dar.reader.UciReader;

public class ActClusterTime {
	
	static public class ATime{
		public long t;
		public long t0;
		public long tN;
		public float w;
		public ATime(long t, long t0,long tN) {
			this.t=t;
			this.t0=t0;
			this.tN=tN;
			this.w=this.getW();
		}
		
		public float getW() {
			if(t<t0) return 0;
			if(t>tN) return 1;
			if(tN==t0) {
				if(t0==t) return 1;
				return 0;
			}
			return (float)(t-tN)/(float)(t0-tN);
		}

		public float getW2() {
			System.out.println("0");
			if(t<t0) return 0;
			System.out.println("1");
			if(t>tN) return 1;
			System.out.println("2");

			if(tN==t0) {
				if(t0==t) return 1;
				return 0;
			}
			System.out.println("3");

			return (float)(t-tN)/(float)(t0-tN);
		}

		@Override
		public String toString() {
			try {
				return "ATime [t=" + A.long2HHMM(t) + ", t0=" + A.long2HHMM(t0) + ", tN=" + A.long2HHMM(tN) + ", w=" + w + "]";
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		
		
	}
/**	
	static public Pair<ATime,ATime> getRandomTime(ActClusterTime ct1, ClusterId ci1, ActClusterTime ct2, ClusterId ci2) {
		List<ATime> ts1=ct1.times.get(ci1);
		List<ATime> ts2=ct2.times.get(ci2);
		float incW=0;
		while(true) {
			
			ATime at1=ts1.get(Control.random.nextInt(ts1.size()));
			ATime at2=ts2.get(Control.random.nextInt(ts2.size()));
			if(Math.abs(at1.w-at2.w)<incW)
				return new Pair<ATime,ATime>(at1,at2);
			incW+=0.01f;			
		}
		//return ts.get(Control.random.nextInt(ts.size()));
		
	}
*/	
	TreeMap<ClusterId, List<ATime>> times = new TreeMap<ClusterId,List<ATime>>(
			new Comparator<ClusterId>() {
				@Override
				public int compare(ClusterId o1, ClusterId o2) {
					return Comparator.comparing(ClusterId::getAct)
				              .thenComparing(ClusterId::getId)				        
				              .compare(o1, o2);
				}
				});
	
	
	TreeMap<ClusterId, ClusterIdData> centers = new TreeMap<ClusterId,ClusterIdData>(
			new Comparator<ClusterId>() {
				@Override
				public int compare(ClusterId o1, ClusterId o2) {
					return Comparator.comparing(ClusterId::getAct)
				              .thenComparing(ClusterId::getId)				        
				              .compare(o1, o2);
				}
				});
	public List<ClusterId> getClusters(){
		return this.clusters;
	}
	public List<ClusterId> getClustersByActivity(String act){
		List<ClusterId> ret=new LinkedList<ClusterId>();
		for(ClusterId ci:this.getClusters())
			if(act.equals(ci.act))
				ret.add(ci);
		return ret;
	}
	
	public List<ClusterId> getClustersByIndexSensorGTE(int indexSensor, float alpha){
		List<ClusterId> ret=new LinkedList<ClusterId>();
		for(ClusterId ci:this.getClusters()) {
			if(this.centers.get(ci).isGTE(indexSensor, alpha))
				ret.add(ci);
		}
		return ret;
	}
	public List<ClusterId> getClustersByIndexSensorLTE(int indexSensor, String act, float alpha){
		List<ClusterId> ret=new LinkedList<ClusterId>();
		for(ClusterId ci:this.getClusters()) {
			if(act.equals(ci.act))
				if(this.centers.get(ci).isLTE(indexSensor, alpha))
					ret.add(ci);
		}
		return ret;
	}	
	public ClusterId getClustersByIndex(int index){		
		return getClusters().get(index);
	}
	public int getIndexClustersByActivity(ClusterId ci0){
		int i=0;
		for(ClusterId ci:this.getClusters())
			if(ci.equals(ci0))
				return i;
			else
				i++;
		return -1;
	}
	

	public ActClusterTime(String room, int day) throws Exception{
		String file=Param.Dataset.path_res(room)+day+"/train/TimeActivity.tsv";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		String line=null;

		 while ((line = br.readLine()) != null) {
			 
			 String [] codes=line.trim().split("\t");
			 System.out.println(Arrays.toString(codes));
			 ClusterId cluster=new ClusterId(codes[1],Integer.parseInt(codes[2]));
			 
			 if(!times.containsKey(cluster))
				 times.put(cluster, new LinkedList<ATime>());
			 
			
			 times.get(cluster).add(new ATime(UciReader.D2long(codes[0]),UciReader.D2long(codes[4]),UciReader.D2long(codes[5])));
			 

		 }
		 br.close();

		 
		 this.clusters=new LinkedList<ClusterId>(this.times.keySet());
		 for(ClusterId cid:clusters)
			 this.addClusterCenter(room, day, cid);
	}
	
	
	private void addClusterCenter(String room, int day, ClusterId cid) throws Exception{
		String file=Param.Dataset.path_res(room)+day+"/train/clusters/"+cid.act+".cluster.w.tsv";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		String line=null;

		int countCluster=0;
		 while ((line = br.readLine()) != null) {
			 
			 if(countCluster!= cid.id) {
				 countCluster++;
				 continue;
			 }
			 String [] codes=line.trim().split("\t");
			 System.out.println(Arrays.toString(codes));
			 float w=Float.parseFloat(codes[0].replaceAll("#",""));
			 float [] centers=new float[codes.length-1];
			for(int j=1;j<codes.length;j++)
				centers[j-1]=Float.parseFloat(codes[j]);
			this.centers.put(cid, new ClusterIdData(cid,centers,w));
			 System.out.println("reading from ["+cid.act+","+cid.id+"]:"+Arrays.toString(centers));
			break;
		 }
		 br.close();
		
	}
	
	LinkedList<ClusterId> clusters;
	
	static public class ClusterId{
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((act == null) ? 0 : act.hashCode());
			result = prime * result + id;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClusterId other = (ClusterId) obj;
			if (act == null) {
				if (other.act != null)
					return false;
			} else if (!act.equals(other.act))
				return false;
			if (id != other.id)
				return false;
			return true;
		}
		public String act;
		public int id;
		public ClusterId(String act, int id){
			this.act=act;
			this.id=id;
		}
		
		@Override
		public String toString() {
			return "ClusterId [act=" + act + ", id=" + id + "]";
		}
		public String getAct() {
			return this.act;
		}
		public int getId() {
			return this.id;
		}
	}
	
	static public class ClusterIdData extends ClusterId{
		float [] cluster;
		float w;
		public ClusterIdData(ClusterId id, float [] cluster, float w) {
			super(id.act, id.id);
			this.cluster=cluster;
			this.w=w;
		}
		
		public boolean isGTE(int indexSensor, float alpha) {
			return this.cluster[indexSensor]>=alpha;
		}
		public boolean isLTE(int indexSensor, float alpha) {
			return this.cluster[indexSensor]<=alpha;
		}

		@Override
		public String toString() {
			return "ClusterIdData [cluster=" + Arrays.toString(cluster) + ", w=" + w + ", act=" + act + ", id=" + id
					+ "]";
		}	
		
		
	}
}

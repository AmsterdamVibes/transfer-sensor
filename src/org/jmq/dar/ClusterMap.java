package org.jmq.dar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ClusterMap {

	HashMap<ClusterCenter, Float> map=new HashMap<ClusterCenter, Float>();
	int countCluster=0;
	
	static public class ClusterCenter{
		public float [] data;


		ClusterCenter(float [] data){
			this.data=data;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(data);
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
			ClusterCenter other = (ClusterCenter) obj;
			if (!Arrays.equals(data, other.data))
				return false;
			return true;
		}
		
	}
	public int nSensor;
	public String act;
	public int day;
	public String room;
	ClusterMap(String room, int day, String act,int nSensor) throws Exception{
		this.room=room;
		this.act=act;
		this.nSensor=nSensor;
		this.day=day;
		File folder=new File(Param.Dataset.path_res(room)+day+"/train/clusters/"+act+"/");
		
		for (String pathname : folder.list()) {
            // Print the names of files and directories
            System.out.println(pathname);
            loadMap(Param.Dataset.path_res(room)+day+"/train/clusters/"+act+"/"+pathname);
        }
		
		float max=0f;
		for(ClusterCenter cc:this.map.keySet()) 
			if( map.get(cc)>max)
				max=map.get(cc);

		HashMap<ClusterCenter, Float> map2=new HashMap<ClusterCenter, Float>();
		for(ClusterCenter cc:this.map.keySet()) {
			map2.put(cc, map.get(cc)/max);
		}
		
		this.map=map2;
	}
	
	public void filterClusterW(float w) throws Exception{
		
		BufferedWriter wb0 = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(Param.Dataset.path_res(room)+day+"/train/clusters/"+act+".cluster.tsv"), "utf-8"));
		BufferedWriter wb1 = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(Param.Dataset.path_res(room)+day+"/train/clusters/"+act+".cluster.w.tsv"), "utf-8"));
		
		HashMap<ClusterCenter, Float> map2=new HashMap<ClusterCenter, Float>();
		for(ClusterCenter cc:this.map.keySet()) {
			wb0.write("#"+this.map.get(cc)+"#");
			for(float d:cc.data)
				wb0.write("\t"+d);
			wb0.write("\n");
			if(this.map.get(cc)>w) {
				map2.put(cc,this.map.get(cc));
				wb1.write("#"+this.map.get(cc)+"#");
				for(float d:cc.data)
					wb1.write("\t"+d);
				wb1.write("\n");
			}
		}
		this.map=map2;
		wb0.close();
		wb1.close();
	}
	
	public void loadMap(String file) throws Exception{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		String line=null;

		int count=0;
		 while ((line = br.readLine()) != null) {
			 
			 if(count==0) {
				 count++; 
				 continue;
			 }
			 System.out.println("#"+line+"#");
			 if(line.contains("NaN")) System.exit(0);
			 String [] codes=line.trim().split("\t");
			 if(codes.length!=nSensor) throw new Exception("Error codes.length!=nSensor "+codes.length+","+nSensor);
			 float [] data=new float[nSensor];
			 for(int i=0;i<codes.length;i++)
				 data[i]=Float.parseFloat(codes[i]);
			 
			 ClusterCenter cc=new ClusterCenter(data);
			 if(!this.map.containsKey(cc))
				 this.map.put(cc, 1f);
			 else
				 this.map.put(cc, this.map.get(cc)+1f);
			 
			 countCluster++;
			 count++;
		 }
		 System.out.println("file:"+file+"->"+(count-1));
		 br.close();
		 
	}
}

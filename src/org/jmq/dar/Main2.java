package org.jmq.dar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jmq.dar.ActClusterTime.ATime;
import org.jmq.dar.ActClusterTime.ClusterId;
import org.jmq.dar.reader.UciReader;

public class Main2 {

	
	static public List<Pair<Long,Long>> closerRandomTimes(List<Pair<Long,Long>> ret,ATime tB,History historyA,History historyB,List<ClusterId> clustersA, ActClusterTime acA) throws Exception{
		float incW=0;
		while(true) {
			ClusterId iClusterA=clustersA.get(Control.random.nextInt(clustersA.size()));
			List<ATime> tsA=acA.times.get(iClusterA);

			
			ATime tA=tsA.get(Control.random.nextInt(tsA.size()));
			if(Math.abs(tA.w-tB.w)<incW) {
				
//				System.out.println("\t\t iCluster  #A:"+Math.abs(tA.w-tB.w)+"@"+acA.centers.get(iClusterA));
//				System.out.println("\t\t:"+tA+"->"+tB+"w:"+Math.abs(tA.w-tB.w));
//				System.in.read();
//				for(int i=0;i<Param.wCloser;i++)
//					ret.add(new Pair<Long,Long>(historyA.getLongT(tA.t,i),historyB.getLongT(tB.t,i)));
//				for(int i=0;i<Param.wCloser;i++)
//					ret.add(new Pair<Long,Long>(historyA.getLongT(tA.t,-i),historyB.getLongT(tB.t,-i)));
				
			//	ret.add(new Pair<Long,Long>(historyA.getLongT2(tA.t,-1),historyB.getLongT2(tB.t,-1)));
				ret.add(new Pair<Long,Long>(tA.t,tB.t));
				//	ret.add(new Pair<Long,Long>(historyA.getLongT2(tA.t,+1),historyB.getLongT2(tB.t,+1)));
				

				break;
			}else		
				incW+=0.000001f;			
		}
		return ret;
	}
	
	static public List<Pair<Long,Long>> find10(int day, int limit00, float d,History historyA,History historyB,  String sensor, ActClusterTime acA, ActClusterTime acB) throws Exception{

		List<Pair<Long,Long>> ret=new LinkedList<Pair<Long,Long>>();
        int indexSensor=historyB.getCol1(sensor)-Param.filesAct.length;
        
        System.out.println("Sensor "+sensor+" #"+indexSensor);
 //       List<ClusterId> clustersBon=acB.getClustersByIndexSensorGTE(indexSensor, Param.degreeSimSelection);
   //     System.out.println("Clusters on: "+clustersBon);
   //     System.in.read();
        
        List<ClusterId> list=acB.getClustersByIndexSensorGTE(indexSensor, Param.degreeSimSelection2);
        if(list.size()==0) {
        	System.out.println("Error not on data:"+indexSensor+" day:"+day);
        	System.exit(1);
        }
        HashMap<ClusterId, Float> ws=new HashMap<ClusterId, Float>();
        float max=0;
        for(ClusterId iClusterBon:list) {
        	max=Math.max(max,acB.centers.get(iClusterBon).w);
        }
        for(ClusterId iClusterBon:list) {
        	ws.put(iClusterBon, acB.centers.get(iClusterBon).w/max);
        }
        for(ClusterId iClusterBon:list) {
        	float w=ws.get(iClusterBon);
        	int limit0=(int) (w* limit00);
            int count=0;
		while(true) {
			
	//		ClusterId iClusterBon=clustersBon.get(Control.random.nextInt(clustersBon.size()));
	//		System.out.println(acB.centers.get(iClusterBon).w);
		//	if((Math.random()>acB.centers.get(iClusterBon).w)) continue;
		//	System.out.println("in");
			List<ATime> tsBon=acB.times.get(iClusterBon);
			ATime tBon=tsBon.get(Control.random.nextInt(tsBon.size()));
			
	//		if(historyB.data.get(tB.t).getDegree(indexSensor)!=d) continue;
			
			System.out.println("\t\t iClusterOn #B:"+acB.centers.get(iClusterBon));

			
			List<ClusterId> clustersAon=acA.getClustersByActivity(iClusterBon.act);
//			static public List<Pair<Long,Long>> closerRandomTimes(List<Pair<Long,Long>> ret,ATime tB,History historyA,History historyB,List<ClusterId> clustersA, ActClusterTime acA) throws Exception{
			ret=closerRandomTimes(ret,tBon,historyA,historyB,clustersAon,acA);
	        List<ClusterId> clustersBoff=acB.getClustersByIndexSensorLTE(indexSensor, iClusterBon.act, 1-Param.degreeSimSelection);
	        if(clustersBoff.size()>0)
	    		while(true) {
	    			
	    			ClusterId iClusterBoff=clustersBoff.get(Control.random.nextInt(clustersBoff.size()));
	    			if((Math.random()>acB.centers.get(iClusterBoff).w)) break;
	    			List<ATime> tsBoff=acB.times.get(iClusterBoff);
	    			ATime tBoff=tsBoff.get(Control.random.nextInt(tsBoff.size()));
	    			
	    	//		if(historyB.data.get(tB.t).getDegree(indexSensor)!=d) continue;
	    			
	    			System.out.println("\t\t iClusterOff #B:"+acB.centers.get(iClusterBoff));
	    		
	    			
	    			List<ClusterId> clustersAoff=acA.getClustersByActivity(iClusterBoff.act);
//	    			static public List<Pair<Long,Long>> closerRandomTimes(List<Pair<Long,Long>> ret,ATime tB,History historyA,History historyB,List<ClusterId> clustersA, ActClusterTime acA) throws Exception{
	    			ret=closerRandomTimes(ret,tBoff,historyA,historyB,clustersAoff,acA);
	    	        break;
	        }

			count++; 
			if(count>=limit0) break;
		}
        }
		return ret;
	}
	
	
	

	static public List<Pair<Long,Long>> find(int limit, float d,History historyA,History historyB,  String sensor, ActClusterTime acA, ActClusterTime acB) throws Exception{

		List<Pair<Long,Long>> ret=new LinkedList<Pair<Long,Long>>();
        int count=0;
        int indexSensor=historyB.getCol1(sensor);
        
        List<ClusterId> clustersB=acB.getClusters();
		while(true) {
			
			ClusterId iClusterB=clustersB.get(Control.random.nextInt(clustersB.size()));
			List<ATime> tsB=acB.times.get(iClusterB);
			ATime tB=tsB.get(Control.random.nextInt(tsB.size()));
			
			if(historyB.data.get(tB.t).getDegree(indexSensor)!=d) continue;
			
			System.out.println("\t"+A.long2HHMM(tB.t));
			
			List<ClusterId> clustersA=acA.getClustersByActivity(iClusterB.act);
			float incW=0;
			while(true) {
				ClusterId iClusterA=clustersA.get(Control.random.nextInt(clustersA.size()));
				List<ATime> tsA=acA.times.get(iClusterA);

				ATime tA=tsA.get(Control.random.nextInt(tsA.size()));
				if(Math.abs(tA.w-tB.w)<incW) {
//						if("05/12/2011 14:38:30".equals(A.long2HHMM(tB.t))){
//							System.out.println("\t\t"+tB+" vs "+tA);
//							System.exit(0);						
//						}
						ret.add(new Pair<Long,Long>(tA.t,tB.t));
						break;
				}else		
					incW+=0.000001f;						
			}
			count++;
			if(count>=limit) break;
		}
		return ret;
	}
	
	static public void main(String [] args) throws Exception{
		for(int d:Param.days)
			generateTrain("roomA/","roomB/",d);
	}
	static public void generateTrain(String roomA, String roomB, int day) throws Exception{
			
		System.out.println(roomA);
		History historyA=new History();
		historyA.readFile(Param.Dataset.path_res(roomA)+"TimeLine.csv");
		
		{
			System.out.println(historyA.data.get(A.D2long("09/12/2011 12:43:30")));
	//		System.exit(0);
		}
		
		
		ActClusterTime clusterA=new ActClusterTime(roomA, day);
		List<Integer> colSA=historyA.getCols(Arrays.asList("#S#"));
		System.out.println(colSA.size()+"x"+clusterA.getClusters().size());
		
		
		System.out.println(roomB);
		History historyB=new History();
		historyB.readFile(Param.Dataset.path_res(roomB)+"TimeLine.csv");
		List<Integer> colSB=historyB.getCols(Arrays.asList("#S#"));
		ActClusterTime clusterB=new ActClusterTime(roomB, day);
		
//		System.out.println("Room A");
//		for(ClusterId cid: clusterA.getClustersByActivity("#A#1")) {
//			System.out.println("\t:"+clusterA.centers.get(cid));
//		}
//
//		System.out.println("Room B");
//		for(ClusterId cid: clusterB.getClustersByActivity("#A#1")) {
//			System.out.println("\t:"+clusterB.centers.get(cid));
//		}
	//	System.exit(1);
		
		for(int iSensorB:colSB) {
			
			String colB=historyB.cols.get(iSensorB);
		//	if(!colB.equals("#S#Entrance_Magnetic_Maindoor")) continue;

			BufferedWriter bw= new BufferedWriter(new FileWriter(new File(Param.Dataset.path_res(roomA)+day+"/train/TimeRelationAB."+colB+".tsv").getAbsoluteFile()));	

			System.out.println("Generating finder"	);
			//static public List<Pair<ATime,ATime>> find(int limit, float d,String room, int day, History historyA,History historyB,  String sensor, ActClusterTime acB, ActClusterTime acA) throws Exception{

				
			List<Pair<Long,Long>> listOn=find10(day,Param.GenerateData.nOnTrain, 1, historyA, historyB, colB,clusterA,clusterB);
			for(Pair<Long,Long> pair:listOn)
				bw.write(A.long2HHMM(pair.getFirst())+"\t"+A.long2HHMM(pair.getSecond())+"\n");

			List<Pair<Long,Long>> listOff=find(listOn.size(), 0, historyA, historyB, colB,clusterA,clusterB);
			for(Pair<Long,Long> pair:listOff)
				bw.write(A.long2HHMM(pair.getFirst())+"\t"+A.long2HHMM(pair.getSecond())+"\n");

			bw.close();
		}
		
	}
}

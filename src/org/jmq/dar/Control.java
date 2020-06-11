package org.jmq.dar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jmq.dar.ClusterMap.ClusterCenter;
import org.jmq.dar.History.Fill;
import org.jmq.dar.Param.Dataset;
import org.jmq.dar.reader.UciReader;


/**
Control class to handle the FTW and LSTM approach
 */
public class Control {

	
	static Random random=new Random(0L);
	
	
	static public List<A> streamAct=new LinkedList<A>();
	static public List<S> streamData=new LinkedList<S>();
	static public List<String> days=new LinkedList<String>();
	
	static A getInsideByTime(String act, long t) {
		for(A a:streamAct) {
			if(a.t0<=t&&t<=a.tN&&a.value.equals(act))
				return a;
		}
		return getCloserEndTime(act, t);		
	}
	
	static A getCloserEndTime(String act, long t) {
		long distE=Long.MAX_VALUE;
		A ret=null;
		for(A a:streamAct) 
			if(Math.abs(a.tN-t)<distE&&a.value.equals(act)) {
				distE=Math.abs(a.tN-t);
				ret=a;
			}
		return ret;
	}

	static public void loadData(String room) throws Exception{
		streamAct=Param.Dataset.reader.readAllAct(Dataset.activityFile(room));
		streamData=Param.Dataset.reader.readAllData(Dataset.sensorFile(room));
		
		 
		 days=Param.Dataset.reader.getDays(streamAct);
		 System.out.println("Days:"+days.size());
		 System.out.println(days);
		 
		 System.out.println("Datas:");
		 System.out.println(streamData.size());
	
		 System.out.println("Act:");
		 System.out.println(streamAct.size());
	
		 System.out.println("END READ DATA");
	
		Collections.sort(streamData, new S.SComparator());
		S.sS=S.calculateIndexOfSensor(streamData);
		System.out.println("S#"+S.sS);
		System.out.println("Sensors:"+S.hSensor);
		
		Collections.sort(streamAct, new A.VComparator());
		A.sA=A.calculateIndexOfAct(streamAct);
		System.out.println("SA#"+A.sA+"#"+streamAct.size());
		for(A a: streamAct)
			System.out.println(a.value+" ["+A.long2HHMM(a.t0)+","+A.long2HHMM(a.tN)+"]");
	
	}
	
	
	static public void writeResDays(String room) throws Exception{
		for(int day=1;day<=days.size();day++){
			File fday=new File(Dataset.path_res(room)+day);
			fday.mkdirs();
			File fdayTra=new File(Dataset.path_res(room)+day+"/train");
			fdayTra.mkdirs();
			File fdayTest=new File(Dataset.path_res(room)+day+"/test");
			fdayTest.mkdirs();
		}
	}

	
	static public void buildTimeLineWrite(String room) throws Exception{
		
		History history=new History();
		
		System.out.println("Adding time line activities");
		history.addTimeBetween(
				streamAct.get(0).t0,
				streamAct.get(streamAct.size()-1).tN
				);
				
		System.out.println("Ordering time line");
		history.calculateTimeLine();
		
		System.out.println("Adding Activities activation");
		history.addAs(streamAct);
		history.markNoneActivity();
		
		System.out.println("Adding sensors");
		history.addSs(streamData);
		
		history.addSsLast(streamData);
		
		
		history.writeInFile(Dataset.path_res(room)+"TimeLine.csv");
	}


	
	
	
	
	
	
//	static float [] scale(float  [] ws, float scale){
//		for(int i=0;i<ws.length;i++)
//			ws[i]*=scale;
//		return ws;
//	}	

			
			
//	static public void writeKey(String room) throws Exception {
//		String keys="";
//		for(VStatistic st:statisticList)
//			keys+=st.label+"="+Param.Dataset.reader.getKey_1(st.label)+"\n";
//		
//		write(Dataset.path(room)+"Keys.txt", keys.toString());
//	}
	static public void write(String file, String content) throws Exception{
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream(file), "utf-8"))) {
	   writer.write(content);
	   	writer.close();
		}		
	}
	


	static public void writeFTW(History history, String room) throws Exception{


		
		System.out.println("Adding fuzzy temporal windows in sensors");
		for(String s:	Param.filesAct){			
			history.addFTW_TA(streamAct,s);
			history.addFTW_TB(streamAct,s);
		}
		
		for(String s:	S.hSensor.keySet()){
			System.out.println("\t "+s);
			history.addFTW_TA(streamData,s);
			history.addFTW_TB(streamData,s);
			
		}

		history.writeInFile(Dataset.path_res(room)+"TimeLine."+FWindow.sTA+"."+FWindow.sTB+".csv");
	}
	
	static float f1(float p, float r){
		return (2*p*r)/(p+r);
	}
	
	
	static public History getDayClusters(History history, String room) throws Exception{
		



		for(int day=1;day<=days.size();day++){

			System.out.println("Day:"+day);
		
			write(Param.Dataset.path_res(room)+day+"/Day2Eval.txt", day+"");

			{
			File fd=new File(Dataset.path_res(room)+day+"/train/clusters/");
			fd.mkdirs();
			}
			
			String eDay=days.get(day-1);
			
			long t0=History.D2long(eDay+" 04:00:00");
			long tN=t0+1*24L*60L*60L;
			
			
			//ACTIVITIES	
			for(String actLabel:Param.filesAct){
				System.out.println(actLabel+"->"+history.getCol1(actLabel));
				history.markAll();
				history.mark(
						t0,
						tN,
						false
						);

				System.out.println("\t Rejecting between @"+History.long2D(t0)+" and "+History.long2D(tN));
				int countAct=0;
				for(A ai:history.computeInterval2(actLabel)){

						System.out.println("\t activity in :"+ai);
						
						history.unmarkAll();
						history.mark(
								ai.t0,
								ai.tN,
								true
								);
						
					//	System.in.read();
						{
						File fd=new File(Dataset.path_res(room)+day+"/train/clusters/"+actLabel);
						fd.mkdirs();
						}
										
						HistoryCluster cluster= new HistoryCluster(history);
						cluster.write(Param.Dataset.path_res(room)+day+"/train/clusters/"+actLabel+"/"+countAct+".cluster.tsv");

						countAct++;
					}
			}
		}
		return history;
	}
	
	static public HashMap<Integer,HashMap<String,ClusterMap>> getAggregatedCluster(History history, String room) throws Exception{
		
		HashMap<Integer,HashMap<String,ClusterMap>>  map=new HashMap<Integer,HashMap<String,ClusterMap>> ();
		int nSensor=history.getCols(Arrays.asList("#S#")).size();
		for(int day=1;day<=days.size();day++){
	
			HashMap<String,ClusterMap> map2= new HashMap<String,ClusterMap>();
			for(String actLabel:Param.filesAct) {
				ClusterMap cm=new ClusterMap(room, day, actLabel, nSensor);
				cm.filterClusterW(Param.alphaRejectCluster);
			
				map2.put(actLabel, cm);
			}
			map.put(day, map2);
		}
		return map;
	}
	
	static public History createRandomTimes(HashMap<Integer,HashMap<String,ClusterMap>> clusters, String room, History history)throws Exception{
						
			History historyAugmented=new History();
			for(String col:history.cols)
				historyAugmented.cols.add(col);
			
			for(int day=1;day<=days.size();day++){

				System.out.println("Day:"+day);
			
				
				String eDay=days.get(day-1);
				


				
				
				int countAllCluster=0;
				BufferedWriter writerTrainTA1 = new BufferedWriter(new OutputStreamWriter(
			              new FileOutputStream(Param.Dataset.path_res(room)+day+"/train/TimeActivity.tsv"), "utf-8"));

				//ACTIVITIES	
				for(String actLabel:Param.filesAct){		
						int countCluster=0;
						int colA=history.getCol1(actLabel);
						ClusterMap cmap=clusters.get(day).get(actLabel);
						System.out.println(day+"#act:"+actLabel+"#"+colA+" clusters:"+cmap.map.keySet().size());
						
						
						history.unmarkAll();
						history.markOnlyColHigher(colA, 0);
						long t1=History.D2long(eDay+" 04:00:00");
						long t2=t1+1*24L*60L*60L;
						System.out.println("\t Activating timeline between @"+History.long2D(t1)+" and "+History.long2D(t2));
						history.mark1(
								t1,
								t2,
								false
								);	
						
						List<Long> ts=history.getMarkedTimes();
						for(long t:ts)
							System.out.println(A.long2HHMM(t));
					
						HistoryCluster cluster=new HistoryCluster(history,cmap.map.keySet());
						
						for(int tI:cluster.getRandomPosCluster(Param.GenerateData.nTrainByCLuster, Param.degreeSimSelection))	{
	
								long t=ts.get(tI);
					
								if(history.data.get(t).getDegree(colA)==0) continue;
								if(!history.data.get(t).mark) continue;
		
		
								Fill f=new Fill();
						 		for(Float degree:history.data.get(t).data)
						 			f.data.add(degree);
						 		historyAugmented.data.put(t, f);
						 		A act=history.getInterval(actLabel, t);
								writerTrainTA1.write(UciReader.long2HHMM2(t)+"\t"+actLabel+"\t"+(countCluster/(Param.GenerateData.nTrainByCLuster))+"\t"+(countAllCluster/(Param.GenerateData.nTrainByCLuster))+"\t"+UciReader.long2HHMM2(act.t0)+"\t"+UciReader.long2HHMM2(act.tN)+"\n");
								
								countCluster++;
								countAllCluster++;						
					}

			}

			
			writerTrainTA1.close();
			}
			historyAugmented.calculateTimeLine();
			for(int day=1;day<=days.size();day++){

				System.out.println("Day:"+day);
				
				String eDay=days.get(day-1);
				
				long t0=History.D2long(eDay+" 04:00:00");
				long tN=t0+1*24L*60L*60L;
				
					BufferedWriter writerTestTA = new BufferedWriter(new OutputStreamWriter(
				              new FileOutputStream(Param.Dataset.path_res(room)+day+"/test/TimeActivity.tsv"), "utf-8"));
				//	BufferedWriter writerTestData = new BufferedWriter(new OutputStreamWriter(
					//              new FileOutputStream(Dataset.path_res+day+"/test/Data.tsv"), "utf-8"));
					
				history.unmarkAll();
				
				long t1=History.D2long(eDay+" 04:00:00");
				long t2=t1+1*24L*60L*60L;
				System.out.println("\t Activating timeline between @"+History.long2D(t1)+" and "+History.long2D(t2));
				history.mark1(
						t1,
						t2,
						true
						);				
				for(long t:history.getTimeLine()) {
					if(!history.data.get(t).mark) continue;
					writerTestTA.write(UciReader.long2HHMM2(t)+"\t"+history.getMaxActivitLabel(t)+"\n");					
					//	writerTestData.write(Arrays.toString(history.getSensorFeatures(t,fV))+"\n");
				}
				writerTestTA.close();
				//	writerTestData.close();
				
		}//day
	
		return historyAugmented;
	}//balance

}

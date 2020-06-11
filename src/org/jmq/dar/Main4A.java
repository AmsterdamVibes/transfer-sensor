package org.jmq.dar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jmq.dar.History.Fill;
import org.jmq.dar.Param.Dataset;
import org.jmq.dar.reader.UciReader;
import org.jmq.dar.reader.UciReaderA;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;
import weka.filters.supervised.instance.ClassBalancer;

public class Main4A {
	
	
	static public List<String> features=Arrays.asList("#S#","#AT#S#","#BT#S#");
	
	static public Classifier classifier() {
		return new RandomForest();
	}
	
	static public ArrayList<Long> readT(String file, int iT) throws Exception{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		String line=null;
		ArrayList<Long> ret=new ArrayList<Long>(); 
		 while ((line = br.readLine()) != null) {			 
			 String [] codes=line.trim().split("\t");	
		//	 System.out.println("\t"+codes[iT]);
			 ret.add(A.D2long(codes[iT]));
		 }
		 br.close();
		 return ret;
	}
	static public ArrayList<Long> readT2(String file, int iT) throws Exception{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		String line=null;
		ArrayList<Long> ret=new ArrayList<Long>(); 
		 while ((line = br.readLine()) != null) {			 
			 String [] codes=line.trim().split("\t");	
		//	 System.out.println("\t"+codes[iT]);
			 ret.add(UciReaderA.D2long(codes[iT]));
		 }
		 br.close();
		 return ret;
	}

	static public void main(String [] args ) throws Exception{
		
		
		System.out.println("Room A");
		History historyA=new History();
		historyA.readFile("./roomA/results/TimeLine."+FWindow.sTA+"."+FWindow.sTB+".csv");
		
		for(int c:historyA.getCols(features))
			System.out.print(historyA.cols.get(c)+"\t");
		System.out.println();
		for(int c:historyA.getCols(Arrays.asList("#S#")))
			System.out.print(historyA.cols.get(c)+"\t");
		System.out.println();
		System.out.println("RoomB");
		History historyB=new History();
		historyB.readFile("./roomB/results/TimeLine."+FWindow.sTA+"."+FWindow.sTB+".csv");
		for(int c:historyB.getCols(Arrays.asList("#S#")))
			System.out.print(historyB.cols.get(c)+"\t");
		System.out.println();
		
		for(int d:Param.days)
			dataTrainFile("roomA/","roomB/",historyA,historyB,d);
		
	}
	static public void dataTrainFile(String roomA, String roomB, History historyA, History historyB, int day) throws Exception{
		{
			
		
		for(int posS:historyB.getCols(Arrays.asList("#S#"))) {
			System.out.println(historyB.cols.get(posS)+"\t");
			createTrainFile(roomA, historyA,posS, historyB, day);
			}
		}
		
		History eval=new History();
		ArrayList<Long> tAs=readT2(Param.Dataset.path_res(roomA)+day+"/test/TimeActivity.tsv",0);
		System.out.println("tAs:"+Param.Dataset.path_res(roomA)+day+"/test/TimeActivity.tsv");
		//System.exit(1);
		//for(Long t:tAs)
		//	System.out.print(UciReader.long2DayTime(t)+",");
		System.out.println("");
			
		{
		for(int posS:historyB.getCols(Arrays.asList("#S#")))
			createTestFile(roomA, historyA,tAs,posS, day, historyB);
		for(long t:tAs)
			eval.data.put(t, new Fill());
			
		eval.calculateTimeLine();
		for(int posA:historyA.getCols(Arrays.asList("#A#")))
			eval.cols.add(historyA.cols.get(posA));
		for(int posS:historyB.getCols(Arrays.asList("#S#")))
			eval.cols.add(historyB.cols.get(posS));
		
		addActivity(historyA,tAs,eval);
		
		}
		for(int posS:historyB.getCols(Arrays.asList("#S#")))
			eval(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS)+".arff",Param.Dataset.path_res(roomA)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS)+".arff",tAs, eval);
		eval.writeInFile(Param.Dataset.path_res(roomB)+day+"/test/"+"TimeSensorsAB.tsv");
	}
	static public void createTrainFile(String roomA, History historyA,int posS,  History historyB, int day) throws Exception{
		
		
		String labelSensor=historyB.cols.get(posS);
		
		
		ArrayList<Long> tAs=readT(Param.Dataset.path_res(roomA)+day+"/train/TimeRelationAB."+labelSensor+".tsv",0);
		ArrayList<Long> tBs=readT(Param.Dataset.path_res(roomA)+day+"/train/TimeRelationAB."+labelSensor+".tsv",1);
		
		int wOn=1;
		int wOff=1;
		{
			int on=0,off=0;
			for(int i=0;i<tAs.size();i++) {
				long tB=tBs.get(i);
			
				if(historyB.data.get(tB).getDegree(posS)>0) {
				
					on++;
				}else {
				
					off++;
				}
				
			}
			System.out.println("\t"+getNameSensorL(historyB,posS)+" on:"+on+",off:"+off);
//			if(on<off) {
//				wOn=(on+off)/on;
//			}if(off<on)
//				wOff=(on+off)/off;
			System.out.println("\t"+getNameSensorL(historyB,posS)+" on:"+wOn+",off:"+wOff);
		}
		
		PrintWriter writerWeka = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS)+".arff", "UTF-8");
		writerWeka.println("@RELATION sensor_transfer");
		//int iSensor=historyB.getCols(Arrays.asList("#S#")).get(posS);
		for(int c:historyA.getCols(features)) {
			writerWeka.println("@ATTRIBUTE "+historyA.cols.get(c).replaceAll("#", "")+" REAL");
			//	break;
		}
		writerWeka.println("@ATTRIBUTE active {0,1}");
		writerWeka.println("@DATA");

		for(int i=0;i<tAs.size();i++) {
			long tA=tAs.get(i);
			long tB=tBs.get(i);
			boolean on=historyB.data.get(tB).getDegree(posS)>0;
			
			boolean on1u=historyB.data.get(historyB.getLongT2(tB, 1)).getDegree(posS)>0;
			boolean on2u=historyB.data.get(historyB.getLongT2(tB, 2)).getDegree(posS)>0;
			boolean on3u=historyB.data.get(historyB.getLongT2(tB, 3)).getDegree(posS)>0;
			boolean on1d=historyB.data.get(historyB.getLongT2(tB, -1)).getDegree(posS)>0;
			boolean on2d=historyB.data.get(historyB.getLongT2(tB, -2)).getDegree(posS)>0;
			boolean on3d=historyB.data.get(historyB.getLongT2(tB, -3)).getDegree(posS)>0;
			on=on||on1u||on2u||on3u||on1d||on2d||on3d;
			
			//			System.out.println(A.long2HHMM(tB)+"->"+on+"->"+historyB.data.get(tB));
//			System.in.read();
			int times=1;
			if(on) 
				times=wOn;
			else
				times=wOff;
			
			for(int j=0;j<times;j++){
				for(int c:historyA.getCols(features)) {
					//System.out.println("\t"+UciReader.long2HHMM2(tA));
					writerWeka.write(historyA.data.get(tA).getDegree(c)+",");
					//	break;
				}
				if(on) {
				//	System.out.println("is 1!");
					
					writerWeka.write("1"+"\n");
					
				}else {
					writerWeka.write("0"+"\n");
				
				}
			}
		}

		writerWeka.close();
	}
	
	static public String getNameSensorL(History historyB, int posS) {
		return historyB.cols.get(posS);
	}
	
	static public void createTestFile(String room, History historyA, ArrayList<Long> tAs, int posS,  int day, History historyB) throws Exception{
		PrintWriter writerWeka = new PrintWriter(Param.Dataset.path_res(room)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS)+".arff", "UTF-8");
		writerWeka.println("@RELATION sensor_transfer");
		for(int c:historyA.getCols(features)) {
			writerWeka.println("@ATTRIBUTE "+historyA.cols.get(c).replaceAll("#", "")+" REAL");
			//		break;
		}
		writerWeka.println("@ATTRIBUTE active {0,1}");
		writerWeka.println("@DATA");

		for(int i=0;i<tAs.size();i++) {
			long tA=tAs.get(i);
			//System.out.println(UciReader.long2DayTime(tA)+"->"+historyA.data.get(tA));
			for(int c:historyA.getCols(features)) {
				writerWeka.write(historyA.data.get(tA).getDegree(c)+",");

		//		break;
			}
			writerWeka.write(0+"\n");
		}

	
		writerWeka.close();
	}
	
	static public void eval(String path_train, String path_test, ArrayList<Long> tAs, History eval) throws Exception{
		System.out.println("\t Evaluating "+path_test);
		
		Instances data_train = new Instances(new BufferedReader(new FileReader(path_train)));
		data_train.setClassIndex(data_train.numAttributes() - 1);
		data_train.randomize(Control.random);
		
		

//	Filter balancer=new ClassBalancer();
//		data_train = Filter.useFilter(data_train, balancer);
		
		Classifier classifier= classifier();
		classifier.buildClassifier(data_train);
		
		Instances data_test = new Instances(new BufferedReader(new FileReader(path_test)));
		data_test.setClassIndex(data_test.numAttributes() - 1);
		
		for(int i=0;i<data_test.numInstances();i++){
			Instance instance = data_test.instance(i);
			int on=((int)classifier.classifyInstance(instance));
			double[] prediction=classifier.distributionForInstance(instance);

			eval.data.get(tAs.get(i)).data.add((float)on);
		//	eval.data.get(tAs.get(i)).data.add((float)prediction[1]/(float)(prediction[0]+prediction[1]));
		}
	}
	
	
	static public void addActivity(History historyA, ArrayList<Long> tAs, History eval) throws Exception{
		
		for(long t:tAs){
			for(int posA:historyA.getCols(Arrays.asList("#A#")))
				eval.data.get(t).data.add(historyA.data.get(t).data.get(posA));
		}
	}
}

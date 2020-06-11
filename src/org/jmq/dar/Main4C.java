package org.jmq.dar;

import java.io.BufferedReader;
import java.io.File;
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

public class Main4C {
	
	

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
		long t=historyA.getTimeLine().first();
		System.out.println("t0A:"+t+" -> next 0:"+historyA.getLongT2(t, 0));
		System.out.println("t0A:"+t+" -> next 1:"+historyA.getLongT2(t, 1));
		System.out.println("t0A:"+t+" -> next 2:"+historyA.getLongT2(t, 2));
		long t2=historyA.getLongT2(t, 2);
		System.out.println("t2A:"+t2+" -> befo 0:"+historyA.getLongT2(t2, -0));
		System.out.println("t2A:"+t2+" -> befo 1:"+historyA.getLongT2(t2, -1));
		System.out.println("t2A:"+t2+" -> befo 2:"+historyA.getLongT2(t2, -2));		

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
			eval(roomA, getNameSensorL(historyB,posS),day,tAs,eval);
		eval.writeInFile(Param.Dataset.path_res(roomB)+day+"/test/"+"TimeSensorsAB.tsv");
	}
	static public void createTrainFile(String roomA, History historyA,int posS,  History historyB, int day) throws Exception{
		
		
		String labelSensor=historyB.cols.get(posS);
		
		
		ArrayList<Long> tAs=readT(Param.Dataset.path_res(roomA)+day+"/train/TimeRelationAB."+labelSensor+".tsv",0);
		ArrayList<Long> tBs=readT(Param.Dataset.path_res(roomA)+day+"/train/TimeRelationAB."+labelSensor+".tsv",1);
		System.out.println("#tAs:"+tAs.size());
		System.out.println("#tBs:"+tBs.size());
		
		
		
		File ft=new File(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS));
		ft.mkdirs();
		File fts=new File(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals");
		fts.mkdirs();
		int count=0;
		
		for(int indexSensorA:historyA.getCols(Arrays.asList("#S#"))) {
			String lSensorA=historyA.cols.get(indexSensorA);
			PrintWriter writerBefore = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals/"+lSensorA+".A.csv", "UTF-8");
			count=0;
			for(long tA:tAs) {
				//writerBefore.write("  "+historyA.data.get(tA).getDegree(indexSensorA));
				for(int i=0;i<FWindow.sTB;i++)
					writerBefore.write("  "+historyA.data.get(tA).getDegree(historyA.getCol1(History.getLabel_TB(lSensorA,i))));
				writerBefore.write("\n");
				count++;
			}
			System.out.println("#"+count);
			writerBefore.close();
			PrintWriter writerAfter = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals/"+lSensorA+".B.csv", "UTF-8");
			count=0;
			for(long tA:tAs) {
				//writerAfter.write("  "+historyA.data.get(tA).getDegree(indexSensorA));
				for(int i=0;i<FWindow.sTA;i++)
					writerAfter.write("  "+historyA.data.get(tA).getDegree(historyA.getCol1(History.getLabel_TA(lSensorA,i))));
				writerAfter.write("\n");
				count++;
			}
			System.out.println("#"+count);
			writerAfter.close();
		}
		for(int indexSensorA:historyA.getCols(Arrays.asList("#A#"))) {
			String lSensorA=historyA.cols.get(indexSensorA);
			PrintWriter writerBefore = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals/"+lSensorA+".A.csv", "UTF-8");
			count=0;
			for(long tA:tAs) {
				//writerBefore.write("  "+historyA.data.get(tA).getDegree(indexSensorA));
				for(int i=0;i<FWindow.sTB;i++) {
					System.out.println(History.getLabel_TB(lSensorA,i));
					writerBefore.write("  "+historyA.data.get(tA).getDegree(historyA.getCol1(History.getLabel_TB(lSensorA,i))));
				}
				writerBefore.write("\n");
				count++;
			}
			System.out.println("#"+count);
			writerBefore.close();
			PrintWriter writerAfter = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals/"+lSensorA+".B.csv", "UTF-8");
			count=0;
			for(long tA:tAs) {
				//writerAfter.write("  "+historyA.data.get(tA).getDegree(indexSensorA));
				for(int i=0;i<FWindow.sTA;i++)
					writerAfter.write("  "+historyA.data.get(tA).getDegree(historyA.getCol1(History.getLabel_TA(lSensorA,i))));
				writerAfter.write("\n");
				count++;
			}
			System.out.println("#"+count);
			writerAfter.close();
		}
		
		PrintWriter writerY = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/train/Sensor_AB_"+getNameSensorL(historyB,posS)+"/y.csv", "UTF-8");
		
		count=0;
		for(long tB:tBs) {
			boolean on=historyB.data.get(tB).getDegree(posS)>0;
			boolean on1u=historyB.data.get(historyB.getLongT2(tB, 1)).getDegree(posS)>0;
			boolean on2u=historyB.data.get(historyB.getLongT2(tB, 2)).getDegree(posS)>0;
			boolean on3u=historyB.data.get(historyB.getLongT2(tB, 3)).getDegree(posS)>0;
			boolean on1d=historyB.data.get(historyB.getLongT2(tB, -1)).getDegree(posS)>0;
			boolean on2d=historyB.data.get(historyB.getLongT2(tB, -2)).getDegree(posS)>0;
			boolean on3d=historyB.data.get(historyB.getLongT2(tB, -3)).getDegree(posS)>0;
			
			writerY.write((on?"1":"0")+"\n");
			count++;
			}
		System.out.println("@"+count);
		

		writerY.close();
	//	System.exit(1);
	}
	
	static public String getNameSensorL(History historyB, int posS) {
		return historyB.cols.get(posS);
	}
	
	static public void createTestFile(String roomA, History historyA, ArrayList<Long> tAs, int posS,  int day, History historyB) throws Exception{
		File ft=new File(Param.Dataset.path_res(roomA)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS));
		ft.mkdirs();
		File fts=new File(Param.Dataset.path_res(roomA)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals");
		fts.mkdirs();
		
		
		for(int indexSensorA:historyA.getCols(Arrays.asList("#S#"))) {
			String lSensorA=historyA.cols.get(indexSensorA);
			PrintWriter writerBefore = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals/"+lSensorA+".A.csv", "UTF-8");
			for(long tA:tAs) {
		//		writerBefore.write("  "+historyA.data.get(tA).getDegree(indexSensorA));
				for(int i=0;i<FWindow.sTB;i++)
					writerBefore.write("  "+historyA.data.get(tA).getDegree(historyA.getCol1(History.getLabel_TB(lSensorA,i))));
				writerBefore.write("\n");
			}
			PrintWriter writerAfter = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals/"+lSensorA+".B.csv", "UTF-8");
			for(long tA:tAs) {
			//	writerAfter.write("  "+historyA.data.get(tA).getDegree(indexSensorA));
				for(int i=0;i<FWindow.sTA;i++)
					writerAfter.write("  "+historyA.data.get(tA).getDegree(historyA.getCol1(History.getLabel_TA(lSensorA,i))));
				writerAfter.write("\n");
			}
			writerBefore.close();
			writerAfter.close();
		}
		
		for(int indexSensorA:historyA.getCols(Arrays.asList("#A#"))) {
			String lSensorA=historyA.cols.get(indexSensorA);
			PrintWriter writerBefore = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals/"+lSensorA+".A.csv", "UTF-8");
			for(long tA:tAs) {
		//		writerBefore.write("  "+historyA.data.get(tA).getDegree(indexSensorA));
				for(int i=0;i<FWindow.sTB;i++)
					writerBefore.write("  "+historyA.data.get(tA).getDegree(historyA.getCol1(History.getLabel_TB(lSensorA,i))));
				writerBefore.write("\n");
			}
			PrintWriter writerAfter = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS)+"/signals/"+lSensorA+".B.csv", "UTF-8");
			for(long tA:tAs) {
			//	writerAfter.write("  "+historyA.data.get(tA).getDegree(indexSensorA));
				for(int i=0;i<FWindow.sTA;i++)
					writerAfter.write("  "+historyA.data.get(tA).getDegree(historyA.getCol1(History.getLabel_TA(lSensorA,i))));
				writerAfter.write("\n");
			}
			writerBefore.close();
			writerAfter.close();
		}
		PrintWriter writerY = new PrintWriter(Param.Dataset.path_res(roomA)+day+"/test/Sensor_AB_"+getNameSensorL(historyB,posS)+"/y.csv", "UTF-8");
		int i=0;
		for(long tA:tAs) {
			//boolean on=historyB.data.get(tB).getDegree(posS)>0;
			writerY.write(i%2==0?"0\n":"1\n");
			i++;
			}
		

		writerY.close();
		}

	
	
	static public void eval(String roomA, String sensor, int day, ArrayList<Long> tAs, History eval) throws Exception{
		
		String cmd="\"c:\\Program Files\\Python37\\python.exe\" CNN+LSTM.py "+roomA+"\\results\\"+day+" Sensor_AB_"+sensor;
		System.out.println(cmd);
		Process p = Runtime.getRuntime().exec(cmd);
        
        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new 
             InputStreamReader(p.getErrorStream()));

        String s="";
        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        
        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
        
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(roomA+"\\results\\"+day+"\\test\\Sensor_AB_"+sensor+"\\p.tsv"), Charset.forName("UTF-8")));
		String line=null;
		int i=0;

		 while ((line = br.readLine()) != null) {	 
			
			 eval.data.get(tAs.get(i)).data.add(Float.parseFloat(line.trim()));
			 i++;
		 }
	}
	
	
	static public void addActivity(History historyA, ArrayList<Long> tAs, History eval) throws Exception{
		
		for(long t:tAs){
			for(int posA:historyA.getCols(Arrays.asList("#A#")))
				eval.data.get(t).data.add(historyA.data.get(t).data.get(posA));
		}
	}
}

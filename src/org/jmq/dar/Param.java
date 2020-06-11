package org.jmq.dar;


import org.jmq.dar.reader.ReaderInstance;
import org.jmq.dar.reader.UciReaderA;
import org.jmq.dar.reader.UciReaderB;

public class Param {

	static public class Dataset{
//		static public String room="roomA";
//		static public String path="./"+room+"/";
//		static public String sensorFile=path+"/OrdonezA_Sensors.txt";
//		static public String activityFile=path+"/OrdonezA_ADLs.txt";
//		static public String path_res=path+"";
//		static public ReaderInstance reader=new UciReaderA();;
		
		
//		static public String room="roomB";
//		static public String path="./"+room+"/";
//		static public String sensorFile=path+"/OrdonezA_Sensors.txt";
//		static public String activityFile=path+"/OrdonezA_ADLsT.txt";
//		static public String path_res=path+"";
//		static public ReaderInstance reader=new UciReaderA();
		
		//static public String room="roomB";
		static public String path(String room) {
			return "./"+room+"/";
		}
		static public String sensorFile(String room) {
			return path(room)+"/Ordonez_Sensors.txt";
		}
		static public String activityFile(String room) {
			return path(room)+"/Ordonez_ADLsT.txt";
		}
		static public String path_res(String room) {
			return  path(room)+"/results/";
		}
		static public ReaderInstance reader=new UciReaderA();

	
	}
	static public class GenerateData{
		//number of data in train
		static int nTrainByCLuster=200;
		static int nOnTrain=400;
	//	static int nOffTrainAct=400;
	//	static int nOffTrain=500;
		
	}	
	
	static String [] filesAct= {
			"#A#0",
			"#A#1",
			"#A#2",
			"#A#3",
			"#A#4",
			"#A#5",
			"#A#6",
			"#A#7",
			"#A#8",
			"#A#9"
	};
	
	static Integer [] days= {
			1,
			2,
			3,
			4,
			5,
			6,
			7,
			8,
			9,
			10,
			11,
			12,
			13,
			14
	};
	
	static float degreeSimSelection=0.75f;
	static float degreeSimSelection2=0.75f;
	
//	static public float alphaClusterSensor=0.5f;
//	static public float alphaClusterSensorMin=0.5f;
	
	static public float alphaRejectCluster=0.1f;

	static double joinMinDegreeCluster=0.3d;
	static double getMinClusterRepresentation=0.3d;
	
//	static public int wCloser=2;
	
//	static public W
}

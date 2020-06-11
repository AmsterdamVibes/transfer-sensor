package org.jmq.dar;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.jmq.dar.Param.Dataset;
import org.jmq.dar.reader.UciReader;

public class Main5 {

	static public void main(String [] args) throws Exception{
		System.out.println("Day 0");
		History historyB=new History();
		historyB.readFile("./roomB/results/1/test/TimeSensorsAB.tsv");
		
		for(int d:Param.days)
			if(d>1)
				historyB.fusionAppend("./roomB/results/"+d+"/test/TimeSensorsAB.tsv");
		
		historyB.calculateTimeLine();
		writeSensorTimeLine("./roomB_1/",historyB);
		historyB.writeInFile("./roomB_1/TimeRawJoined.tsv");
		Files.copy(new File("./roomA/Ordonez_ADLsT.txt").toPath(), new File("./roomB_1/Ordonez_ADLsT.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	static public void writeSensorTimeLine(String folder, History historyB)throws Exception{
		
		File roomB_1=new File(folder);
		roomB_1.mkdirs();
		
		PrintWriter writeSensor = new PrintWriter(folder+"Ordonez_Sensors.txt", "UTF-8");
		
		writeSensor.write("Start time          	End time            	Location	Type	Place\n");
		writeSensor.write("--------------------	--------------------	--------	--------	-----\n");

		
		for(int c:historyB.getCols(Arrays.asList("#S#"))) {
			boolean on=false;
			long tI=-1L;
			for(long t:historyB.getTimeLine()){
				if(historyB.data.get(t).data.get(c)>0) {
					if(!on)
						tI=t;
					on=true;
				}else {
					if(on) {
						writeSensor.write(UciReader.long2HHMM2(tI)+"\t"+UciReader.long2HHMM2(t-1)+"\t"+divideSensorName(historyB.cols.get(c))+"\n");
					}
					on=false;
				}
			}
		}
		writeSensor.close();
	}

	static String divideSensorName(String code0) {
		//#S#Living_PIR_Door
		String code=code0.replaceAll("#S#", "");
		String [] codes=code.split("_");
		return codes[2]+"\t"+codes[1]+"\t"+codes[0];
	}
}

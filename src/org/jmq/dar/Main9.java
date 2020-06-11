package org.jmq.dar;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Main9 {

	static public void main(String [] args) throws Exception{
		System.out.println("Day 0");
		History historyB=new History();
		historyB.readFile("./roomA/results/1/test/TimeSensorsAB.tsv");
		
		for(int d:Param.days)
			if(d>1)
				historyB.fusionAppend("./roomA/results/"+d+"/test/TimeSensorsAB.tsv");
		
		Main5.writeSensorTimeLine("./roomA_1/",historyB);
		historyB.writeInFile("./roomA_1/TimeRawJoined.tsv");
	//	Files.copy(new File("./roomA/OrdonezA_ADLs.txt").toPath(), new File("./roomA_1/Ordonez_ADLsT.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}

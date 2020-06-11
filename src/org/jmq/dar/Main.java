package org.jmq.dar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jmq.dar.ClusterMap.ClusterCenter;
import org.jmq.dar.Param.Dataset;
import org.jmq.dar.reader.UciReaderA;
import org.jmq.dar.reader.UciReaderB;


/**
 * Main class to run FTW-LSTM in a dataset
 */
public class Main {

	static public void main(String [] args) throws Exception{
		{
		{
		String room="roomA/";
		
		Control.loadData(room);
		Control.writeResDays(room);
		Control.buildTimeLineWrite(room);
				
		History historyA=new History();
		historyA.readFile("roomA/results/TimeLine.csv");
		Control.getDayClusters(historyA, room);
		HashMap<Integer, HashMap<String, ClusterMap>> cc=Control.getAggregatedCluster(historyA, room);
		History historyAaugmented=Control.createRandomTimes(cc, room, historyA);
		historyAaugmented.writeInFile("roomA/results/TimeLineAugmented.csv");
		historyAaugmented.writeInFileTime2("roomA/results/TimeLineAugmented.2.csv");
		Control.writeFTW(historyA, room);

		}
		{
			String room="roomB/";
			
			Control.loadData(room);
			Control.writeResDays(room);
			Control.buildTimeLineWrite(room);
					
			History historyB=new History();
			historyB.readFile("roomB/results/TimeLine.csv");
			Control.getDayClusters(historyB, room);
			HashMap<Integer, HashMap<String, ClusterMap>> cc=Control.getAggregatedCluster(historyB, room);
			History historyBaugmented=Control.createRandomTimes(cc, room, historyB);
			historyBaugmented.writeInFile("roomB/results/TimeLineAugmented.csv");
			historyBaugmented.writeInFileTime2("roomB/results/TimeLineAugmented.2.csv");
			Control.writeFTW(historyB, room);
		}
	}
}
	
}

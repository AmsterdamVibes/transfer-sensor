package org.jmq.dar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jmq.dar.Param.Dataset;
import org.jmq.dar.reader.UciReaderA;
import org.jmq.dar.reader.UciReaderB;



public class Main6 {

	static public void main(String [] args) throws Exception{
		{
		String room="roomB_1/";
		
		Control.loadData(room);
		Control.writeResDays(room);
		Control.buildTimeLineWrite(room);
				
		History historyB_1=new History();
		historyB_1.readFile("roomB_1/results/TimeLine.csv");
		Control.getDayClusters(historyB_1, room);
		HashMap<Integer, HashMap<String, ClusterMap>> cc=Control.getAggregatedCluster(historyB_1, room);
		History historyAaugmented=Control.createRandomTimes(cc, room, historyB_1);
		historyAaugmented.writeInFile("roomB_1/results/TimeLineAugmented.csv");
		historyAaugmented.writeInFileTime2("roomB_1/results/TimeLineAugmented.2.csv");
		Control.writeFTW(historyB_1, room);

		}
	}
	
}

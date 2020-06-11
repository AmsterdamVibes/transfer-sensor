package org.jmq.dar;

import java.util.Arrays;

public class Main8 {

	static public void main(String [] args ) throws Exception{
		
		System.out.println("Room B_1");
		History historyB_1=new History();
		historyB_1.readFile("./roomB_1/results/TimeLine."+FWindow.sTA+"."+FWindow.sTB+".csv");
		
		for(int c:historyB_1.getCols(Arrays.asList("#AT#S#","#BT#S#")))
			System.out.print(historyB_1.cols.get(c)+"\t");
		System.out.println();
		
		System.out.println("RoomA");
		History historyA=new History();
		historyA.readFile("./roomA/results/TimeLine."+FWindow.sTA+"."+FWindow.sTB+".csv");
		for(int c:historyA.getCols(Arrays.asList("#S#")))
			System.out.print(historyA.cols.get(c)+"\t");
		System.out.println();
		
		for(int d:Param.days)
			Main4A.dataTrainFile("roomB_1/","roomA/",historyB_1,historyA,d);
		
	}
}

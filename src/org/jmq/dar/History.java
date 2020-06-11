package org.jmq.dar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.jmq.dar.reader.UciReader;

import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * 
 * Class to build a matrix of a timeline where activity and sensor are defined in time slot
 */
public class History {


	public List<String> cols;
	public HashMap<Long, Fill> data;
	
	History(){
		this.cols=new LinkedList<String>();
		this.data=new HashMap<Long, Fill>();
	}
	
	public float calculateRelevance(int source, int act){
		
		float w=0;
		float wT=0;
		for(long t:getTimeLine()){
			float dAct=this.data.get(t).getDegree(act);
			if(dAct<=0) continue;
			wT+=dAct;
			float dSource=this.data.get(t).getDegree(source);
			w+=dSource;
		}

		return w/wT;
	}
	
	int getIndexOfCol(String col2){
		int i=0;
		for(String col:cols)
			if(col.equals(col2))
				return i;
			else
				i++;
		return -1;
	}
	
	float getDegree(long t, String col){
		return this.data.get(t).getDegree(getIndexOfCol(col));
	}
	
	
	public void addTimeBetween(long t0, long tN) throws ParseException{
		for(long t=t0;t<tN+FWindow.getIncT();t+=FWindow.getIncT()){
		//	System.out.println(long2D(t));
			this.data.put(t, new Fill());
		}
		
	}
	
	
	
	public void addAs(List<A> streamAct0) throws Exception{
		for(String actLabel:A.allAct){
			cols.add(actLabel);
			for(long t:getTimeLine()){
				float v=0;
				for(A act: streamAct0){
					if(!act.value.equals(actLabel)) continue;
					if(act.isOn(t,t+FWindow.getIncT())>0f){
						v=1f;
						break;
					}
				}
				this.data.get(t).data.add(v);
//				if(v==1f) {
//					this.data.get(this.getLongT(t,-2)).data.add(0.5f);
//					this.data.get(this.getLongT(t,-1)).data.add(1f);
//					this.data.get(this.getLongT(t,+1)).data.add(1f);
//					this.data.get(this.getLongT(t,+2)).data.add(0.5f);
//				}
				
			}
		}
		
	}
	
	

	static public String getLabelLast(String s){
		return "#R#"+s;
	}	

	
	public void addSsLast(List<S> streamData) throws ParseException{
		for(String s:	S.hSensor.keySet()){
			cols.add(getLabelLast(s));
		}
			for(long t:getTimeLine()){
				S last=S.getCloser(streamData,t+FWindow.getIncT());
				for(String s:	S.hSensor.keySet()){
					if(last!=null&&last.sensor.equals(s))
						this.data.get(t).data.add(1f);
					else
						this.data.get(t).data.add(0f);
				}

			}
		
	}	

	
	public void addSs(List<S> streamData) throws ParseException{
		for(String s:	S.hSensor.keySet()){
		
			cols.add(s);
		}
			for(long t:getTimeLine()){
				//	boolean one=false;
				for(String s:	S.hSensor.keySet()){
				float d=0;
				//	if(!one)
				d=S.isOn2(s, streamData, t, t+FWindow.getIncT());

				this.data.get(t).data.add(d);
			//	if(d>0)
				//		one=true;
			}
		}
	}
	



	static public String getLabel_TA(){
		return "#AT";
	}
	static public String getLabel_TA(String sensor, int t){
		return getLabel_TA()+sensor+"#"+t;
	}

	static public String getLabel_TB(){
		return "#BT";
	}
	static public String getLabel_TB(String sensor, int t){
		return getLabel_TB()+sensor+"#"+t;
	}



	public void addFTW_TA(List<? extends A> stream, String col) throws ParseException{
			System.out.println("A@"+col);
			for(int t=0;t<FWindow.sTA;t++)
				cols.add(getLabel_TA(col,t));
			for(long ti:getTimeLine())
				for(FWindow fw:FWindow.getFTW_A(ti)) {
					float d=getFWindowFTW(stream,col, fw);
					if(d>1) {
						System.out.println("Error 1 in "+ti);
						System.exit(1);
					}
					this.data.get(ti).data.add(
							d
							
							);
				}
			
		
	}
	
	public float [] getActivityFeatures2(long ti,float [] features) {

		List<Integer> sCols=this.getCols(Arrays.asList("#A#","#AT#A#","#BT#A#"));
		
		int i=0;

				for(int c:sCols) {
					features[i]=this.data.get(ti).getDegree(c);
					i++;
				}
		
		return features;
	}
	
	public float [] getSensorFeaturesFTW(long ti,float [] features) {

		List<Integer> sCols=this.getCols(Arrays.asList("#AT#S#","#BT#S#"));
		
		int i=0;

				for(int c:sCols) {
					features[i]=this.data.get(ti).getDegree(c);
					i++;
				}
		
		return features;
	}
	public float [] getSensorFeaturesOnOff(long ti,float [] features) {

		List<Integer> sCols=this.getCols(Arrays.asList("#S#"));
		
		int i=0;

				for(int c:sCols) {
					features[i]=this.data.get(ti).getDegree(c);
					i++;
				}
		
		return features;
	}
	
	List<Long> getMarkedTimes(){
		List<Long> ret=new LinkedList<Long>();
		
		for(long ti:getTimeLine())
			if(this.data.get(ti).mark) 
				ret.add(ti);
		return ret;
	}

	public void addFTW_TB(List<? extends A> stream, String col) throws ParseException{
		System.out.println("B@"+col);
		for(int t=0;t<FWindow.sTB;t++)
			cols.add(getLabel_TB(col,t));
		for(long ti:getTimeLine())
			for(FWindow fw:FWindow.getFTW_B(ti)) {
			float d=getFWindowFTW(stream,col, fw);
			if(d>1) {
				System.out.println("Error 2 in "+ti);
				System.exit(1);
			}
				this.data.get(ti).data.add(
						d
						
						);
			}
		
	
}

	private float  getFWindowFTW(List<? extends A> stream,String col,FWindow fw) throws ParseException{
		float max=0f;
	//	System.out.println("@"+col);
		for(A interval:stream){
			
			if(interval.value.equals(col)) {
				float d=fw.getMembeship(interval.t0, interval.tN);
			//	System.out.println("\t"+interval.t0+","+interval.tN+"->"+d);
				
				max=Math.max(max, d);
			}
		}
		//System.out.println("max:"+max);
		return max;
	}
	
	


	


	
	public void mark(long t0, long tN){
		for(long t:getTimeLine()){
			if(t>=t0&&t<tN)
				this.data.get(t).mark=true;
			else
				this.data.get(t).mark=false;
		}
	}
	
	public void markAll(){
		for(long t:getTimeLine()){
				this.data.get(t).mark=true;
		}
	}
	
	public void unmarkAll(){
		for(long t:getTimeLine()){
				this.data.get(t).mark=false;
		}
	}
	public void mark(long t0, long tN, boolean a){
		for(long t:getTimeLine()){
			if(t>=t0&&t<tN)
				this.data.get(t).mark=a;
			else
				this.data.get(t).mark=!a;
		}
	}	
	public void markOnlyColHigher(int posC, float d) throws ParseException{
		for(long t:getTimeLine()){
			if(this.data.get(t).data.get(posC)>d) {
				//System.out.print("\t"+UciReader.long2HHMM2(t)+":"+this.data.get(t).data.get(posC)+"#"+posC);
				this.data.get(t).mark=true;
			}
		}
	}	
	
//	public void markNoneActivity() throws ParseException{
//		for(long t:getTimeLine()){
//			if(this.getMaxActivitIndex0(t)==-1) {
//				//System.out.print("\t"+UciReader.long2HHMM2(t)+":"+this.data.get(t).data.get(posC)+"#"+posC);
//				this.data.get(t).mark=true;
//			}else
//				this.data.get(t).mark=false;
//		}
//	}
//	
	public void markNoneActivity() throws ParseException{
		List<Float> idleV=new LinkedList<Float>();
		for(long t:getTimeLine())
			idleV.add(this.getMaxActivitIndex0(t)==-1?1f:0f);
		cols.add("#A#0");
		int i=0;
		for(long t:getTimeLine()) {
			this.data.get(t).data.add(idleV.get(i));
			i++;
		}
	}	
	
	public void writeNoneActivity() throws ParseException{
		for(long t:getTimeLine()){
			if(this.getMaxActivitIndex0(t)==-1) {
				//System.out.print("\t"+UciReader.long2HHMM2(t)+":"+this.data.get(t).data.get(posC)+"#"+posC);
				this.data.get(t).mark=true;
			}else
				this.data.get(t).mark=false;
		}
	}	
	
		
	public void mark1(long t0, long tN, boolean a){
		for(long t:getTimeLine()){
			if(t>=t0&&t<tN)
				this.data.get(t).mark=a;
		}
	}		
	public int sum(String sensor, String act){
		int total=0;
		for(long t:getTimeLine()){
			if(!this.data.get(t).mark) continue;
			total+=this.getDegree(t, sensor)*this.getDegree(t, act);
		}
		return total;
	}	
	
	public int sumMarked(){
		int total=0;
		for(long t:getTimeLine()){
			if(!this.data.get(t).mark) continue;
			total++;
		}
		return total;
	}	
	
	
	public List<A> computeInterval2(String col){
		List<A> ret=new LinkedList<A>();
		int c=this.getCol1(col);
		A temp=null;
		long lasT=-1L;
		for(long t:getTimeLine()){
			if(!this.data.get(t).mark) continue;
			
			if(this.data.get(t).data.get(c)>0&&temp==null) {
				temp=new A(t,Long.MAX_VALUE,col);
			}
			if(this.data.get(t).data.get(c)==0&&temp!=null) {
				temp.tN=t;
				ret.add(temp);
				temp=null;
			}
			lasT=t;
		}
		if(temp!=null&&temp.t0!=lasT) {
			temp.tN=lasT;
			ret.add(temp);
		}
		return ret;
	}
	
	public A getInterval(String col, long tX) throws ParseException{
	
		int c=this.getCol1(col);
		A temp=null;
		long lasT=-1L;
		for(long t:getTimeLine()){
//			if(!this.data.get(t).mark) continue;
			
			if(this.data.get(t).data.get(c)>0&&temp==null) {
				temp=new A(t,Long.MAX_VALUE,col);
			}
			if(this.data.get(t).data.get(c)==0&&temp!=null) {
				temp.tN=t;
				//		System.out.println("\t\t"+temp+" vs "+A.long2HHMM(tX)+":["+temp.t0+","+temp.tN+"]:"+tX+"?"+(tX>=temp.t0&& tX<=temp.tN)+"?"+(tX>=temp.t0)+"?"+(tX<=temp.tN));
				if(tX>=temp.t0&& tX<=temp.tN) return temp;
				temp=null;
			}
			lasT=t;
		}
		if(temp!=null&&temp.t0!=lasT) {
			temp.tN=lasT;
			if(tX>=temp.t0&& tX<=temp.tN) return temp;
		}
		//	System.out.println(this.data.get(lasT).data.get(c)+"\t"+lasT+"\t"+tX+"\t"+temp);
		if(this.data.get(lasT).data.get(c)>0) {
			temp.tN=lasT;
		//	System.out.println("\t\t"+temp+" vs "+A.long2HHMM(tX)+":["+temp.t0+","+temp.tN+"]:"+tX+"?"+(tX>=temp.t0&& tX<=temp.tN)+"?"+(tX>=temp.t0)+"?"+(tX<=temp.tN));
			if(tX>=temp.t0&& tX<=temp.tN) return temp;
		}
		return null;
	}

	
	@Override
	public String toString(){
		String ret="Time";
		
		for(String col:cols)
			ret+="\t"+col;
		ret+="\n";
		int i=0;
		for(long t:getTimeLine()){
			try {
				ret+=long2D(t);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(float f:this.data.get(t).data)
				ret+="\t"+f;
			ret+="\n";			
		
			i++;
		}
			
		return ret;
	}
	
	TreeSet<Long> timeKey;
	public TreeSet<Long> getTimeLine(){
		return timeKey;	
	}
	

	public Collection<Long> calculateTimeLine(){
		return timeKey=new TreeSet<Long>(data.keySet());	
	}
	
	static public class Fill{
		List<Float> data;
		boolean mark;
		Fill(){
			this.data=new LinkedList<Float>();
			this.mark=false;
		}
		
		public float getDegree(int pos){
			return this.data.get(pos);
		}

		@Override
		public String toString() {
			return "Fill [data=" + data + ", mark=" + mark + "]";
		}
		
		
		
	}
	
	
	static SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	static public long D2long(String day) throws ParseException{
		return df.parse(day).getTime()/1000L;
	}		
	static public String long2D(long day) throws ParseException{
		return df.format(day*1000L);
	}		
	

	
	public List<Integer> getCols(List<String> initCols){
		
		List<Integer> colIndexs=new LinkedList<Integer>();
		for(String init:initCols){
			
			for(int i=0;i<cols.size();i++){
					String col=cols.get(i);
					if(col.startsWith(init)&&!colIndexs.contains(i)){
						colIndexs.add(i);
						
					//	System.out.println("Init: "+init+" Index of "+col+" "+i+" / "+this.cols.size());
//						break;
					}
			}
		}
		
		return colIndexs;		
	}
	
	public void printCols(){
		for(String col:this.cols)
			System.out.println("\t"+col);
	}
	public List<Integer> getCol(String initCol){
		
		List<String> initCols=new LinkedList<String>();
		initCols.add(initCol);
		return this.getCols(initCols);
	}
	
	public int getCol1(String col){
		return getCol(col).get(0);
	}

	
	public void printCol(String col) throws ParseException{
		for(long t:getTimeLine()){
			if(this.data.get(t).mark){
			System.out.println("\t"+long2D(t)+"\t"+this.getDegree(t, col));
				
			}
		}
	}	
	public void printColsSW(List<Integer> cols) throws ParseException{
		for(long t:getTimeLine()){
			if(this.data.get(t).mark){
			System.out.print("\t"+long2D(t));
			
			for(Integer col0:cols){
					System.out.print("\t"+this.data.get(t).getDegree(col0));
			}
			System.out.println("");
			}
		}
	}	
	
	
	
	
	

	public void writeInFile(String fileName) throws IOException, ParseException{
		
		BufferedWriter bw= new BufferedWriter(new FileWriter(new File(fileName).getAbsoluteFile()));	
	
		bw.write("Time");
		for(int i=0;i<cols.size();i++)
			bw.write("\t"+cols.get(i));
		bw.write("\n");
		for(long t:getTimeLine()){
			bw.write(long2D(t));
			for(float f:this.data.get(t).data)
				bw.write("\t"+f);
			bw.write("\n");		
		}
			
		bw.close();
	}
	
//	public long getLongT(long t0, int off) throws Exception{
//		int pos=0;
//		for(long t:getTimeLine()){
//			if(t==t0)
//				break;
//			pos++;
//		}
//		pos+=off;
//		if(pos<0)
//			pos=0;
//		if(pos>=this.getTimeLine().size())
//			pos=this.getTimeLine().size()-1;
//		int i=0;
//		for(long t:getTimeLine()){
//			if(i==pos) return t;
//			i++;
//		}
//		throw new Exception("Error in time offset");
//	}
	public long getLongT2(long t0, int off) throws Exception{
		TreeSet<Long> ts=this.timeKey;
		long t=t0;
		if(off>0)
			for(int i=0;i<off;i++) {
				long t2=t+FWindow.getIncT();
				if(!ts.contains(t2))
					break;
				t=t2;
			}
		if(off<0)
			for(int i=0;i<-off;i++) {
				long t2=t-FWindow.getIncT();
				if(!ts.contains(t2))
					break;
				t=t2;
			}
		return t;
	}
	public void writeInFileTime2(String fileName) throws IOException, ParseException{
		
		BufferedWriter bw= new BufferedWriter(new FileWriter(new File(fileName).getAbsoluteFile()));	
	
		bw.write("Time");
		for(int i=0;i<cols.size();i++)
			bw.write("\t"+cols.get(i));
		bw.write("\n");
		for(long t:getTimeLine()){
			bw.write(UciReader.long2HHMM2(t));
			for(float f:this.data.get(t).data)
				bw.write("\t"+f);
			bw.write("\n");		
		}
			
		bw.close();
	}
	
	public void writeInFileIfMarker(String fileName) throws IOException, ParseException{
		
		BufferedWriter bw= new BufferedWriter(new FileWriter(new File(fileName).getAbsoluteFile()));	
	
		bw.write("Time");
		for(int i=0;i<cols.size();i++)
			bw.write("\t"+cols.get(i));
		bw.write("\n");
		for(long t:getTimeLine()){
			if(!this.data.get(t).mark) continue;
			bw.write(long2D(t));
			for(float f:this.data.get(t).data)
				bw.write("\t"+f);
			bw.write("\n");		
		}
			
		bw.close();
	}
	
	public void removeCols(List<Integer> iCols){		
		int rt=0;
		for(int c:iCols){
			this.cols.remove(c-rt);
			for(long t:getTimeLine())
					this.data.get(t).data.remove(c-rt);
			rt++;
		}
	}
	
	public String getMaxActivitLabel(long t) {

		
		int indexC=getMaxActivitIndex(t);
			return indexC<0?"":this.cols.get(indexC);
		}
	public int getMaxActivitIndex(long t) {

		float maxC=-1;
		int indexC=-1;
		for(int c:this.getCols(Arrays.asList("#A#")))
			if(maxC<this.data.get(t).getDegree(c)) {
				maxC=this.data.get(t).getDegree(c);
				indexC=c;
			}
			return indexC;
		}
	public int getMaxActivitIndex0(long t) {

		float maxC=0;
		int indexC=-1;
		for(int c:this.getCols(Arrays.asList("#A#")))
			if(maxC<this.data.get(t).getDegree(c)) {
				maxC=this.data.get(t).getDegree(c);
				indexC=c;
			}
			return indexC;
		}
	
	public String getRandomActivitOn(long t) {

		List<String> act=new LinkedList<String>();
		for(int c:this.getCols(Arrays.asList("#A#")))
			if(this.data.get(t).getDegree(c)>0) 
				act.add(this.cols.get(c));
		if(act.size()==0) return null;	
		return act.get(Control.random.nextInt(act.size()));
		}
	public void readFile(String file) throws NumberFormatException, IOException, ParseException{
		InputStreamReader isr = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
		
		BufferedReader br = new BufferedReader(isr);
		String line=null;
		int count=0;
		 while ((line = br.readLine()) != null) {
			 	String [] codes=line.split("\t");
			 	if(count==0){
			 		for(int i=1;i<codes.length;i++)
			 			this.cols.add(codes[i]);
			 	}else{
			 		long t=D2long(codes[0]);
			 		Fill f=new Fill();
			 		for(int i=1;i<codes.length;i++)
			 			f.data.add(Float.parseFloat(codes[i]));
				 	this.data.put(t, f);
			 	}
			 		
	 			count++;
		    }		
		 isr.close();
		 calculateTimeLine();
	}
	
	public void fusionAppend(String file) throws Exception{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		String line=null;
		int count=0;
		 while ((line = br.readLine()) != null) {
			 	String [] codes=line.split("\t");
			 	if(count==0){
			 		for(int i=1;i<codes.length;i++) {
			 			System.out.println(this.cols.get(i-1)+"vs"+codes[i]);
			 			if(!this.cols.get(i-1).equals(codes[i]))
			 				throw new Exception("Not same columns in history fusion");
			 		}
			 	}else{
			 		long t=D2long(codes[0]);
			 		Fill f=new Fill();
			 		for(int i=1;i<codes.length;i++)
			 			f.data.add(Float.parseFloat(codes[i]));
				 	this.data.put(t, f);
			 	}
			 		
	 			count++;
		    }		
		 br.close();
		 calculateTimeLine();
	}
}

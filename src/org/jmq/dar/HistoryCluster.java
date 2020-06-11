package org.jmq.dar;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jmq.dar.ClusterMap.ClusterCenter;
import org.jmq.dar.reader.UciReader;


public class HistoryCluster {
	
	static public int nCluster=5;
	
	FuzzyClustering clustering;
	History history;

	double [][] data_point;
	
	int num_data_points;
	int num_dimensions;
	List<Integer> cols;
	HistoryCluster(History history) throws ParseException{
		this.history=history;
		this.num_data_points=this.history.sumMarked();
	//	this.num_data_points=4;
		this.cols=this.history.getCols(Arrays.asList("#S#"));
		this.num_dimensions=cols.size();
		this.data_point=new double[num_data_points][num_dimensions];
		fillDataPoints();
	//	System.exit(1);
		FuzzyClustering clustering0=new FuzzyClustering(data_point,num_data_points,num_dimensions,nCluster,2d,100);
		//clustering.print_data_points0();
	//	clustering0.print_membership_matrix22();
		
		clustering0.fcm();
	//	clustering0.print_data_points0();
		clustering0.print_cluster();
	    System.out.println("D1:"+clustering0.data_point.length+" #"+clustering0.cluster_centre.length);
	    this.clustering=clustering0.getDifClusters1(Param.joinMinDegreeCluster);
	    this.clustering.update_degree_of_membership();
	    this.clustering.print_cluster();
	    System.out.println("D2:"+this.clustering.data_point.length+" #"+this.clustering.cluster_centre.length);
	    this.clustering=this.clustering.getDifClusters2(Param.getMinClusterRepresentation);
	    this.clustering.update_degree_of_membership();
	    this.clustering.print_data_points0();
	    this.clustering.print_cluster();
	    System.out.println("D3:"+this.clustering.data_point.length+" #"+this.clustering.cluster_centre.length);
	}
	
	HistoryCluster(History history, Set<ClusterCenter> centers) throws ParseException, IOException{
		this.history=history;
		this.num_data_points=this.history.sumMarked();
		this.cols=this.history.getCols(Arrays.asList("#S#"));
		this.num_dimensions=cols.size();
		this.data_point=new double[num_data_points][num_dimensions];
		fillDataPoints();
		FuzzyClustering clustering0=new FuzzyClustering(data_point,num_data_points,num_dimensions,centers.size(),2d,100);
		int i=0;
		for(ClusterCenter cc:centers){
				
				System.out.println(Arrays.toString(cc.data));
				for(int j=0;j<num_dimensions;j++)
					clustering0.cluster_centre[i][j]=cc.data[j];
				i++;
		}
		clustering0.print_cluster();
	    System.out.println("D1:"+clustering0.data_point.length+" #"+clustering0.cluster_centre.length);
	    this.clustering=clustering0.getDifClusters1(Param.joinMinDegreeCluster);
	    this.clustering.update_degree_of_membership();
	    this.clustering.print_cluster();
	    System.out.println("D2:"+this.clustering.data_point.length+" #"+this.clustering.cluster_centre.length);
	    this.clustering=this.clustering.getDifClusters2(Param.getMinClusterRepresentation);
	    this.clustering.update_degree_of_membership();
	    //    this.clustering.print_data_points0();
	    this.clustering.print_cluster();
	    System.out.println("D3:"+this.clustering.data_point.length+" #"+this.clustering.cluster_centre.length);
//	    System.in.read();
	}
	
	public List<Integer> getRandomPosCluster(int sizeCluster, double maxR) throws IOException{

		List<Integer> ret=new LinkedList<Integer>();
	
		for(int c=0;c<this.getSizeCluster();c++) {
			double minD=1;
			double maxD=0;
			for(int i=0;i<this.data_point.length;i++) {
				if(this.clustering.degree_of_memb[i][c]<minD)
					minD=this.clustering.degree_of_memb[i][c];
				if(this.clustering.degree_of_memb[i][c]>maxD)
					maxD=this.clustering.degree_of_memb[i][c];
			}
			System.out.println("Cluster:["+minD+","+maxD+"]"+this.data_point.length+"vs"+this.history.sumMarked());
			if(minD==maxD) {
				minD=0;
		//		maxD=1;
			}
			System.out.println("Cluster:["+minD+","+maxD+"]"+this.data_point.length+"vs"+this.history.sumMarked());
			int count=0;
			while(true) {
				int tI=Control.random.nextInt(this.data_point.length);
				double v0=this.clustering.degree_of_memb[tI][c];
				v0=(v0-minD)/(maxD-minD);
				float v=(float)Math.pow(v0, 1);
				//if(this.clustering.degree_of_memb[tI][c]>maxR && this.clustering.degree_of_memb[tI][c]>Control.random.nextDouble()) {
				if(v>maxR&&v>Control.random.nextDouble()) {
					//System.out.println("\t"+this.clustering.degree_of_memb[tI][c]);
					ret.add(tI);
					count++;
					if(count>=sizeCluster)
						break;
				}
			}
			System.out.println("\t"+c+"/"+this.getSizeCluster()+":"+count);
		//	System.in.read();
					
		}
	
		
		return ret;		
	}
	private void fillDataPoints() throws ParseException {
		int i=0;
		for(long t:this.history.getTimeLine()) {
			if(!this.history.data.get(t).mark) continue;
		//	System.out.print("\t"+UciReader.long2HHMM2(t));
			int j=0;
			for(int c:this.cols) {
			//	System.out.print("\t"+this.history.data.get(t).getDegree(c));
				this.data_point[i][j]=(double)this.history.data.get(t).getDegree(c);
				j++;
			}
		//	System.out.print("\n");
			i++;
		}
		System.out.println("fillDataPoints()"+i);
	}
	public void write(String file) throws Exception{
		this.clustering.writeCluster(file);
	}
	
	public int getSizeCluster() {
		return this.clustering.cluster_centre.length;
	}
	
	/**
	float [] w;
	float wT;
	double [] data;
	int nAccept;
	float wThreshold;
	
	public void initW() {
		this.w=new float[getSizeCluster()];
		for(int i=0;i<w.length;i++)
			this.w[i]=0f;
		this.data=new double[num_dimensions];
		this.nAccept=0;
		this.wT=0f;
		this.wThreshold=4f/(float)getSizeCluster();
		
	}
	
	public boolean accept(float [] data0 ) {
		for(int i=0;i<num_dimensions;i++)
			data[i]=(double)data0[i];
		this.clustering.setDataPoint1(data);
		this.clustering.update_degree_of_membership();

		float wT2=0;
		for(int i=0;i<getSizeCluster();i++) 
			wT2+=(float)this.clustering.degree_of_memb[0][i];
		for(int i=0;i<getSizeCluster();i++) {
			float incW=this.w[i]+(float)this.clustering.degree_of_memb[0][i]/(float)(wT2+wT);
			System.out.println((float)this.clustering.degree_of_memb[0][i]+ "vs "+this.w[i]+" incW:"+incW/(float)getSizeCluster()+" vs "+wThreshold);
			if(incW/(float)getSizeCluster() > wThreshold) 
				return false;			
		}
		nAccept++;
		wT+=wT2;
		for(int i=0;i<getSizeCluster();i++) 
			this.w[i]+=(float)this.clustering.degree_of_memb[0][i];
		return true;
	}
	*/
}

package org.jmq.dar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class FuzzyClustering {


	double [][]degree_of_memb;
	double [][] data_point;
	public double [][] cluster_centre;

	int num_data_points;
	int num_clusters;
	int num_dimensions;
	double fuzziness;

	int max_steps;	
	//uzzyness coefficient should be > 1.0\n
	//termination criterion should be > 0.0 and <= 1.0\n
	public FuzzyClustering(double [][] data_point,int num_data_points,  int num_dimensions, int num_clusters,double fuzziness, int max_steps){

		this.data_point=data_point;
		this.num_data_points=num_data_points;
		this.num_dimensions=num_dimensions;
		this.num_clusters=num_clusters;
		this.fuzziness=fuzziness;

		this.max_steps=max_steps;
		

		degree_of_memb=new double[num_data_points][num_clusters];
		cluster_centre=new double[num_clusters][num_dimensions];

		Random random=new Random(0L);


		    for (int i = 0; i < num_data_points; i++) {
		        double s = 0.0f;
		        double r = 100f;
		        for (int j = 1; j < num_clusters; j++) {
		            double rval = random.nextDouble()* (r + 1);
		            r -= rval;
		            degree_of_memb[i][j] = rval / 100.0f;
		            s += degree_of_memb[i][j];
		        }
		        degree_of_memb[i][0] = 1.0f - s;
		    }
		    
//		    for (int i = 0; i < num_data_points; i++) {
//		    	double s=0d;
//		        for (int j = 0; j < num_clusters; j++) {
//		            degree_of_memb[i][j] = random.nextDouble();
//		            s+=degree_of_memb[i][j];
//		        }
//		        for (int j = 0; j < num_clusters; j++) 
//		        	degree_of_memb[i][j]=degree_of_memb[i][j]/s;
//		    }		    
	}
	
	public FuzzyClustering(double [][] data_point, int num_data_point, double [][] cluster_centre,  int num_dimensions, int num_clusters,double fuzziness, int max_steps){

		this.num_data_points=num_data_point;
		this.num_dimensions=num_dimensions;
		this.num_clusters=num_clusters;
		this.fuzziness=fuzziness;

		this.max_steps=max_steps;
		

		this.cluster_centre=cluster_centre;
		this.degree_of_memb=new double[num_data_points][num_clusters];
		
		this.data_point=data_point;
	}
	
	public void setDataPoint(double [][] data_point) {
		this.data_point=data_point;
	}
	public void setDataPoint1D(double [] data_point0) {
		this.num_data_points=1;
		this.degree_of_memb=new double[num_data_points][num_clusters];
		this.data_point=new double[1][num_clusters];
		this.data_point[0]=data_point0;
	}
		
	public void calculate_centre_vectors() {
		    
		    double [][]t=new double[num_data_points][num_clusters];
		    for (int i = 0; i < num_data_points; i++) {
		        for (int j = 0; j < num_clusters; j++) {
		            t[i][j] = (double)Math.pow(degree_of_memb[i][j], fuzziness);
		        }
		    }
		    for (int j = 0; j < num_clusters; j++) {
		        for (int k = 0; k < num_dimensions; k++) {
		            double numerator = 0.0f;
		            double denominator = 0.0f;
		            for (int i = 0; i < num_data_points; i++) {
		                numerator += t[i][j] * data_point[i][k];
		                denominator += t[i][j];
		            }
		            cluster_centre[j][k] = numerator / denominator;
		        }
		    }
		}


	double get_normABS(int i, int j) {
		    
		    double sum = 0.0f;
		    for (int k = 0; k < num_dimensions; k++) {
//		    	if(data_point[i][k] < cluster_centre[j][k])
//		    		sum += Math.pow(data_point[i][k] - cluster_centre[j][k], 4);
//		    	else
//			        sum += Math.pow(data_point[i][k] - cluster_centre[j][k], 2);
		        double v=Math.pow(data_point[i][k] - cluster_centre[j][k], 2);
		    	//double v=Math.abs(data_point[i][k] - cluster_centre[j][k]);
		        sum += v;
		    }
		   // return (double)(sum)/(double)num_dimensions;
		    return Math.sqrt(sum);
		}
		


		double
		simility_data_kernel(int i, int j) {
		    double sum = 0.0f;
		    double p = 2 / ((double)fuzziness - 1);
		    for (int k = 0; k < num_clusters; k++) {
		        double t = 1;
		        if(get_normABS(i, k)>0)
		        	t=get_normABS(i, j) / get_normABS(i, k);
		        t = (double)Math.pow(t, p);
		        sum += t;
		    }
		    if(sum==0) return (double)num_dimensions;
		    return 1.0f / sum;
		}

		double
		update_degree_of_membership() {
		    
		    double avg_diff=0f;
		    int t=0;
		    for (int j = 0; j < num_clusters; j++) {
		        for (int i = 0; i < num_data_points; i++) {
		        	double new_uij = simility_data_kernel(i, j);
		            double diff = new_uij - degree_of_memb[i][j];
		            avg_diff+=Math.abs(diff);
		      //      System.out.println("diff:"+diff+" vs avg_diff:"+avg_diff+" vs t:"+t);
		            t++;
		            degree_of_memb[i][j] = new_uij;
		        }
		    }
		    return avg_diff/(double)t;
		}
		
		

		
	    public double avg_diff=0f;
	    int step=0;
		int
		fcm() {

			step=0;
		    avg_diff=0f;
		    do {
		        calculate_centre_vectors();
		        avg_diff = update_degree_of_membership();
			 //   System.out.println("\t #"+step+"\t avg_diff:"+avg_diff);
		        step++;
		    } while (step<max_steps);
		    return 0;
		}


		void
		print_data_points0() {
		    System.out.println("Data points:\n");
		    for (int i = 0; i < num_data_points; i++) {
	//	    	System.out.println("Data[%d]: "+ i);
		        for (int j = 0; j < num_dimensions; j++) {
		        	System.out.print("\t"+ data_point[i][j]);
		        }
		        System.out.println();
		    }
		}

		void
		print_membership_matrix0() {
		    System.out.println("Membership matrix:\n");
		    for (int i = 0; i < num_data_points; i++) {
		 //   	System.out.println("Data[%d]: "+ i);
		        for (int j = 0; j < num_clusters; j++) {
		        	System.out.print("\t"+degree_of_memb[i][j]);
		        }
		        System.out.println();
		    }
		}
		
		void
		print_membership_matrix0(float [] cxs,float [] cys, float [] pxs,float [] pys) {
		    System.out.println("Membership matrix:\n");
		    for (int i = 0; i < num_data_points; i++) {
		    //	System.out.println("Data[%d]: "+ i);
		    	pxs[i]=0f;
		    	pys[i]=0f;
		    	float w=0f;
		        for (int j = 0; j < num_clusters; j++) {
		        	float wi=(float)this.simility_data_kernel(i, j);
		        	System.out.print("\t"+wi);
		        	pxs[i]+=cxs[j]*wi;
		        	pys[i]+=cys[j]*wi;
		        	w+=wi;
		        }
		    	pxs[i]/=w;
		    	pys[i]/=w;
		    	System.out.println("\t ("+pxs[i]+","+pys[i]+")");
		    }
		}
		void
		print_membership_matrix22() {
		    System.out.println("Similarity matrix:\n");
		    for (int i = 0; i < num_data_points; i++) {
		    	System.out.print("Data[%d]: "+ i+")");
		        for (int j = 0; j < num_clusters; j++) {
		        	System.out.print("\t"+this.simility_data_kernel(i, j));
		        }
		        System.out.println();
		    }
		}
		
		void
		write_membership_matrix22() {
		    System.out.println("Similarity matrix:\n");
		    for (int i = 0; i < num_data_points; i++) {
		    	System.out.print("Data[%d]: "+ i+")");
		        for (int j = 0; j < num_clusters; j++) {
		        	System.out.print("\t"+this.simility_data_kernel(i, j));
		        }
		        System.out.println();
		    }
		}

		void print_cluster(){
		    System.out.println("Clusters:\n");

			  for (int j = 0; j < num_clusters; j++) {
				  
			        for (int k = 0; k < num_dimensions; k++) {
			        	System.out.print("\t"+cluster_centre[j][k]);
			        }
			        System.out.println();
			    }
		}
		
		
		FuzzyClustering getDifClusters1(double max){
			
			List<double []> clearCluster=new LinkedList<double []>();
			
			for (int j = 0; j < num_clusters; j++) {
				boolean in=false;
				for(double [] c:clearCluster)
					if(maxDiff(c, cluster_centre[j])<max)
						in=true;
			
				if(!in)
					clearCluster.add(cluster_centre[j]);
			}
			
			double [][] cluster_centre2=new double [clearCluster.size()][num_dimensions];
			int i=0;
			for(double [] c:clearCluster) {
				for(int j=0;j<num_dimensions;j++)
					cluster_centre2[i][j]=c[j];
				i++;
			}
		//	public FuzzyClustering(double [][] cluster_centre,  int num_dimensions, int num_clusters,double fuzziness, int max_steps){

			return new FuzzyClustering(this.data_point, this.num_data_points,cluster_centre2, num_dimensions, clearCluster.size(), this.fuzziness, this.max_steps);
		}
		
		FuzzyClustering getDifClusters2(double max2){
			
			List<double []> clearCluster=new LinkedList<double []>();
			
			for (int j = 0; j < num_clusters; j++) {				
				double maxCluster=getMaxDegreeCluster(j)/getMaxDegreeClusters();
				System.out.println("\t cluster "+j+":"+maxCluster);
				if(maxCluster>max2)
					clearCluster.add(cluster_centre[j]);
			}
			
			double [][] cluster_centre2=new double [clearCluster.size()][num_dimensions];
			int i=0;
			for(double [] c:clearCluster) {
				for(int j=0;j<num_dimensions;j++)
					cluster_centre2[i][j]=c[j];
				i++;
			}
		//	public FuzzyClustering(double [][] cluster_centre,  int num_dimensions, int num_clusters,double fuzziness, int max_steps){

			return new FuzzyClustering(this.data_point, this.num_data_points,cluster_centre2, num_dimensions, clearCluster.size(), this.fuzziness, this.max_steps);
		}
		
		public double getMaxDegreeCluster(int c) {
			double max=Float.MIN_VALUE;
			for(int i=0;i<this.data_point.length;i++)
				if(this.degree_of_memb[i][c]>max)
					max=this.degree_of_memb[i][c];
					
			return max;
		}
		public double getMaxDegreeClusters() {
			double max=Float.MIN_VALUE;
			for(int i=0;i<this.num_clusters;i++)
				if(getMaxDegreeCluster(i)>max)
					max=getMaxDegreeCluster(i);
					
			return max;
		}
			
			public double maxDiff(double [] c1, double [] c2) {
				double max=0f;
				for (int k = 0; k < num_dimensions; k++) 
					if(Math.abs(c1[k]-c2[k])>max)
						max=Math.abs(c1[k]-c2[k]);
					
				return max;
			}
		
		void writeCluster(String fileName) throws Exception{
			BufferedWriter bw= new BufferedWriter(new FileWriter(new File(fileName).getAbsoluteFile()));	
	       	bw.write(this.num_clusters+"\n");
			 for (int j = 0; j < num_clusters; j++) {
				  
			        for (int k = 0; k < num_dimensions; k++) {
			        	bw.write("\t"+round(cluster_centre[j][k],1));
			        }
			        bw.write("\n");
			    }
			bw.close();
						
		}
		static int readSizeCluster(String fileName) throws Exception{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName("UTF-8")));
			String line=null;
			int count=0;
			 while ((line = br.readLine()) != null) {
				 int size=Integer.parseInt(line);
				 br.close();
				 return size;
			    }
			 br.close();
			 return -1;
						
		}
		
		static double get_norm0(int i, int j, int num_dimensions, double [][]data_point, double [][] cluster_centre) {
		    
		    double sum = 0.0f;
		    for (int k = 0; k < num_dimensions; k++) {
		        double v=Math.pow(data_point[i][k] - cluster_centre[j][k], 2);
		    //	double v=Math.abs(data_point[i][k] - cluster_centre[j][k]);
		        sum += v;
		    }
		    return (double)Math.sqrt(sum);
		}

		


		static double
		simility_data_kernel2(int i, int j, int num_dimensions, int num_clusters, double [][]data_point, double [][] cluster_centre,double fuzziness) {
		    double sum = 0.0f;
		    double p = 2 / ((double)fuzziness - 1);
		    for (int k = 0; k < num_clusters; k++) {
		        double t = get_norm0(i, j,num_dimensions,data_point,cluster_centre) / get_norm0(i, k,num_dimensions,data_point,cluster_centre);
		        t = (double)Math.pow(t, p);
		        sum += t;
		    }
		    if(sum==0) return (double)num_dimensions;
		    return 1.0f / sum;
		}
		

		
		/**static public void writeOutMembership(String fileName,Collection<Long> ts, int num_dimensions, int num_clusters, double [][]data_point, double [][]cluster_centre, double fuzziness)throws Exception{
			BufferedWriter bw= new BufferedWriter(new FileWriter(new File(fileName).getAbsoluteFile()));	
			bw.write("Time");
			for(int c=0;c<num_clusters;c++)
				bw.write("\t#"+c);
			bw.write("\n");
			int i=0;
			for(long t:ts){
				bw.write(LoadLayer.long2HHMM2(t));
				
				for(int c=0;c<num_clusters;c++)
					bw.write("\t"+simility_data_kernel(i,c,num_dimensions,num_clusters,data_point, cluster_centre, fuzziness));

				bw.write("\n");
				i++;
			}
			bw.close();
		}*/
		public static double round(double value, int places) {
		    if (places < 0) throw new IllegalArgumentException();

		    long factor = (long) Math.pow(10, places);
		    value = value * factor;
		    long tmp = Math.round(value);
		    return (double) tmp / factor;
		}
		
		static public void main(String [] args) throws Exception{
		    //double [][] data_point,int num_data_points, int num_clusters, int num_dimensions, double fuzziness, double epsylon
			
			int num_data_points=5;
			int num_dimensions=12;
			int num_clusters=3;
			//double [][] data_point=new double[num_data_points][num_dimensions];
			
//			data_point[0][0]=0f;
//			data_point[0][1]=0f;
//			
//			data_point[1][0]=1f;
//			data_point[1][1]=1f;
//
//			data_point[2][0]=0.2f;
//			data_point[2][1]=0.2f;
//
//			data_point[3][0]=0.9f;
//			data_point[3][1]=0.9f;

			
			double [][] data_point = { 
					 {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0}
                   };


			FuzzyClustering fc=new FuzzyClustering(data_point,num_data_points,num_dimensions,num_clusters,2d,100);
			
			fc.print_data_points0();
		    fc.fcm();
		    fc.print_membership_matrix0();
		    fc.print_cluster();
		    fc.print_membership_matrix22();
		    
		    
			double [] data_point2 = 
				
                   {0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0}
                  ;
		    
		    FuzzyClustering fc2=fc.getDifClusters1(0.2d);
		    fc2.update_degree_of_membership();
		    fc2=fc2.getDifClusters2(0.2d);
		    fc2.print_cluster();
		    fc2.setDataPoint1D(data_point2);
		    fc2.update_degree_of_membership();
		    fc2.print_membership_matrix0(); 
		    fc2.update_degree_of_membership();
		    fc2.print_membership_matrix22(); 		  
		    
		    
		    //0.0	0.0	1.0	0.0	0.0	0.0	0.0	0.0	0.0	0.0	0.0	0.0

	}
	
}


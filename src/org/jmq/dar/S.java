package org.jmq.dar;

import java.text.ParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * 
 * Class of sensor representation
 */
public class S extends A{

		public String sensor;

		
		public S(long t0, long tN, String sensor){
			super(t0,tN, sensor);
			this.sensor=sensor;
		}

		
		@Override
		public String toString(){
			return "["+t0+","+tN+"]"+"="+value;
		}

		
		public float isOn(long t0, long tN) throws ParseException{

			
			//Se pasan de los limites de la ventana
			if(tN<this.t0) return 0f;
			if(this.tN<t0) return 0f;
			
			//Sensor dentro de la ventana
			if(t0<this.t0&& this.tN<tN) return 1f;

			//Ventana dentro del sesnor
			if(this.t0<=t0&& tN<=this.tN) return 1f;
			//Si hay cambio de true a false
			if(t0<this.tN&&this.tN<tN) return 1f;
			//Si hay cambio de false a true
			if(t0<=this.t0&&this.t0<tN) return 1f;
			return 0f;
			
		}
		
		
		
//NUMERO DE SENSORES		
static public int sS=39;



/**
 * Para ordenar el vector en funcion del tiempo
 */
static public class SComparator implements Comparator<S> {
    @Override
    public int compare(S o1, S o2) {
        return (int)(o1.t0-o2.t0);
    }
}



//calcula el índice de un sensor el vector
static public int indexOfSensor(S s) throws Exception{
	return hSensor.get(s.sensor);
}
static public HashMap<String, Integer> hSensor=new HashMap<String, Integer> ();


static public int calculateIndexOfSensor(List<S> streamData) throws Exception{
	hSensor=new HashMap<String, Integer> ();
	int i=0;
	for(S s:streamData)
		if(!hSensor.containsKey(s.sensor)){
			System.out.println("\t s:"+s.sensor+"\t"+i);
			hSensor.put(s.sensor, i);
			i++;
		}
	return i;
}

static public int calculateIndexOfSensorX(List<S> streamData) throws Exception{
	int i=0;
	for(S s:streamData)
		if(!hSensor.containsKey(s.sensor)&&!s.sensor.startsWith("#R#")){
			System.out.println("\t s:"+s.sensor+"\t"+i);
			hSensor.put(s.sensor, i);
			i++;
		}
	return i;
}

static public int addSensorIndex2(List<String> newSensor) throws Exception{
	int i=hSensor.size();
	for(String s:newSensor){
			System.out.println("\t s:"+s+"\t"+i);
			hSensor.put(s, i);
			i++;
		}
	return i;
}


static public float isOn2(String label,List<S> streamData, long t0,long tN) throws ParseException{

	float w=0f;
	for(S s:streamData)
		if(s.sensor.equals(label)){
			//System.out.println(s.isOn2(t0, tN));
			w=Math.max(w,s.isOn(t0, tN));
		}
	
	
	return w;
}


static public S getCloserT0(long t, List<S> streamData){
	long minT=Long.MAX_VALUE;
	S ret=null;
	for(S s:streamData)
		if(Math.abs(t-s.t0)<minT){
			ret=s;
			minT=Math.abs(t-s.t0);
		}
	return ret;
}
static public S getCloserT0(long t, List<S> streamData, String sSensor){
	long minT=Long.MAX_VALUE;
	S ret=null;
	for(S s:streamData)
		if(s.sensor.equals(sSensor))
			if(Math.abs(t-s.t0)<minT){
			ret=s;
			minT=Math.abs(t-s.t0);
		}
	return ret;
}

static public S getCloserTN(long t, List<S> streamData, String sSensor){
	long minT=Long.MAX_VALUE;
	S ret=null;
	for(S s:streamData)
		if(s.sensor.equals(sSensor))
			if(Math.abs(t-s.tN)<minT){
			ret=s;
			minT=Math.abs(t-s.tN);
		}
	return ret;
}


static public S getCloser(List<S> streamData, long tN) throws ParseException{
	float minD=Float.MAX_VALUE;
	S ret=null;
	for(S s:streamData){
		if((tN-s.tN)>0&&(tN-s.tN)<minD){
			minD=(tN-s.tN);
			ret=s;
		}
		if((tN-s.t0)>0&&(tN-s.t0)<minD){
			minD=(tN-s.t0);
			ret=s;
		}
	}
//	System.out.println("\t"+tN+" is "+ret);
//	System.exit(1);
	return ret;
}


}


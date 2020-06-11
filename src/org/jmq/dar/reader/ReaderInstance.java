package org.jmq.dar.reader;

import java.text.ParseException;
import java.util.List;

import org.jmq.dar.A;
import org.jmq.dar.S;
/**
 * Operations to handle the dataset 
 *
 */
public interface ReaderInstance {

	public String long2Day2(long day) throws ParseException;
	public List<A> readAllAct(String file) throws Exception;
	public List<S> readAllData(String file ) throws Exception;
	public int getNumber(String id) throws Exception;
	public String getKey(String id) throws Exception;
//	public String getKey_1(String id) throws Exception;
	public List<String> getDays(List<A>  actStream) throws ParseException;
	public String idleId();
	public String getIdSensorCommand(String s);

}

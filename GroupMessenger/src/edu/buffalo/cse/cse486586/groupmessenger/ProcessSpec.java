package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.Serializable;
import java.util.Map;

public class ProcessSpec implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fromPort;
	private String msg;
	private int localNo;
	private int sequenceNO ;
	private int[] localVector= new int[5];
	private int avdNo;
	
	public ProcessSpec(String msg,int sequenceNO,String fromPort,int localNo) {
		
		super();
		this.msg = msg; 
		this.sequenceNO = sequenceNO;
		this.fromPort = fromPort;
		this.localNo = localNo;
	}

	int[] getLocalVector() {
		return localVector;
	}
	void setLocalVector(int[] localVector) {
		this.localVector = localVector;
	}
	String getMsg() {
		return msg;
	}

	void setMsg(String msg) {
		this.msg = msg;
	}

	int getSequenceNO() {
		return sequenceNO;
	}

	void setSequenceNO(int sequenceNO) {
		this.sequenceNO = sequenceNO;
	}
	int getLocalNo() {
		return localNo;
	}
	void setLocalNo(int localNo) {
		this.localNo = localNo;
	}
	int getAvdNo() {
		return avdNo;
	}

	void setAvdNo(int avdNo) {
		this.avdNo = avdNo;
	}

}

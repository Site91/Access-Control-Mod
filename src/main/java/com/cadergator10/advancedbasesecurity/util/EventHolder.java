package com.cadergator10.advancedbasesecurity.util;

public class EventHolder {
	//public int threadID;
	public String processId;
	public String data;
	public String data2;
	public String data3;
	public EventHolder(String pr, String... da){
		processId = pr;
		data = da.length > 0 ? da[0] : null;
		data2 = da.length > 1 ? da[1] : null;
		data3 = da.length > 2 ? da[2] : null;
	}
}

package com.cadergator10.advancedbasesecurity.util;

import org.apache.http.HttpResponse;

public class ResponseHolder {
	public HttpResponse response;
	public String[] args;
	public ResponseHolder(HttpResponse response, String... args){
		this.response = response;
		this.args = args;
	}
}

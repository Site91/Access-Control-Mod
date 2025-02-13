package com.cadergator10.advancedbasesecurity.util;

/**
 * Used by CardReader's text displays
 */
public class ReaderText{
	public String text;
	public byte color;
	public ReaderText(String text, byte color){
		this.text = text;
		this.color = color;
	}
	public String toString(){
		return String.format("{Text: %s | Color: %b}", text, color);
	}
}

package com.example.roboarmapp;

import java.io.Serializable;

public class DataPacket implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1817832267212300484L;

	public static final int TRANSMISSION = 201;
	
	public int type = TRANSMISSION;
	public float x;
	public float y;
	public float z;
	
	public DataPacket() {
		// Empty here.
	}
}

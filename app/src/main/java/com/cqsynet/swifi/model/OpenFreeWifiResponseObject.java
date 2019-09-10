package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class OpenFreeWifiResponseObject extends BaseResponseObject {
	
	public Body body;
    
	public class Body {
		public String time;
		public String mac;
		public ArrayList<String> link;
		public ArrayList<String> linkTime;
	}
}

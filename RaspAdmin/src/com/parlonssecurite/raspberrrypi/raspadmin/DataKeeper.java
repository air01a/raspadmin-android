package com.parlonssecurite.raspberrrypi.raspadmin;

public class DataKeeper {
	  public static class Raspi { public String host, user, password, name; int port ; boolean ssl; }	
	  public static boolean isActive=false;
	  private static DataKeeper.Raspi raspi = new Raspi(); 
	  
	  
	  public static Raspi getData() {return DataKeeper.raspi;}	  
	  public static void setData(String host, String user, String password) 
	  {  
		  raspi.host=host;
		  raspi.user=user;
		  raspi.password=password;
	  }
	  
	  public static void setData(String name, String host, String user, String password, int port, boolean ssl) 
	  {  
		  raspi.host=host;
		  raspi.user=user;
		  raspi.password=password;
		  raspi.name = name;
		  raspi.port = port;
		  raspi.ssl = ssl;
	  }
	 
}

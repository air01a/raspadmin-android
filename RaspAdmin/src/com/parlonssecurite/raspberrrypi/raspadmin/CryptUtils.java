package com.parlonssecurite.raspberrrypi.raspadmin;
import java.util.Random;


public class CryptUtils {
	
	//Generate a random string
	static  public String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, numchars);
    }
	
	// Xor a string with another one
	static  public String xorStrings(String str1, String str2){
		String data;
		String key;
		if (str1 == null)
			return null;
		if (str1.length()>str2.length()){
			data=str1;
			key=str2;
		}else{
			data=str2;
			key=str1;
		}
		
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < data.length(); i++) {
			sb.append((char)(data.charAt(i) ^ key.charAt(i % key.length())));
		}
		return sb.toString();
	}
}

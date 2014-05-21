package com.parlonssecurite.raspberrrypi.raspadmin;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DBManager {
	final int pErrorNotFound = 10;
	// Class for table JSON deserialization
	public class RaspPiIdentifier {
		  public String name;
		  public String host;
		  public String user;
	      public String password;
		  public int	 port;
		  public boolean ssl;
	      
	      public RaspPiIdentifier(String lname, String lhost, boolean lssl, String luser, String lpassword, int lport)
	      {
	    	  name=lname;
	    	  ssl=lssl;
	    	  host=lhost;
	    	  user=luser;
	    	  password=lpassword;
	    	  port=lport;
	      }
	 }
	private List<RaspPiIdentifier> pRaspiIdentifiers;
	
	// Manager information store
	@SuppressWarnings("unchecked")
	public DBManager(Context context, String repos, String varName, String key)
	{
		Gson gson = new Gson();
		SharedPreferences lockMode = context.getSharedPreferences(repos, Context.MODE_PRIVATE);
		String JsonDB = lockMode.getString(varName, null);
		String androidId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);

		JsonDB = CryptUtils.xorStrings(JsonDB, androidId);
		JsonDB = CryptUtils.xorStrings(JsonDB, key);

		Type listType = new TypeToken<List<RaspPiIdentifier>>(){}.getType();
		try {
			pRaspiIdentifiers = (List<RaspPiIdentifier>) gson.fromJson(JsonDB, listType);
		} catch (Exception e) {
			pRaspiIdentifiers = null;
		}
		if (pRaspiIdentifiers==null)
			pRaspiIdentifiers = new ArrayList<RaspPiIdentifier>();
	}
	
	public String[] getRaspNames()
	{
		String[] returnValue = new String[pRaspiIdentifiers.size()];
		for (int i=0;i < pRaspiIdentifiers.size(); i++) {
			returnValue[i]=pRaspiIdentifiers.get(i).name;
		}
		return returnValue;
	}
	
	public int addRaspberry(String name, String host, boolean ssl, String user, String password, int port)
	{
		RaspPiIdentifier raspi = new RaspPiIdentifier(name, host, ssl, user, password, port);
		pRaspiIdentifiers.add(raspi);
		return 0;
	}
	
	public RaspPiIdentifier getRaspi(String name)
	{
		for (int i=0;i < pRaspiIdentifiers.size(); i++)
			if (pRaspiIdentifiers.get(i).name.equals(name))
				return pRaspiIdentifiers.get(i);
		return null;
	}
	
	public int modifyRaspi(String name, String host, boolean ssl, String user, String password, int port)
	{
		RaspPiIdentifier current = getRaspi(name);
		if (current == null)
			return pErrorNotFound;
		
		current.host = host;
		current.ssl = ssl;
		current.user = user;
		current.password = password;
		current.port = port;
		return 0;
	}
	
	public int addOrEdit(String name, String host, boolean ssl, String user, String password, int port)
	{
		if (getRaspi(name)!=null) {
			return modifyRaspi(name,host,ssl,user,password,port);
		}
		else {
			return addRaspberry(name,host,ssl,user,password,port);
		}
		
	}
	
	public int saveDB(Context context, String repos, String varName, String key) {
		Gson gson = new Gson();
		String jsondump = gson.toJson(pRaspiIdentifiers); 
		SharedPreferences localDB = context.getSharedPreferences(repos, Context.MODE_PRIVATE);
		Editor edit = localDB.edit();
		edit.clear();
		String androidId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
		jsondump=CryptUtils.xorStrings(jsondump, androidId);
		jsondump=CryptUtils.xorStrings(jsondump, key);
		edit.putString(varName, jsondump);
		edit.commit();
		return 0;
	}
	
	public void delete(String name) {
		for (int i=0; i<pRaspiIdentifiers.size(); i++) {
			if (pRaspiIdentifiers.get(i).name==name) {
				pRaspiIdentifiers.remove(i);
				return;
			}
		}
		
	}
	
}

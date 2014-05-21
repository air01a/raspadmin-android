package com.parlonssecurite.raspberrrypi.raspadmin;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.os.AsyncTask;
import android.util.Log;
import javax.net.ssl.SSLSession;

public class HttpHandler extends AsyncTask<String, Void, String> {
	private Hashtable<String, String> pGetVars;
	private Hashtable<String, String> pPostVars;
	private MethodWrapper pCallBack;
	private MethodWrapper pErrorCallBack;
	private String pServerCookies;
	
	interface MethodWrapper {
	    public void call(String Cookie, String s); 
	}
	
	public HttpHandler(){
		pGetVars=new Hashtable<String, String>();
		pPostVars=new Hashtable<String, String>();
		pCallBack=null;
		pServerCookies=null;
		pErrorCallBack=null;
	}
	
	public void setCookie(String cookie)
	{
		pServerCookies=cookie;
	}
	
	
	public void setErrorCallBack(MethodWrapper func) {
		pErrorCallBack = func;
	}
	
	private String computeVars(Hashtable<String, String> params)
	{
		if(params.size() == 0)
			return "";
		
		StringBuffer buf = new StringBuffer();
		Enumeration<String> keys = params.keys();
		while(keys.hasMoreElements()) {
			buf.append(buf.length() == 0 ? "" : "&");
		    String key = keys.nextElement();
		    buf.append(key).append("=").append(params.get(key));
		}
		return buf.toString();
	}
	
	public void addGetVars(String name, String value)
	{
		pGetVars.put(name,value);
	}
	
	public void addPostVars(String name, String value)
	{
		pPostVars.put(name,value);
	}

	

	
	private static void disableSSLCertificateChecking() {

		
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// Not implemented
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				// Not implemented
			}
			}    
		};
				
		try {		
			SSLContext sc = SSLContext.getInstance("TLS");		 
			sc.init(null, trustAllCerts, new java.security.SecureRandom());	 
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public void setCallBack(MethodWrapper func) {
	    pCallBack=func;
	}
	
	
    protected String doInBackground(String... urls) {

    		disableSSLCertificateChecking();

            URL urlConnector=null;
            String url;
            HttpURLConnection connection=null;
            try {
            	url=urls[0];
            	String getvars=computeVars(pGetVars);
            	//Add getvars
            	if (getvars!="")
            		url+="?"+getvars;
            
            	urlConnector = new URL(url);
            	
            	final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
            	};
            	if (urlConnector.getProtocol().toLowerCase().equals("https")) {
                    
                    HttpsURLConnection https = (HttpsURLConnection) urlConnector.openConnection();
                    https.setHostnameVerifier(DO_NOT_VERIFY);
                    connection = https;
                } else {
                    connection = (HttpURLConnection) urlConnector.openConnection();
                }
                        	
            	
            	connection.setConnectTimeout(10000);
            	// Add cookies if requested
            	if (pServerCookies!=null)
            		connection.setRequestProperty("Cookie", pServerCookies);
            	// Add post vars
            	String postvars=computeVars(pPostVars);
            	if (postvars!="") {
            		connection.setRequestMethod("POST");
            		connection.setDoInput(true);
            		connection.setDoOutput(true);
            		
            		OutputStream os = connection.getOutputStream();
            		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            		writer.write(postvars);
            		writer.flush();
            		writer.close();
            		os.close();
            		
            	}
            	
            	
            	connection.connect();
            	
            	// Get cookies if needed
            	String cookies = connection.getHeaderField("Set-Cookie");
            	if (cookies!=null)
            		pServerCookies = cookies;
            	
            	// Get response
            	InputStream is = connection.getInputStream();
            	@SuppressWarnings("resource")
				java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            	return s.hasNext() ? s.next() : "";
            } catch (Exception e) {
                    e.printStackTrace();
                    if (pErrorCallBack!=null) {
                    	pErrorCallBack.call("Error","Error");
                    }
            } finally {
            	connection.disconnect();
            }
            
            return null;
    }
	
	protected void onPostExecute(String result) {
			if (result==null)
				result="Null :(";
			
			//If a callback function exists, call it
			if (pCallBack!= null)
				try {
					pCallBack.call(pServerCookies, result);
				} catch (Exception e) {
					Log.e("CallBack","failed");
					
				}
	      	Log.e("Final",result);
	    }
	
}
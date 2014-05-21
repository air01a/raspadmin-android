package com.parlonssecurite.raspberrrypi.raspadmin;

import java.lang.reflect.Type;
import java.util.List;

import com.parlonssecurite.raspberrrypi.raspadmin.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.webkit.SslErrorHandler;
import android.net.http.SslError;


@SuppressLint("SetJavaScriptEnabled")
public class WebActivity extends Activity {
	private WebView webView;
	private List<Post> pMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mMenuTitles;
    private ActionBarDrawerToggle mDrawerToggle;
	private final static int FILECHOOSER_RESULTCODE=1;
	private ValueCallback<Uri> mUploadMessage;

    // Class for JSON deserialization 
	public class DataObject {
		protected String token;
		protected String connected;
		protected String error;
		@Override
		public String toString() {
		   return "DataObject";
		}
	 
	}
	
	// Class for table JSON deserialization
	public class Post {
	      private String link;
	      private String name;
	      
	      public Post(String l, String n)
	      {
	    	  link=l;
	    	  name=n;
	      }
	 }
	
	// Get Data from Json using GSON lib
	private DataObject fromJson(String json)
	{
		Gson gson = new Gson();

		//convert the json string back to object
		DataObject obj = gson.fromJson(json, DataObject.class);
		return obj;
	}
	
	
	// Get Menu elements
	@SuppressWarnings("unchecked")
	private void getMenu(String json)
	{
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Post>>(){}.getType();
		pMenu = (List<Post>) gson.fromJson(json, listType);
		pMenu.add(new Post("/?num_logout=1","Logout"));
	}
	
	private void logout()
	{
		DataKeeper.Raspi params = DataKeeper.getData();
		webView = (WebView) findViewById(R.id.webView);
		webView.loadUrl(params.host+"/?num_logout=1");
		finish();
	}
	// Load Menu with webservice
	public void connectGetMenu(String cookie, String s)
	{
		getMenu(s);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);		
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mMenuTitles= new String[pMenu.size()];
        
        for (int i=0 ;i < pMenu.size();i++)
        	mMenuTitles[i]=pMenu.get(i).name;


        mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, mMenuTitles));
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener(){
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		DataKeeper.Raspi params = DataKeeper.getData();
        		webView = (WebView) findViewById(R.id.webView);
        		webView.loadUrl(params.host+"/"+pMenu.get(position).link);
        		mDrawerLayout.closeDrawer(mDrawerList);
        		if (pMenu.get(position).name=="Logout")
        			finish();
        	}
        });
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
	}
	
	private void showError(String error) {
		webView = (WebView) findViewById(R.id.webView);
		StringBuilder html = new StringBuilder();   
        html.append("<html>");
        html.append("<head>");
        html.append("<link rel='stylesheet' type='text/css' href='style.css'>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>Error</h1>");
        html.append("<div id='error'><p>"+error+"</p></div>");
        html.append("</body></html>");   
        webView.loadDataWithBaseURL("file:///android_asset/html/", html.toString(), "text/html", "UTF-8", "");   
		
	}
	
	
	// Authenticate with token from step 1
	public void connectStep2(String cookie, String s)
	{
		DataObject data = fromJson(s);
		DataKeeper.Raspi params = DataKeeper.getData();
		
		if (data.connected=="true") {
			
			webView = (WebView) findViewById(R.id.webView);
			webView.getSettings().setJavaScriptEnabled(true);
			CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(webView.getContext());
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.setCookie(params.host, cookie);
			cookieSyncManager.sync();
			webView.loadUrl(params.host);
			
			HttpHandler httpHandler = new HttpHandler();
			httpHandler.setCookie(cookie);
			httpHandler.setCallBack(new HttpHandler.MethodWrapper() {
		        public void call(String cookie, String s) {
		            connectGetMenu(cookie,s);
		        } }
		    );
			httpHandler.execute(params.host+"/mobile/");
		} else 
			showError("Error during authentication process.<br>Are you sure about login & password ? ");
	}
	
	// Get Token from Json for posting authentication forms
	public void connect(String cookie, String s)
	{
		
		DataObject data = fromJson(s);
		DataKeeper.Raspi params = DataKeeper.getData();
		
		HttpHandler httpHandler = new HttpHandler();
		httpHandler.addGetVars("alphanum_json","true");
		httpHandler.addPostVars("str_password",params.password);
		httpHandler.addPostVars("alphanum_login",params.user);
		httpHandler.addPostVars("alphanum_token",data.token);
		httpHandler.setCookie(cookie);
		httpHandler.setCallBack(new HttpHandler.MethodWrapper() {
	        public void call(String cookie, String s) {
	            connectStep2(cookie,s);
	        } }
	    );
		httpHandler.setErrorCallBack(new HttpHandler.MethodWrapper() {
	        public void call(String cookie, String s) {
	            showError("Error connecting raspi, are you sure of it's address ? ");
	        } }
	    );
		httpHandler.execute(params.host);
	}
	
	
	@Override  
	 protected void onActivityResult(int requestCode, int resultCode, Intent intent) {  
	  if(requestCode==FILECHOOSER_RESULTCODE)  
	  {  
		  if (null == mUploadMessage) return;  
	      Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();  
	      mUploadMessage.onReceiveValue(result);  
	      mUploadMessage = null;  
	              
	  }  
	 }  
	// On create
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.webcontent);
		// Get Data send by main activity
		DataKeeper.Raspi data = DataKeeper.getData();
		
		// Initialize WebView
		webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(false);
		webView.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.e("URL",url);
				if (url.startsWith("http"))
					return false;
				return true;
			}
			 @Override
			 public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			     handler.proceed(); // Ignore SSL certificate errors
			 }
		});
		
		
		webView.setWebChromeClient(new WebChromeClient(){
			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
				mUploadMessage = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);
			}
			@SuppressWarnings("unused")
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				mUploadMessage = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);  
			}

				// For Android 3.0+
			@SuppressWarnings("unused")
			public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType ) {
				mUploadMessage = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);  
				}
			

		});
		
		// Show a page for waiting
        StringBuilder html = new StringBuilder();   
        html.append("<html>");
        html.append("<head>");
        html.append("<link rel='stylesheet' type='text/css' href='style.css'>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>Connecting to "+data.host+"</h1>");
        html.append("<div id='authentication'><p>Please Wait</p></div>");
        html.append("</body></html>");   
        webView.loadDataWithBaseURL("file:///android_asset/html/", html.toString(), "text/html", "UTF-8", "");    
                
        // Let's go for authentication process
		HttpHandler httpHandler = new HttpHandler();
		httpHandler.addGetVars("alphanum_json","true");
		// On webservice request end, let's got to step 1 for authent
		httpHandler.setCallBack(new HttpHandler.MethodWrapper() {
	        public void call(String cookie, String s) {
	            connect(cookie, s);
	        } }
	    );
		
		httpHandler.setErrorCallBack(new HttpHandler.MethodWrapper() {
	        public void call(String cookie, String s) {
	            showError("Error connecting raspi, are you sure of it's address ? ");
	        } }
	    );
		httpHandler.execute(data.host);    
	}
	@Override  
	public void onBackPressed() {
	    super.onBackPressed();   
	    // Do extra stuff here
	    logout();
	}
}
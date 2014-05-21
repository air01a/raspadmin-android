package com.parlonssecurite.raspberrrypi.raspadmin;
import java.util.ArrayList;
import java.util.Arrays;

import com.parlonssecurite.raspberrrypi.raspadmin.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.haibison.android.lockpattern.LockPatternActivity;

public class MainActivity extends Activity {
	private static final int REQ_CREATE_PATTERN = 1;
	private static final int REQ_ENTER_PATTERN = 2;
	private static final int REQ_ADD_HOST = 3;
	private Context pContext;
	private String pKey;
	private String[] pMenuStr;
	private DBManager pDB;
	
	public void onCreate(Bundle savedInstanceState) {
		this.pContext = this;
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		super.onCreate(savedInstanceState);
		
		// Get cipher key
		SharedPreferences lockMode = this.pContext.getSharedPreferences("lockmode", MODE_PRIVATE);
		String uniqKey = lockMode.getString("uniqKey", "");
		String savedPattern = lockMode.getString("pattern","");
		
		
		if (uniqKey.equals("") || savedPattern.equals("")) {
			uniqKey = CryptUtils.getRandomHexString(20);
			Editor edit = lockMode.edit();
			edit.clear();
			edit.putString("uniqKey",uniqKey);
			edit.commit();
			Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, this.pContext, LockPatternActivity.class);
			startActivityForResult(intent,REQ_CREATE_PATTERN);
		} else {
			Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,this.pContext, LockPatternActivity.class);
			String androidId = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
			
			this.pKey = CryptUtils.xorStrings(savedPattern,uniqKey);
			this.pKey = CryptUtils.xorStrings(this.pKey,androidId);
			intent.putExtra(LockPatternActivity.EXTRA_PATTERN, this.pKey.toCharArray());
			startActivityForResult(intent, REQ_ENTER_PATTERN);
		}

	}

	private void mainActivity(){
		setContentView(R.layout.main);
		final Context context=this.pContext;
		pDB = new DBManager(this, "DB","rasp",pKey);
		ListView mainListView = (ListView) findViewById( R.id.listView1 );
		// Create and populate a List of planet names.
	    this.pMenuStr = pDB.getRaspNames();  
	    ArrayList<String> raspPi = new ArrayList<String>();
	    raspPi.addAll( Arrays.asList(this.pMenuStr) );
	    
	    // Create ArrayAdapter using the planet list.
	    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, raspPi);
	    
	    // Add more planets. If you passed a String[] instead of a List<String> 
	    // into the ArrayAdapter constructor, you must not add more items. 
	    // Otherwise an exception will occur.
	    listAdapter.add( "Add Raspberry pi" );
	    
	    // Set the ArrayAdapter as the ListView's adapter.
	    mainListView.setAdapter( listAdapter );      
	    mainListView.setClickable(true);
	    mainListView.setOnItemClickListener(new OnItemClickListener()
	    {
	    	public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
	    	{
	    		
			    String name=(String) ((TextView) v).getText();
			    if (!name.equals("Add Raspberry pi")) {	
			    	Intent intent = new Intent(context, WebActivity.class);
			    	DBManager.RaspPiIdentifier raspi = pDB.getRaspi(name);
			    	String url;
			    	if (raspi.ssl)
			    		url="https";
			    	else
			    		url="http";
			    	url+="://"+raspi.host+':'+String.valueOf(raspi.port);
			    	
			    	DataKeeper.setData(url,raspi.user, raspi.password);
			    	
			    	startActivity(intent);
			    	Toast.makeText(getApplicationContext(), ((TextView) v).getText(),
			    			Toast.LENGTH_SHORT).show();
			    } else {
			    	Intent intent = new Intent(context, AddHost.class);
			    	startActivityForResult(intent,REQ_ADD_HOST);
			    }
	    	}
	    });
	    /*
	    mainListView.setOnItemLongClickListener(new OnItemLongClickListener()
	    {
	    	public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id)
	    	{
	    		Toast.makeText(getApplicationContext(), ((TextView) v).getText()+" long",
	    	            Toast.LENGTH_SHORT).show();
	    		return true;
	    	}
	    	
	    
	    });
	    
	    button = (Button) findViewById(R.id.buttonUrl);
		button.setOnClickListener(new OnClickListener() {

		  @Override
		  public void onClick(View arg0) {
		    Intent intent = new Intent(context, WebActivity.class);
		    startActivity(intent);
		  }
		});
	    */
	    registerForContextMenu(mainListView);
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	  if (v.getId()==R.id.listView1) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    if (info.position >= this.pMenuStr.length)
	    	return;
	    menu.setHeaderTitle(this.pMenuStr[info.position]);
	    
	    menu.add(Menu.NONE, 0, 0, "Edit");
	    menu.add(Menu.NONE, 1, 1, "Delete");
	    menu.add(Menu.NONE, 2, 2, "Cancel");
	  }
	}
	public void deleteRasp(String name)
	{
		pDB.delete(name);
		pDB.saveDB(this, "DB","rasp",pKey);
		mainActivity();
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  String[] menu = new String[] { "Edit" , "Delete","Cancel"};
	  if (menuItemIndex==1) {
		  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		  final String toDelete = pMenuStr[info.position];
		  new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.deletion)
	        .setMessage(R.string.really_delete)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	              deleteRasp(toDelete);      
	            }

	        })
	        .setNegativeButton(R.string.no, null)
	        .show();
		  
	  } else if (menuItemIndex==0) {
		  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		  String toEdit = pMenuStr[info.position];
		  DBManager.RaspPiIdentifier raspi = pDB.getRaspi(toEdit);
		  DataKeeper.setData(raspi.name, raspi.host, raspi.user, raspi.password, raspi.port, raspi.ssl);
		  DataKeeper.isActive=true;
		  Intent intent = new Intent(this, AddHost.class);
	      startActivityForResult(intent,REQ_ADD_HOST);
	  }
	  Toast.makeText(getApplicationContext(), menu[menuItemIndex], Toast.LENGTH_SHORT).show();

	  return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	        Intent data) {
	    switch (requestCode) {
	    case REQ_CREATE_PATTERN: {
	        if (resultCode == RESULT_OK) {
	        	
	            String pattern = new String(data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN));
	            SharedPreferences lockMode = this.pContext.getSharedPreferences("lockmode", MODE_PRIVATE);
	            String uniqKey = lockMode.getString("uniqKey", "");
	            String androidId = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);
	            String cryptedPattern = CryptUtils.xorStrings(pattern, uniqKey);
	            cryptedPattern=CryptUtils.xorStrings(cryptedPattern,androidId);
	            Editor edit = lockMode.edit();
	            edit.putString("pattern",cryptedPattern);
	            this.pKey = pattern;          
	            edit.commit();
	            mainActivity();
	        } else 
	        	finish();
	        break;
	    }// REQ_CREATE_PATTERN
	    case REQ_ENTER_PATTERN : {
	    	switch (resultCode) {
	    		case RESULT_OK:
	    			// The user passed
	    			mainActivity();
	    			break;
	    		case RESULT_CANCELED:
	    			// The user cancelled the task
	    			finish();
	    			break;
	    		case LockPatternActivity.RESULT_FAILED:
	    		    Intent intent = new Intent(this.pContext, PatternRecoveryActivity.class);
	    		    startActivity(intent);
	    		    finish();
	    			break;
	    		case LockPatternActivity.RESULT_FORGOT_PATTERN:
	    		// The user forgot the pattern and invoked your recovery Activity.
	    			break;
	        	}
	         
	         	//int retryCount = data.getIntExtra(LockPatternActivity.EXTRA_RETRY_COUNT, 0);
	         	
	         	break;
	     	}
	    case REQ_ADD_HOST : {
	    	if (resultCode==RESULT_OK) {
	    		DataKeeper.Raspi raspi = DataKeeper.getData();
	    		pDB.addOrEdit(raspi.name, raspi.host,raspi.ssl,raspi.user,raspi.password,raspi.port);
	    		pDB.saveDB(this, "DB","rasp",pKey);
	    		mainActivity();
	    	}
	      }
	    }
	}
}

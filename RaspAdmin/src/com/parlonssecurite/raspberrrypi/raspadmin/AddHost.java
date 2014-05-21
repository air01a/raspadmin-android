package com.parlonssecurite.raspberrrypi.raspadmin;

import com.parlonssecurite.raspberrrypi.raspadmin.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddHost extends Activity {
	private Button button;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.addhost);
	    // Check if this activity was launch for adding or mod
	    if (DataKeeper.isActive) {
	    	// If mod, show datas
	    	DataKeeper.Raspi data = DataKeeper.getData();
	    	
	    	((EditText)findViewById(R.id.et_host)).setText(data.host);
			((EditText)findViewById(R.id.et_user)).setText(data.user);
		    ((EditText)findViewById(R.id.et_password)).setText(data.password);
			((EditText)findViewById(R.id.et_port)).setText(String.valueOf(data.port));
			((EditText)findViewById(R.id.et_name)).setText(data.name);
			((EditText)findViewById(R.id.et_name)).setFocusable(false);
			((CheckBox) findViewById(R.id.cb_ssl)).setChecked(data.ssl);
			DataKeeper.isActive=false;
	    }
	    // On Send click, modify information
	    button = (Button) findViewById(R.id.buttonaddhost);
		button.setOnClickListener(new OnClickListener() {

		  @Override
		  public void onClick(View arg0) {
			  String host =  ((EditText)findViewById(R.id.et_host)).getText().toString();
			  String user =  ((EditText)findViewById(R.id.et_user)).getText().toString();
			  String password =  ((EditText)findViewById(R.id.et_password)).getText().toString();
			  String port =  ((EditText)findViewById(R.id.et_port)).getText().toString();
			  String name =  ((EditText)findViewById(R.id.et_name)).getText().toString();
			  int pport;
			  try {
				  pport = Integer.valueOf(port);
			  } catch (Exception e) {
			  	  pport = 7443;
			  }
			  
			  boolean ssl=((CheckBox) findViewById(R.id.cb_ssl)).isChecked();
			  DataKeeper.setData(name, host, user, password, pport, ssl);
			  Intent returnIntent = new Intent();
			  setResult(RESULT_OK,returnIntent);    
			  finish();
		  }
		});
	    // TODO Auto-generated method stub
	}

}

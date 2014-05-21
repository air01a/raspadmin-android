package com.parlonssecurite.raspberrrypi.raspadmin;

import com.parlonssecurite.raspberrrypi.raspadmin.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PatternRecoveryActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.patternrecovery);
	    
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SharedPreferences lockMode = arg0.getContext().getSharedPreferences("lockmode", MODE_PRIVATE);
				Editor edit = lockMode.edit();
				edit.clear();
				edit.commit();
				SharedPreferences DB = arg0.getContext().getSharedPreferences("DB", MODE_PRIVATE);
				edit = DB.edit();
				edit.clear();
				edit.commit();
				finish();
			}
		});
	}
}

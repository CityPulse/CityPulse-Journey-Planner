package com.siemens.parkingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class RouteConstraintsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_constraints);
		
		
		Button okBtn = (Button)findViewById(R.id.okButton4);
		Button cancelBtn = (Button)findViewById(R.id.cancelButton2);
		
		final Intent intent = getIntent();
	    final Bundle bundle = new Bundle();
	    
	    
	    okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				boolean fastest = false;
				boolean shortest = false;
				boolean cleaner = false;
				
				CheckBox fastestCheckBox = (CheckBox)findViewById(R.id.fastestCheckBox);
				CheckBox shortestCheckBox = (CheckBox)findViewById(R.id.shortestCheckBox);
				CheckBox cleanerCheckBox = (CheckBox)findViewById(R.id.cleanerCheckBox);
				
				if(fastestCheckBox.isChecked())
				{
					fastest = true;
				}
								
				if(shortestCheckBox.isChecked())
				{
					shortest = true;
				}
				
				if(cleanerCheckBox.isChecked())
				{
					cleaner = true;
				}
				
				bundle.putBoolean("fastest", fastest);
				bundle.putBoolean("shortest", shortest);
				bundle.putBoolean("cleaner", cleaner);
				intent.putExtras(bundle);
				setResult(RESULT_OK,intent);
				finish();
				
			}
		});
		
		
	    cancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				setResult(RESULT_CANCELED);				
				finish();
				
			}
		});
		
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.route_constraints, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

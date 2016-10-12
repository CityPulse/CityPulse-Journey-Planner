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

public class ParkingPlaceConstraintsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_place_constraints);

		Button okBtn = (Button)findViewById(R.id.okButton3);
		Button cancelBtn = (Button)findViewById(R.id.cancelButton1);
		
		final Intent intent = getIntent();
	    final Bundle bundle = new Bundle();
		
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				boolean cheapest = false;
				boolean shortestWalk = false;
				CheckBox cheapestCheckBox = (CheckBox)findViewById(R.id.cheapestCheckBox);
				CheckBox shortestWalkCheckBox = (CheckBox)findViewById(R.id.shortestWalkCheckBox);
				if(cheapestCheckBox.isChecked())
				{
					cheapest = true;
				}
				if(shortestWalkCheckBox.isChecked())
				{
					shortestWalk = true;
				}
			    bundle.putBoolean("cheapest", cheapest);
			    bundle.putBoolean("shortestWalk", shortestWalk);
			    intent.putExtras(bundle);
			    setResult(RESULT_OK, intent);
			    finish();
				
			}
		});
		cancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//intent.putExtras(bundle);
				setResult(RESULT_CANCELED);				
				finish();
				
				
			}
		});
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.parking_place_constraints, menu);
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

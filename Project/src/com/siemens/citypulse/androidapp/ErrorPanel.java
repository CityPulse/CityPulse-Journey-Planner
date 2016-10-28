package com.siemens.citypulse.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This activity is used to display error messages for the user. Mainly when the
 * back-end server is not accessible.
 * 
 * @author dan.puiu
 *
 */

public class ErrorPanel extends Activity {

	private TextView errorTextField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_error);

		errorTextField = (TextView) findViewById(R.id.errorTextField);

		Intent intent = getIntent();

		errorTextField.setText(intent.getStringExtra("Error"));

	}

}

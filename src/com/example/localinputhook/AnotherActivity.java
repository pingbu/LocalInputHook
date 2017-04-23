package com.example.localinputhook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

public class AnotherActivity extends Activity implements View.OnClickListener {

	private static final String TAG = AnotherActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_another);
		findViewById(R.id.button_main_activity).setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown keyCode=" + keyCode + " event=" + event);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_main_activity:
			startActivity(new Intent(this, MainActivity.class));
			break;
		}
	}
}

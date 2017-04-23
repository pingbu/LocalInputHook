package com.example.localinputhook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener, View.OnKeyListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.button_dialog).setOnClickListener(this);
		findViewById(R.id.button_window).setOnClickListener(this);
		findViewById(R.id.button_another_activity).setOnClickListener(this);
		findViewById(R.id.button_another_process).setOnClickListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown keyCode=" + keyCode + " event=" + event);
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_dialog:
			new AlertDialog.Builder(this).setMessage(R.string.button_dialog).create().show();
			break;

		case R.id.button_window:
			WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 0,
					PixelFormat.RGBA_8888);
			TextView v = new TextView(this);
			v.setText(R.string.button_window);
			v.setOnKeyListener(this);
			getWindowManager().addView(v, params);
			break;

		case R.id.button_another_activity:
			startActivity(new Intent(this, AnotherActivity.class));
			break;

		case R.id.button_another_process:
			startActivity(new Intent(this, AnotherProcess.class));
			break;
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			getWindowManager().removeView(v);
			return true;
		}
		return false;
	}
}

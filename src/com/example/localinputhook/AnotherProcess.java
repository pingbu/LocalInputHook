package com.example.localinputhook;

import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;

public class AnotherProcess extends Activity {

	private static final String TAG = AnotherProcess.class.getSimpleName();

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown keyCode=" + keyCode + " event=" + event);
		return super.onKeyDown(keyCode, event);
	}
}

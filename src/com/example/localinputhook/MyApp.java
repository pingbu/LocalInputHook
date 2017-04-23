package com.example.localinputhook;

import android.app.Application;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;

public class MyApp extends Application {

	private static final String TAG = MyApp.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		LocalInputHook.init(this, new LocalInputHook.Handler() {
			@Override
			public boolean onPreInput(int seq, InputEvent event) {
				Log.i(TAG, "onPreInput seq=" + seq + ", event=" + event);
				if (event instanceof KeyEvent && ((KeyEvent) event).getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
					Log.e(TAG, "hooked!!!");
					return true;
				}
				return false;
			}

			@Override
			public void onPostInput(int seq, boolean handled) {
				Log.i(TAG, "onPostInput seq=" + seq + ", handled=" + handled);
			}
		});
	}
}

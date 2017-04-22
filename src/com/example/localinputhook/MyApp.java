package com.example.localinputhook;

import android.app.Application;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;

public class MyApp extends Application {

	private static final String TAG = MyApp.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		LocalInputHook.init(this, new LocalInputHook.Handler() {
			@Override
			public void onPreInput(int seq, InputEvent event) {
				Log.d(TAG, "onPreInput seq=" + seq + ", event=" + event);
				if (event.isFromSource(InputDevice.SOURCE_KEYBOARD)
						&& ((KeyEvent) event).getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
					Log.d(TAG, "hooked!!!");
				} else {
					super.onPreInput(seq, event);
				}
			}

			@Override
			public void onPostInput(int seq, boolean handled) {
				Log.d(TAG, "onPostInput seq=" + seq + ", handled=" + handled);
				super.onPostInput(seq, handled);
			}
		});
	}
}

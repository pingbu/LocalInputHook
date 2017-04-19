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
		LocalInputHook.init(this, new LocalInputHook.Listener() {
			@Override
			public boolean onHookInput(InputEvent event) {
				Log.d(TAG, "onHookInput event=" + event);
				if (event.isFromSource(InputDevice.SOURCE_KEYBOARD)
						&& ((KeyEvent) event).getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
					Log.d(TAG, "hooked!!!");
					return true;
				}
				return false;
			}
		});
	}
}

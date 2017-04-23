package com.example.localinputhook;

import android.util.Log;

public class Utils {
	public static void logD(String tag, String fmt, Object... args) {
		if (BuildConfig.DEBUG)
			Log.d(tag, String.format(fmt, args));
	}
}

package com.example.localinputhook;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.view.IWindowSession;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputEventSender;

public final class LocalInputHook {

	protected static final String TAG = LocalInputHook.class.getSimpleName();

	public static abstract class Handler {
		public void onPreInput(int seq, InputEvent event) {
			sInputEventSender.sendInputEvent(seq, event);
		}

		public void onPostInput(int seq, boolean handled) {
		}
	}

	private static int sSeq = 0;
	private static Handler sHandler;
	private static IWindowSession sWindowSession;
	private static InputEventSender sInputEventSender;

	private static InvocationHandler sWindowSessionHook = new InvocationHandler() {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().startsWith("add")
					&& method.getParameterTypes()[args.length - 1].equals(InputChannel.class)) {
				Log.d(TAG, "invoke sWindowSession." + method.getName());

				InputChannel oldInputChannel = (InputChannel) args[args.length - 1];
				InputChannel newInputChannel = new InputChannel();

				args[args.length - 1] = newInputChannel;
				Object r = method.invoke(sWindowSession, args);

				new InputEventReceiver(newInputChannel, Looper.myLooper()) {
					@Override
					public void onInputEvent(InputEvent event) {
						int seq = sSeq++;
						if (sHandler != null)
							sHandler.onPreInput(seq, event);
						else
							sInputEventSender.sendInputEvent(seq, event);
						super.onInputEvent(event);
					}
				};

				InputChannel[] inputChannels = InputChannel
						.openInputChannelPair(TAG + "#" + android.os.Process.myPid());
				inputChannels[1].transferTo(oldInputChannel);

				sInputEventSender = new InputEventSender(inputChannels[0], Looper.myLooper()) {
					@Override
					public void onInputEventFinished(int seq, boolean handled) {
						if (sHandler != null)
							sHandler.onPostInput(seq, handled);
						super.onInputEventFinished(seq, handled);
					}
				};

				return r;
			}

			return method.invoke(sWindowSession, args);
		}
	};

	public static boolean init(Context context, Handler handler) {
		try {
			Class<?> wmGlobalClass = Class.forName("android.view.WindowManagerGlobal");
			Field sWindowSessionFiled = wmGlobalClass.getDeclaredField("sWindowSession");
			sWindowSessionFiled.setAccessible(true);

			sWindowSession = (IWindowSession) sWindowSessionFiled.get(null);
			if (sWindowSession != null)
				throw new Exception(TAG + " must be init once while app create");

			sHandler = handler;
			sWindowSession = (IWindowSession) wmGlobalClass.getMethod("getWindowSession").invoke(null);
			sWindowSessionFiled.set(null, Proxy.newProxyInstance(context.getClassLoader(),
					new Class<?>[] { IWindowSession.class }, sWindowSessionHook));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}

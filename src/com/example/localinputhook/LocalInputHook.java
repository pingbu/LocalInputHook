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

	public interface Listener {
		public boolean onHookInput(InputEvent event);
	}

	private static int sSeq = 0;
	private static Listener sListener;
	private static IWindowSession sWindowSession;
	private static InputEventSender sInputEventSender;

	private static InvocationHandler sWindowSessionHook = new InvocationHandler() {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (method.getName().startsWith("add")
					&& method.getParameterTypes()[args.length - 1]
							.equals(InputChannel.class)) {
				Log.d(TAG, "invoke sWindowSession." + method.getName());

				InputChannel oldInputChannel = (InputChannel) args[args.length - 1];
				InputChannel newInputChannel = new InputChannel();

				args[args.length - 1] = newInputChannel;
				Object r = method.invoke(sWindowSession, args);

				new InputEventReceiver(newInputChannel, Looper.myLooper()) {
					@Override
					public void onInputEvent(InputEvent event) {
						if (sListener == null || !sListener.onHookInput(event))
							sInputEventSender.sendInputEvent(sSeq++, event);
						super.onInputEvent(event);
					}
				};

				InputChannel[] inputChannels = InputChannel
						.openInputChannelPair(TAG + "#"
								+ android.os.Process.myPid());
				inputChannels[1].transferTo(oldInputChannel);

				sInputEventSender = new InputEventSender(inputChannels[0],
						Looper.myLooper()) {
				};

				return r;
			}

			return method.invoke(sWindowSession, args);
		}
	};

	public static boolean init(Context context, Listener listener) {
		try {
			Class<?> wmGlobalClass = Class
					.forName("android.view.WindowManagerGlobal");
			Field sWindowSessionFiled = wmGlobalClass
					.getDeclaredField("sWindowSession");
			sWindowSessionFiled.setAccessible(true);

			sWindowSession = (IWindowSession) sWindowSessionFiled.get(null);
			if (sWindowSession != null)
				throw new Exception(TAG + " must be init once while app create");

			sListener = listener;
			sWindowSession = (IWindowSession) wmGlobalClass.getMethod(
					"getWindowSession").invoke(null);
			sWindowSessionFiled.set(null,
					Proxy.newProxyInstance(context.getClassLoader(),
							new Class<?>[] { IWindowSession.class },
							sWindowSessionHook));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}

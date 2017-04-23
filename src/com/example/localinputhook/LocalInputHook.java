package com.example.localinputhook;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Looper;
import android.view.IWindowSession;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputEventSender;

public final class LocalInputHook {

	protected static final String TAG = LocalInputHook.class.getSimpleName();

	public interface Handler {
		public boolean onPreInput(int seq, InputEvent event);

		public void onPostInput(int seq, boolean handled);
	}

	private static int sHookNum = 0, sSeq = 0;
	private static Handler sHandler;
	private static IWindowSession sWindowSession;
	private static Map<Object, WindowInputHook> sWindowInputHooks = new HashMap<Object, WindowInputHook>();

	private static class WindowInputHook {
		private InputChannel mOldInputChannel, mNewInputChannel;
		private InputChannel[] mInputChannelPair;
		private InputEventReceiver mInputEventReceiver;
		private InputEventSender mInputEventSender;

		private class MyInputEventReceiver extends InputEventReceiver {

			public MyInputEventReceiver() {
				super(mNewInputChannel, Looper.myLooper());
			}

			@Override
			protected void finalize() throws Throwable {
				Utils.logD(TAG, "MyInputEventReceiver.finalize");
				super.finalize();
			}

			@Override
			public void onInputEvent(InputEvent event) {
				if (mInputEventSender != null) {
					int seq = sSeq++;
					if (sHandler == null || !sHandler.onPreInput(seq, event))
						mInputEventSender.sendInputEvent(seq, event);
				}
				super.onInputEvent(event);
			}
		}

		private class MyInputEventSender extends InputEventSender {
			public MyInputEventSender(InputChannel inputChannel) {
				super(inputChannel, Looper.myLooper());
			}

			@Override
			protected void finalize() throws Throwable {
				Utils.logD(TAG, "MyInputEventSender.finalize");
				super.finalize();
			}

			@Override
			public void onInputEventFinished(int seq, boolean handled) {
				if (sHandler != null)
					sHandler.onPostInput(seq, handled);
				super.onInputEventFinished(seq, handled);
			}
		};

		public WindowInputHook(InputChannel inputChannel) {
			mOldInputChannel = inputChannel;
			mNewInputChannel = new InputChannel();
		}

		@Override
		protected void finalize() throws Throwable {
			Utils.logD(TAG, "WindowInputHook.finalize");
			super.finalize();
		}

		public InputChannel getInputChannel() {
			return mNewInputChannel;
		}

		public void startHook() {
			mInputEventReceiver = new MyInputEventReceiver();

			mInputChannelPair = InputChannel
					.openInputChannelPair(TAG + "@" + android.os.Process.myPid() + "#" + sHookNum++);
			mInputChannelPair[1].transferTo(mOldInputChannel);

			mInputEventSender = new MyInputEventSender(mInputChannelPair[0]);
		}

		public void dispose() {
			Utils.logD(TAG, "WindowInputHook.dispose");
			mInputEventReceiver.dispose();
			mInputEventSender.dispose();
			mInputChannelPair[1].dispose();
			mInputChannelPair[0].dispose();
			mOldInputChannel.dispose();
			mNewInputChannel.dispose();
		}
	}

	private static InvocationHandler sWindowSessionHook = new InvocationHandler() {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Utils.logD(TAG, "invoke sWindowSession.%s", method.getName());

			if (method.getName().startsWith("add")
					&& method.getParameterTypes()[args.length - 1].equals(InputChannel.class)) {

				WindowInputHook hook = new WindowInputHook((InputChannel) args[args.length - 1]);
				sWindowInputHooks.put(args[0], hook);

				args[args.length - 1] = hook.getInputChannel();
				Object r = method.invoke(sWindowSession, args);

				hook.startHook();
				return r;
			} else if (method.getName().equals("remove")) {
				WindowInputHook hook = sWindowInputHooks.remove(args[0]);
				if (hook != null)
					hook.dispose();
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

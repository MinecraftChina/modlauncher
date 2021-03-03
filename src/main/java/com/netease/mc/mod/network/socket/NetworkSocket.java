package com.netease.mc.mod.network.socket;

import com.netease.mc.mod.network.message.reply.ModListMessageReply;
import com.netease.mc.mod.network.message.reply.ResourcePackMessageReply;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import com.netease.mc.mod.network.common.*;
import com.netease.mc.mod.network.message.reply.BabaMessageReply;
import com.netease.mc.mod.network.message.reply.HeartBeatMessageReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkSocket {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String MSG_QUIT = "quit";
	private static final String SEVER_IP = "127.0.0.1";
	private static final String CLOSECMD = "\000\001";
	private static final short LOGIN_CMD = 0x0000;
	private static final int BUFFER_SIZE = 20480;

	public static Socket mSocket;
	public static MsgQueue<String> mSendMsgQueue;
	public static MsgQueue<String> mRecvMsgQueue;
	public static char[] tmp = new char[BUFFER_SIZE];
	private static int server_port = 9876;

	public static boolean isLogin = false;
	public static Encrption encrptHandler = null;
	public static Timer timer;
	private static int HEAT_BEAT_INTERVAL = 60 * 1000;

	public NetworkSocket() {
	}

	public static void init() {
		mSendMsgQueue = new MsgQueue<String>();
		mRecvMsgQueue = new MsgQueue<String>();

		try {
			if (GameState.launcherport != 0) {
				server_port = GameState.launcherport;
			}

			LOGGER.info("My socket: " + SEVER_IP + ":" + server_port);
			mSocket = new Socket(SEVER_IP, server_port);
			connect();
			Library.LoadLibrary();
			encrptHandler = new Encrption();
			NetworkHandler.send(LOGIN_CMD, GameState.gameid, encrptHandler.GetSendToken());
			NetworkHandler.networkHandler.register(BabaMessageReply.SMID, new BabaMessageReply());
			NetworkHandler.networkHandler.register(ModListMessageReply.SMID, new ModListMessageReply());
			NetworkHandler.networkHandler.register(ResourcePackMessageReply.SMID, new ResourcePackMessageReply());

			// hear beat
			if(!GameState.launchversion.equals("")) {
				timer = new Timer();
				timer.schedule(new TimerTaskHeartBeat(), HEAT_BEAT_INTERVAL, HEAT_BEAT_INTERVAL);
				NetworkHandler.networkHandler.register(HeartBeatMessageReply.SMID, new HeartBeatMessageReply());
			}

			LOGGER.info("My socket: " + mSocket);
		} catch (Exception e) {
			onSocketConnectFail();
		}
	}

	public static void connect() {
		new Thread(new Sender(), "Sender").start();
		new Thread(new Receiver(), "Receiver").start();
	}

	public static void sendMesg(String msg) {
		pushSendMsg(msg);
	}


	public static class MsgQueue<T> {
		private static final int CAPACITY = 100;
		private List<T> mMsgs = new ArrayList<T>();

		public synchronized void push(T msg) {
			try {
				while (mMsgs.size() >= CAPACITY) {
					wait();
				}
				mMsgs.add(msg);
				notifyAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public synchronized T pop() {
			T msg = null;
			try {
				while (mMsgs.size() <= 0) {
					wait();
				}
				msg = mMsgs.get(0);
				mMsgs.remove(0);
				notifyAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return msg;
		}

		public int count() {
			return mMsgs.size();
		}
	}

	private static class Sender implements Runnable {
		@Override
		public void run() {
			LOGGER.info("Sender ... start --- " + Thread.currentThread().getName());
			try {
				DataOutputStream out = new DataOutputStream(mSocket.getOutputStream());
				String msg = "";
				while (!(msg = mSendMsgQueue.pop()).equals(MSG_QUIT)) {
					onMsgSendStart(msg);
					byte[] data = null;
					if (isLogin) {
						data = encrptHandler.Encrypt(msg.getBytes(Common.STRING_ENCODE));
					} else {
						data = msg.getBytes(Common.STRING_ENCODE);
						isLogin = true;
					}
					out.writeShort(LittleEndian.littleShort(data.length));
					out.write(data);
					out.flush();
					onMsgSendEnd(msg, true);
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				onSocketClosedSelf();
			}
			LOGGER.info("Sender ... end --- " + Thread.currentThread().getName());
		}
	}


	private static class Receiver implements Runnable {
		@Override
		public void run() {
			LOGGER.info("Receiver ... start --- " + Thread.currentThread().getName());
			try {
				// BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
				DataInputStream in = new DataInputStream(mSocket.getInputStream());
				int read;
				int length;
				String msg = "";
				while (mSocket != null) {
					length = in.readUnsignedByte();
					length += in.readUnsignedByte() * 256;
					byte[] tmp = new byte[length];
					for (int i = 0; i < length; i++) {
						tmp[i] = in.readByte();
					}
					byte[] plaintext = encrptHandler.Decrypt(tmp);
					msg = new String(plaintext, Common.STRING_ENCODE);
					onMsgReceived(msg);
				}
				in.close();
				pushSendMsg(MSG_QUIT);// quit sender
				onSocketClosedRemote();
			} catch (IOException e) {
				onSocketClosedSelf();
			}
			LOGGER.info("Receiver ... end --- " + Thread.currentThread().getName());
		}
	}

	public final static void pushSendMsg(String msg) {
		mSendMsgQueue.push(msg);
	}

	public static final int SMIDLEN = 2;

	private static int getSidMid(String msg) {
		if (msg.length() < SMIDLEN) {
			return -1;
		}
		return msg.charAt(0) * 256 + msg.charAt(1);
	}

	public final static void pushRecvMsg(String msg) {
		int smid = getSidMid(msg);
		if (!NetworkHandler.replyAsyncHashMap.containsKey(smid)) {
			mRecvMsgQueue.push(msg);
			return;
		}
		LOGGER.info("smid:" + smid);
		NetworkHandler.replyAsyncHashMap.get(smid).handMessage(msg);
	}

	public final static void quit() {
		pushSendMsg(MSG_QUIT);// quit sender
		try {
			if (mSocket != null) {
				mSocket.close();
				mSocket = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final Socket getSocket() {
		return mSocket;
	}

	private static void onLog(String msg) {
		byte[] bytes;
		try {
			bytes = msg.getBytes(Common.STRING_ENCODE);
		} catch (Exception e) {
			LOGGER.info("MSG is wrong! ");
			return;
		}
		String tem = "";
		for (int i = 0; i < bytes.length && i < 2; i++) {
			tem += bytes[i];
			tem += "  ";
		}
		LOGGER.info(tem);
	}

	protected static void onMsgSendStart(String msg) {
		LOGGER.info("[ME]: ");
		onLog(msg);
	}

	protected static void onMsgReceived(String msg) {
		LOGGER.info("[SERVER]: ");
		onLog(msg);
		pushRecvMsg(msg);
	}

	protected static void onSocketClosedRemote() {
		socketClosedRemote = true;
		LOGGER.info("Remote socket closed, input any words to quit.");
		closeMinecraft();
	}

	private static void onSocketConnectFail() {
		LOGGER.info("My socket: " + "socket connect fail");
		LOGGER.info("Close Game");
		closeMinecraft();
	}

	private static boolean socketClosedRemote = false;

	private static void onSocketClosedSelf() {
		LOGGER.info("Socket Close!");
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSocket = null;
		}
		LOGGER.info("Close Game");
		closeMinecraft();
	}

	protected static void onMsgSendEnd(String msg, boolean success) {
	}

	protected static void onMsgInput(String msg) {
	}

	private static void closeMinecraft() {
		mRecvMsgQueue.push(CLOSECMD);
	}
}

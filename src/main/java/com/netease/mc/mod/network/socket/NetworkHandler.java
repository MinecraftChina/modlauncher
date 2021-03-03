package com.netease.mc.mod.network.socket;

import java.util.HashMap;

import com.netease.mc.mod.network.message.reply.MessageReply;
import com.netease.mc.mod.network.message.request.MessageRequest;

public class NetworkHandler {
	public static NetworkHandler networkHandler = new NetworkHandler();
	public static final int SMIDLEN = 2;
	public static HashMap<Integer, MessageReply> replyHashMap = new HashMap<Integer, MessageReply>();
	public static HashMap<Integer, MessageReply> replyAsyncHashMap = new HashMap<Integer, MessageReply>();
	private static MessageRequest request = new MessageRequest();

	private int getSidMid(String msg) {
		if (msg.length() < SMIDLEN) {
			return -1;
		}
		return msg.charAt(0) * 256 + msg.charAt(1);
	}

	public NetworkHandler() {
	}

	public void register(int smid, MessageReply mrp) {
		replyHashMap.put(smid, mrp);
	}

	public void registerAsync(int smid, MessageReply mrp) {
		replyAsyncHashMap.put(smid, mrp);
	}

	public static void send(int smid, Object... args) {
		request.send(smid, args);
	}
}

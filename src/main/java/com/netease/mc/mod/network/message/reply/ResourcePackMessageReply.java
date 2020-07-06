package com.netease.mc.mod.network.message.reply;

import com.netease.mc.mod.network.game.GameUtils;

public class ResourcePackMessageReply extends Reply {
	public static final int SMID = 0x1101;
	public String data;

	public void handler(String msg) {
		GameUtils.getNativeMCRespacks(false);
	}
}
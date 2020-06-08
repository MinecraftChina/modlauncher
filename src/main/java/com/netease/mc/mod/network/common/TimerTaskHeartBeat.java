package com.netease.mc.mod.network.common;

import com.netease.mc.mod.network.game.GameUtils;
import com.netease.mc.mod.network.message.reply.HeartBeatMessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import com.netease.mc.mod.network.socket.NetworkSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.TimerTask;

public class TimerTaskHeartBeat extends TimerTask {
	private static final Logger LOGGER = LogManager.getLogger();
	public static long lastActiveTimeStamp = Common.getSystemTimeStamp();

	public void run() {
		long current = Common.getSystemTimeStamp();
		if(Math.abs(current - lastActiveTimeStamp) > 5 * 60 )
		{
			GameUtils.getMama();
			// cancel the timer
			if(NetworkSocket.timer != null) {
				NetworkSocket.timer.cancel();
				NetworkSocket.timer.purge();
			}
			return;
		}

		NetworkHandler.send(HeartBeatMessageReply.SMID, 0x0000);
		LOGGER.info("[HEARTBEAT]send heartbeat");
		GameUtils.getRedaolComps();
		GameUtils.getNativeMCRespacks();
	}
}

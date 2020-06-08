package com.netease.mc.mod.network.message.reply;

import com.netease.mc.mod.network.common.Common;
import com.netease.mc.mod.network.common.TimerTaskHeartBeat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HeartBeatMessageReply extends Reply {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final int SMID = 0x0002;

	public void handler(String msg) {
		LOGGER.info("[HEARTBEAT]recv heartbeat from wpflaunch");
		TimerTaskHeartBeat.lastActiveTimeStamp = Common.getSystemTimeStamp();
		LOGGER.info("[HEARTBEAT]lastActiveTimeStamp:" + TimerTaskHeartBeat.lastActiveTimeStamp);
	}
}

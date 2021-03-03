package com.netease.mc.mod.network.common;

import com.google.gson.Gson;
import com.netease.mc.mod.network.protocol.LauncherProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GameHost {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void Init(String[] args) {
		try {
			if (args == null) {
				LOGGER.info("arg is null");
				return;
			}

			String json = "";
			for (int idx = 0; idx < args.length; ++idx) {
				if (args[idx].contains("userProperties")) {
					json = args[idx + 1];
					break;
				}
			}

			LOGGER.info(json);
			Gson gson = new Gson();
			LauncherProperties array = gson.fromJson(json, LauncherProperties.class);
			if (array.launcherport != null && array.launcherport.length > 0) {
				GameState.launcherport = array.launcherport[0];
			}
			if (array.uid != null && array.uid.length > 0) {
				GameState.uid = array.uid[0];
			}
			if (array.launchversion != null && array.launchversion.length > 0) {
				GameState.launchversion = array.launchversion[0];
			}
			if (array.gameid != null && array.gameid.length > 0) {
				GameState.gameid = array.gameid[0];
			}
			if (array.filterkey != null && array.filterkey.length > 0) {
				GameState.filterkey = array.filterkey[0];
			}
			if (array.filterpath != null && array.filterpath.length > 0) {
				GameState.filterpath = array.filterpath[0];
			}
			if (array.blockGainFactor != null && array.blockGainFactor.length > 0) {
				GameState.blockGainFactor = array.blockGainFactor[0];
			}
			if (array.expGainFactor != null && array.expGainFactor.length > 0) {
				GameState.expGainFactor = array.expGainFactor[0];
			}
			if (array.countDownTime != null && array.countDownTime.length > 0) {
				GameState.countDownTime = array.countDownTime[0];
			}
			if (array.msg != null && array.msg.length > 0) {
				GameState.msg = array.msg[0];
			}
		} catch (Exception e) {
			LOGGER.error("GameHost", e);
		}
	}
}

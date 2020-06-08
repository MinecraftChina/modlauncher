package com.netease.mc.mod.network.message.reply;

import com.google.gson.Gson;
import com.netease.mc.mod.network.entity.ReflectionMapName;
import com.netease.mc.mod.network.game.GameUtils;
import com.netease.mc.mod.network.http.HttpUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class BabaMessageReply extends Reply {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final int SMID = 0x0003;

	public void handler(String mapJson, String url) {
		try {
			Gson gson = new Gson();
			HttpUtils.WEBSVR = url;
			GameUtils.mapName = gson.fromJson(mapJson, ReflectionMapName.class);
			LOGGER.info(GameUtils.mapName.toString());
		}
		catch (Exception e) {
			LOGGER.error("mapName Json Error:", e);
		}
	}
}

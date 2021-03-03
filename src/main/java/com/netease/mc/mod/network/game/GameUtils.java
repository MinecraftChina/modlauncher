package com.netease.mc.mod.network.game;

import com.google.gson.Gson;
import com.netease.mc.mod.network.common.GameState;
import com.netease.mc.mod.network.entity.*;
import com.netease.mc.mod.network.http.HttpUtils;
import com.netease.mc.mod.network.message.reply.ModListMessageReply;
import com.netease.mc.mod.network.message.reply.ResourcePackMessageReply;
import com.netease.mc.mod.network.socket.NetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static cpw.mods.modlauncher.Launcher.INSTANCE;

import java.lang.reflect.Method;


public class GameUtils {
	private static final Logger LOGGER = LogManager.getLogger();
	public final static String EMPTY_BABA = "[]";
	public static String lastRedaol = EMPTY_BABA;
	public static ReflectionMapName mapName;

	private static boolean noNeedGetComps() {
		if (null == GameUtils.mapName) {
			return false;
		}
		return !GameUtils.mapName.cn_redaol.equals("") &&
				!GameUtils.mapName.md_get_redaol.equals("") &&
				!GameUtils.mapName.md_redaol_getcomps.equals("") &&
				lastRedaol.equals(EMPTY_BABA);
	}

	public static Class<?> getNativeMCClass() {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(GameUtils.mapName.cn_nativemc, false, INSTANCE.classLoader);
		} catch (Exception e) {
			LOGGER.error("getNativeMCClass", e);
		}

		return clazz;
	}

	public static Object getNativeMCInstance() {
		Class<?> clazz = getNativeMCClass();
		if (clazz == null) {
			return null;
		}

		Object result = null;
		try {
			final Method instanceMethod = clazz.getMethod(GameUtils.mapName.md_get_nativemc);
			result = instanceMethod.invoke(null);
		} catch (Exception e) {
			LOGGER.error("getNativeMCInstance", e);
		}

		return result;
	}

	public static void getNativeMCRespacks() {
		getNativeMCRespacks(true);
	}

	public static void getNativeMCRespacks(boolean check) {
		Class<?> clazz = getNativeMCClass();
		if (clazz == null) {
			return;
		}

		try {
			final Method getrespacksmethod = clazz.getMethod(GameUtils.mapName.md_nativemc_getrpr);
			Object nativeCM = getNativeMCInstance();
			Object gc = getrespacksmethod.invoke(nativeCM);
			Class c = gc.getClass();
			Method getRespackMethod = c.getMethod(GameUtils.mapName.md_rpr_getkcaps);
			List<Object> respackList = (List<Object>) getRespackMethod.invoke(gc);
			if(check && respackList.toString() == EMPTY_BABA) {
				return;
			}

			List<ResourcePackEntity> resourcePackEntities = new ArrayList<ResourcePackEntity>();
			if (respackList != null && respackList.size() > 0) {
				for (int idx = 0; idx < respackList.size(); idx++) {
					Class<?> resourcePackClass = respackList.get(idx).getClass();
					if (resourcePackClass == null) {
						LOGGER.info("resourcePackClass is null");
						break;
					}

					ResourcePackEntity entity = new ResourcePackEntity();
					entity.reResourcePack = respackList.get(idx).toString();
					final Method getTextureDescMethod = resourcePackClass
						.getMethod("func_110519_e");
					final Method getPackNameMethod = resourcePackClass.getMethod("func_110515_d");
					entity.name = getPackNameMethod.invoke(respackList.get(idx)).toString();
					entity.desc = getTextureDescMethod.invoke(respackList.get(idx)).toString();
					resourcePackEntities.add(entity);
				}
			}

			Gson gson = new Gson();
			ResourcePackMessageReply msg = new ResourcePackMessageReply();
			msg.data = gson.toJson(resourcePackEntities);
			NetworkHandler.send(ResourcePackMessageReply.SMID, msg.data);
		} catch (Exception e) {
			LOGGER.error("getNativeMCRespacks", e);
		}
	}

	public static void getRedaolComps() {
		getRedaolComps(true);
	}

	public static void getRedaolComps(boolean check) {
		if (check && !noNeedGetComps()) {
			LOGGER.info("[getRedaolComps]no need getRedaolComps anymore!");
			return;
		}

		try {
			final Class<?> clazz = Class.forName(GameUtils.mapName.cn_redaol, false, INSTANCE.classLoader);
			final Method method1 = clazz.getMethod(GameUtils.mapName.md_get_redaol);
			final Method method2 = clazz.getMethod(GameUtils.mapName.md_redaol_getcomps);
			lastRedaol = method2.invoke(method1.invoke(null)).toString();
			if (lastRedaol.equals(EMPTY_BABA)) {
				return;
			}

			Object mods = method2.invoke(method1.invoke(null));
			List<Object> modlist = (List<Object>)mods;
			List<ModEntity> modEntities = new ArrayList<ModEntity>();
			for (int idx = 0; idx < modlist.size(); idx++) {
				Class<?> modClass = modlist.get(idx).getClass();
				if(modClass == null) {
					LOGGER.info("modClass is null");
					break;
				}

				ModEntity entity = new ModEntity();
				final Method getSourceMethod = modClass.getMethod("getSource");
				final Method getModidMethod = modClass.getMethod("getModId");
				final Method getModnameMethod = modClass.getMethod("getName");
				final Method getModverMethod = modClass.getMethod("getVersion");
				final Method getModdataMethod = modClass.getMethod("getMetadata");
				final Method getModMethod = modClass.getMethod("getCustomModProperties");

				File modSource = (File)getSourceMethod.invoke(modlist.get(idx));
				if(modSource != null) {
					entity.filename = modSource.getCanonicalPath();
				}
				entity.id = getModidMethod.invoke(modlist.get(idx)).toString();
				entity.name = getModnameMethod.invoke(modlist.get(idx)).toString();
				entity.ver = getModverMethod.invoke(modlist.get(idx)).toString();
				entity.md5 = getModMethod.invoke(modlist.get(idx)).toString();

				Object metaDataObj = getModdataMethod.invoke(modlist.get(idx));
				if(metaDataObj != null) {
					Class<?> metaDataClass = metaDataObj.getClass();
					if(metaDataClass != null) {
						Field descriptionField = metaDataClass.getDeclaredField("description");
						final Method authorListMethod = metaDataClass.getMethod("getAuthorList");
						entity.metadata = String.format("desc:%s|authors:%s",
							descriptionField.get(metaDataObj).toString(),
							authorListMethod.invoke(metaDataObj).toString());
					}
				}
				modEntities.add(entity);
			}

			Gson gson = new Gson();
			ModListMessageReply modListMessageReply = new ModListMessageReply();
			modListMessageReply.data = gson.toJson(modEntities);
			NetworkHandler.send(ModListMessageReply.SMID, modListMessageReply.data);
		} catch (Exception e) {
			LOGGER.error("getRedaolComps", e);
		}
	}

	public static void getMama() {
		ClientLog log = new ClientLog(GameState.uid, GameState.launchversion);
		Gson gson = new Gson();
		UrgentLog urgentLog = new UrgentLog();
		urgentLog.log = gson.toJson(log);
		String requestJson = gson.toJson(urgentLog);
		String result = HttpUtils.sendPost(HttpUtils.WEBSVR + "/urgent-log", requestJson);
		LOGGER.info("[mama]:" + result);
	}
}

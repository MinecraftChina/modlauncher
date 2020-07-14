package com.netease.mc.mod.coremod;

import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenzhikun on 2020/6/16
 */
public class CoreModManager {

	static {
		classDataList = new HashMap<>();
		AddCoreModMethodData(
				"net.minecraft.client.renderer.tileentity.SignTileEntityRenderer",
				"render",
				"(Lnet/minecraft/tileentity/SignTileEntity;F" +
				"Lcom/mojang/blaze3d/matrix/MatrixStack;" +
				"Lnet/minecraft/client/renderer/IRenderTypeBuffer;II)V",
				"com.netease.mc.mod.filter.GuiScreenBookTransformer",
				"com.netease.mc.mod.oldInterface.ISignTileEntityRendererOld");
	}

	private static Map<String, CoreModClassData> classDataList;

	public static ArrayList<CoreModClassData> getCoreModClassDataList() {
		return new ArrayList(classDataList.values());
	}

	public static CoreModClassData getClassData(String name) {
		return classDataList.getOrDefault(name.replace('/', '.'), null);
	}

	private static void AddCoreModMethodData(CoreModMethodData data) {

		CoreModClassData newClassData = classDataList.getOrDefault(data.getClassName(), null);
		if (newClassData == null)
		{
			newClassData = new CoreModClassData(data.getClassName());
		}
		newClassData.AddCoreModMethodData(data);
		classDataList.put(data.getClassName(),newClassData);
		LogManager.getLogger().info("getCoreModClassDataList size:" + getCoreModClassDataList().size());
	}

	

	public static void AddCoreModMethodData(String _className, String _methodName, String _desc,
	                                        String _tranformerClassName, String _oldInterface)
	{
		CoreModMethodData data = new CoreModMethodData(
				_className,
				_methodName,
				_desc,
				_tranformerClassName,
				_oldInterface);
		AddCoreModMethodData(data);
	}



}

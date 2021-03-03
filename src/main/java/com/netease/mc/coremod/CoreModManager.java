package com.netease.mc.coremod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenzhikun on 2020/6/16
 */
public class CoreModManager {

	private static final Logger logger = LogManager.getLogger();
	private static final Map<String, CoreModClassData> classDataList = new HashMap<>();

	public static ArrayList<CoreModClassData> getCoreModClassDataList() {
		return new ArrayList<>(classDataList.values());
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


	public static void AddCoreModMethodData(String _className,String _srcMethodName ,String _methodName, String _desc,
	                                        String _transformerClassName, String _oldInterface, Boolean _isStatic )
	{
		CoreModMethodData data = new CoreModMethodData(
				_className,
				_srcMethodName,
				_methodName,
				_desc,
				_transformerClassName,
				_oldInterface,
				_isStatic);
		AddCoreModMethodData(data);
	}

	public static void AddCoreModMethodData(String _className,String _srcMethodName ,String _methodName, String _desc,
											String _transformerClassName, String _oldInterface)
	{
		CoreModMethodData data = new CoreModMethodData(
				_className,
				_srcMethodName,
				_methodName,
				_desc,
				_transformerClassName,
				_oldInterface,
				false);
		AddCoreModMethodData(data);
	}


	private static MethodNode getMethod(ClassNode classNode, String methodName, String methodDescriptor)
	{
		List<MethodNode> methods = classNode.methods;
		LogManager.getLogger().info(methodName + ":"+ methods.size());
		String targetMethodName;
		try
		{
			Class<?> asmApi = Class.forName("net.minecraftforge.coremod.api.ASMAPI");
			Method mapMethod = asmApi.getDeclaredMethod("mapMethod", String.class);
			targetMethodName = (String) mapMethod.invoke(null, methodName);
		}catch (Throwable e)
		{
			logger.error("getMethod Failed :" + classNode.name + " " + methodName + " " + methodDescriptor);
			return null;
		}



		for (MethodNode method : methods) {
			if (method.name.equals(targetMethodName) && method.desc.equals(methodDescriptor)) {
				logger.info("Matched method " + method.name + " " + method.desc);
				return  method;
			}
		}

		logger.error("CoreMod Fatal Error: Method not found: " + methodName);
		for (MethodNode method : methods) {
			logger.error("Iterate method " + method.name + " " + method.desc);
		}

		return null;
	}

	private static  final HashMap<Integer, Integer> Opcode2Size = new HashMap<Integer, Integer>(){{
		put(Opcodes.ALOAD, 1);
		put(Opcodes.ILOAD, 1);
		put(Opcodes.FLOAD, 1);
		put(Opcodes.DLOAD, 2);
		put(Opcodes.LLOAD, 2);
	}};

	public static void transformCommon(ClassNode classNode,  CoreModMethodData coreModMethodData)
	{
		logger.info("start  utilCoreMod: " + coreModMethodData.getMethodName());
		MethodNode method = getMethod(classNode, coreModMethodData.getSrcMethodName(), coreModMethodData.getDesc());
		if (method == null)
		{
			logger.error("utilCoreMod : don not find method  : " + coreModMethodData.getMethodName());
			return;
		}


		MethodNode newMethod = new MethodNode(Opcodes.ASM6);
		newMethod.access = method.access;
		newMethod.name = method.name;
		newMethod.desc = method.desc;
		newMethod.signature = null;
		newMethod.exceptions = method.exceptions;

		method.name = coreModMethodData.getMethodName() + "Old";
		method.access &= ~Opcodes.ACC_PRIVATE ;
		method.access &= ~Opcodes.ACC_PROTECTED ;
		method.access |= Opcodes.ACC_PUBLIC ;


		List<Integer> paramOpcodeList = coreModMethodData.getParamOpcodeList();
		Boolean isStatic = coreModMethodData.getStatic();
		String oldInterface = coreModMethodData.getOldInterface();
		int offset = 0;
		for (Integer Opcode : paramOpcodeList)
		{
			int size = Opcode2Size.getOrDefault(Opcode, 0);
			if (size > 0)
			{
				newMethod.visitVarInsn(Opcode, offset);
				offset += size;
			}
			else {
				logger.error("transfer utilCoreMod failed: unknown opcode" + Opcode);
			}
		}
		if (isStatic)
		{
			newMethod.visitMethodInsn(Opcodes.INVOKESTATIC,
					coreModMethodData.getTransformerClassName().replace(".", "/"),
					coreModMethodData.getMethodName() + "Wrapper",
					coreModMethodData.getDesc(),
					false);
		}
		else
		{

			if (oldInterface != null)
			{
				newMethod.visitMethodInsn(Opcodes.INVOKESTATIC,
						coreModMethodData.getTransformerClassName().replace(".", "/"),
						coreModMethodData.getMethodName() + "Wrapper",
						"(L" + coreModMethodData.getOldInterface().replace(".", "/")  + ";"+ coreModMethodData.getDesc().substring(1),
						false);
			}
			else
			{
				newMethod.visitMethodInsn(Opcodes.INVOKESTATIC,
						coreModMethodData.getTransformerClassName().replace(".", "/"),
						coreModMethodData.getMethodName() + "Wrapper",
						"(L" + coreModMethodData.getClassName().replace(".", "/") + ";"+ coreModMethodData.getDesc().substring(1),
						false);
			}

		}

		newMethod.visitInsn(coreModMethodData.getReturnOpcode());
		classNode.methods.add(newMethod);
		if (oldInterface != null && !classNode.interfaces.contains(oldInterface.replace(".", "/")))
		{
			classNode.interfaces.add(oldInterface.replace(".", "/"));
		}
		logger.info("set new method success utilCoreMod: " + newMethod.name);
	}
}

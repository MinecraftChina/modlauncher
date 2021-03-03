package com.netease.mc.coremod;

import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.Opcodes;
import javax.annotation.Nullable;
import java.util.ArrayList;



/**
 * Created by chenzhikun on 2020/6/16
 */
public class CoreModMethodData {
	private final String className;
	private final String srcMethodName;
	private final String methodName;
	private final String desc;
	private final String oldInterface;
	private final String transformerClassName;
	private final boolean isStatic;

	private ArrayList<Integer> paramOpcodeList;
	private int returnOpcode;


	public CoreModMethodData(String _className, String _srcMethodName, String _methodName, String _desc,  String _transformerClassName)
	{
		this(_className,_srcMethodName, _methodName, _desc, _transformerClassName, null, false);
	}

	public CoreModMethodData(String _className, String _srcMethodName, String _methodName, String _desc,  String _transformerClassName, String _oldInterface, Boolean _isStatic)
	{
		className = _className;
		srcMethodName = _srcMethodName;
		methodName = _methodName;
		desc = _desc;
		oldInterface = _oldInterface;
		transformerClassName = _transformerClassName;
		isStatic = _isStatic;

		initParamOpcodes();
		initReturnOpcode();
	}

	public String getClassName() {
		return className;
	}

	public String getDescClassName() {
		return className.replace('.','/');
	}

	public String getSrcMethodName(){
		if (srcMethodName.isEmpty())
		{
			return  methodName;
		}
		return srcMethodName;
	}

	public String getMethodName(){
		return methodName;
	}

	public String getDesc(){
		return desc;
	}

	@Nullable
	public String getOldInterface(){
		return oldInterface;
	}

	public Boolean getStatic(){
		return isStatic;
	}

	public String getTransformerClassName(){
		return transformerClassName;
	}



	public int getReturnOpcode()
	{
		return returnOpcode;
	}



	public ArrayList<Integer> getParamOpcodeList() {
		return paramOpcodeList;
	}

	private void initParamOpcodes()
	{
		String params = desc.substring(1, desc.indexOf(')'));
		paramOpcodeList = new ArrayList<>();
		if (!isStatic)
		{
			paramOpcodeList.add(Opcodes.ALOAD);
		}


		for (int i = 0; i < params.length(); i++)
		{
			switch (params.charAt(i))
			{
				case 'L':
					paramOpcodeList.add(Opcodes.ALOAD);
					i = params.indexOf(';', i);
					break;
				case '[':
					paramOpcodeList.add(Opcodes.ALOAD);
					while (params.charAt(i) == '[')
					{
						i++;
					}
					if (params.charAt(i) == 'L') {
						i = params.indexOf(';', i);
					}
					break;
				case 'D':
					paramOpcodeList.add(Opcodes.DLOAD);
					break;
				case 'F':
					paramOpcodeList.add(Opcodes.FLOAD);
					break;
				case 'J':
					paramOpcodeList.add(Opcodes.LLOAD);
					break;
				case 'Z':
				case 'B':
				case 'C':
				case 'S':
				case 'I':
					paramOpcodeList.add(Opcodes.ILOAD);
					break;
				default:
					LogManager.getLogger().error("unknown ASM : " +  params.charAt(i));
			}
		}
	}

	private void initReturnOpcode()
	{
		char ret = desc.charAt(desc.indexOf(')') + 1);
		switch (ret)
		{
			case 'V':
			case 'Z':
			case 'B':
			case 'C':
			case 'S':
			case 'I':
				returnOpcode = Opcodes.RETURN;
				break;
			case 'L':
			case '[':
				returnOpcode = Opcodes.ARETURN;
				break;
			case 'D':
				returnOpcode = Opcodes.DRETURN;
				break;
			case 'F':
				returnOpcode = Opcodes.FRETURN;
				break;
			case 'J':
				returnOpcode = Opcodes.LRETURN;
				break;
			default:
				LogManager.getLogger().error("unknown return ASM : " +  ret);
		}
	}
}

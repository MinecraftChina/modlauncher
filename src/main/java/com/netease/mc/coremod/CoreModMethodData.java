package com.netease.mc.mod.coremod;

import com.netease.mc.mod.network.common.Common;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.Opcodes;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;


/**
 * Created by chenzhikun on 2020/6/16
 */
public class CoreModMethodData {
	private String className;
	private String srcMethodName;
	private String methodName;
	private String desc;
	private String oldInterface = null;
	private String tranformerClassName;

	private ArrayList<Integer> paramOpcodelist;
	private int offset = 0;
	private int returnOpcode;


	public CoreModMethodData(String _className, String _srcMethodName, String _methodName, String _desc,  String _tranformerClassName)
	{
		this(_className,_srcMethodName, _methodName, _desc, _tranformerClassName, null);
	}

	public CoreModMethodData(String _className, String _srcMethodName, String _methodName, String _desc,  String _tranformerClassName, String _oldInterface)
	{
		className = _className;
		srcMethodName = _srcMethodName;
		methodName = _methodName;
		desc = _desc;
		oldInterface = _oldInterface;
		tranformerClassName = _tranformerClassName;

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

	public String getTranformerClassName(){
		return tranformerClassName;
	}



	public int getReturnOpcode()
	{
		return returnOpcode;
	}



	public ArrayList<Integer> getParamOpcodelist() {
		return paramOpcodelist;
	}

	private void initParamOpcodes()
	{
		String params = desc.substring(1, desc.indexOf(')'));
		paramOpcodelist = new ArrayList();
		paramOpcodelist.add(Opcodes.ALOAD);

		for (int i = 0; i < params.length(); i++)
		{
			switch (params.charAt(i))
			{
				case 'L':
					paramOpcodelist.add(Opcodes.ALOAD);
					i = params.indexOf(';', i);
					break;
				case '[':
					paramOpcodelist.add(Opcodes.ALOAD);
					while (params.charAt(i) == '[')
					{
						i++;
					}
					if (params.charAt(i) == 'L') {
						i = params.indexOf(';', i);
					}
					break;
				case 'D':
					paramOpcodelist.add(Opcodes.DLOAD);
					break;
				case 'F':
					paramOpcodelist.add(Opcodes.FLOAD);
					break;
				case 'J':
					paramOpcodelist.add(Opcodes.LLOAD);
					break;
				case 'Z':
				case 'B':
				case 'C':
				case 'S':
				case 'I':
					paramOpcodelist.add(Opcodes.ILOAD);
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
			case 'Z':
			case 'B':
			case 'C':
			case 'S':
			case 'I':
				returnOpcode = Opcodes.RETURN;
				break;
			default:
				LogManager.getLogger().error("unknown return ASM : " +  ret);
		}
	}
}

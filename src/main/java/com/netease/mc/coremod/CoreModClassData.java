package com.netease.mc.coremod;

import java.util.ArrayList;

/**
 * Created by chenzhikun on 2020/6/16
 */
public class CoreModClassData {
	private final  String className;
	private final ArrayList<CoreModMethodData> methodList = new ArrayList<>();

	public  CoreModClassData(String name)
	{
		className = name;
	}

	public void AddCoreModMethodData(CoreModMethodData data)
	{
		for (CoreModMethodData methodData :  methodList)
		{
			if (methodData.getMethodName().equals(data.getMethodName())
					&& methodData.getDesc().equals(data.getDesc()))
			{
				return;
			}
		}
		methodList.add(data);
	}

	public String getClassName() {
		return className;
	}

	public ArrayList<CoreModMethodData> getCoreModMethodDataList(){
		return  methodList;
	}
}

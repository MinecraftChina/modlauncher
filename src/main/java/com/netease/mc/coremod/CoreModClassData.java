package com.netease.mc.mod.helper;

import java.util.ArrayList;

/**
 * Created by chenzhikun on 2020/6/16
 */
public class CoreModClassData {
	private String className;
	private ArrayList<CoreModMethodData> methodList = new ArrayList<>();

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

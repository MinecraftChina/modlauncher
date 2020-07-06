package com.netease.mc.mod.network.entity;

public class ResPacksLog extends BaseLog {
	private String msg;

	public ResPacksLog(int id, String message) {
		uid = id;
		type = "kcap";
		msg = message;
	}
}

package com.netease.mc.mod.network.entity;

public class RedaolGamsLog extends BaseLog {
	private String msg;

	public RedaolGamsLog(int id, String message) {
		uid = id;
		type = "gams";
		msg = message;
	}
}

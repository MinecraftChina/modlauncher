package com.netease.mc.mod.network.entity;

public class ClientLog extends BaseLog {
	public String launchversion;

	public ClientLog(int id, String version) {
		type = "wpflauncher-dead";
		launchversion = version;
		uid = id;
	}
}

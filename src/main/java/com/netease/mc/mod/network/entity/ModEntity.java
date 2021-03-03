package com.netease.mc.mod.network.entity;

public class ModEntity {
	public String filename;
	public String id;
	public String name;
	public String md5;
	public String ver;
	public String metadata;

	public ModEntity() {

	}

	public ModEntity(String id, String name, String path, String md5, String ver, String metadata) {
		this.id = id;
		this.name = name;
		this.filename = path;
		this.md5 = md5;
		this.ver = ver;
		this.metadata = metadata;
	}
}

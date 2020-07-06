package com.netease.mc.mod.network.entity;

public class ReflectionMapName {
	public String cn_redaol;
	public String md_get_redaol;
	public String md_redaol_getcomps;
	public String cn_nativemc;
	public String md_get_nativemc;
	public String md_nativemc_getrpr;
	public String md_rpr_getkcaps;

	public String toString() {
		return String.format("{cn_redaol:%s,md_get_redaol:%s,md_redaol_getcomps:%s,cn_nativemc:%s,md_get_nativemc:%s,md_nativemc_getrpr:%s,md_rpr_getkcaps:%s}", cn_redaol, md_get_redaol, md_redaol_getcomps, cn_nativemc, md_get_nativemc, md_nativemc_getrpr, md_rpr_getkcaps);
	}
}

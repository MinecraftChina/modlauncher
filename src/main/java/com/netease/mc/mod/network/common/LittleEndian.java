package com.netease.mc.mod.network.common;

public class LittleEndian {
	public static int littleShort(int number) {
		return ((number & 0x00ff) << 8 | (number >> 8) & 0x00ff);
	}

	public static int littleInt(int number) {
		return ((number & 0x000000ff) << 24 | (number & 0x0000ff00) << 8 | (number >> 8) & 0x0000ff00 | (number >> 24) & 0x000000ff);
	}

	public static long littleLong(long number) {
		return ((number & 0xffL) << 56 | (number & 0xff00L) << 40 | (number & 0xff0000L) << 24
				| (number & 0xff000000L) << 8 | (number >> 8) & 0x00000000ff000000L | (number >> 24)
				& 0x0000000000ff0000L | (number >> 40) & 0x000000000000ff00L | (number >> 56) & 0x00000000000000ffL);
	}
}

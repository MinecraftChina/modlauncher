package com.netease.mc.mod.network.common;

import java.util.ArrayList;

public class GameState {
	public static short gameid = 0;
	public static int uid = 0;
	public static int launcherport = 9876;
	public static String launchversion = "";
	public static String filterkey = "";
	public static String filterpath = "";
	public static int countDownTime = 0;
	public static double blockGainFactor = 1;
	public static double expGainFactor = 1;
	public static String msg = "";
	public static ArrayList<Integer> acceptList = new ArrayList<Integer>();

	public enum GameS {
		INIT, LOAD, SINGLE, SERVER, CLIENT,
	}

	public static GameS gameState;
}

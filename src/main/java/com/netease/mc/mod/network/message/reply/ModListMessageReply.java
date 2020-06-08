package com.netease.mc.mod.network.message.reply;

import com.netease.mc.mod.network.game.GameUtils;

public class ModListMessageReply extends Reply {
    public static final int SMID = 0x1100;
    public String data;

    public void handler(String msg) {
        // get mod list
        GameUtils.getRedaolComps(false);
    }
}

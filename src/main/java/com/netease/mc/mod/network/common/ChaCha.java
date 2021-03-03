package com.netease.mc.mod.network.common;

public class ChaCha {
    public long chaPointer;

    public ChaCha(byte[] key) {
        this.chaPointer = Library.NewChaCha(8, key);
    }

    protected void finalize() {
        Library.DeleteChaCha(this.chaPointer);
    }

    public void Process(byte[] data) {
        Library.ChaChaProcess(this.chaPointer, data, data.length);
    }
}

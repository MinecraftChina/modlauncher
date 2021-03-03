package com.netease.mc.mod.network.common;

import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Library {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    private static Boolean SafeLoadLibrary(String path) {
        try {
            System.load(path);
            return true;
        } catch (Exception e) {
            LOGGER.error("SafeLoadLibrary", e);
            return false;
        }
    }

    public static void main(String... args) {
        try
        {
            LoadLibrary();
            GameHost.Init(args);
        }
        catch (Exception e)
        {
            LOGGER.error(e);
        }
    }

    public static Boolean LoadLibrary() throws Exception {
        try {
            File runtime;
            String runtime_path = System.getProperty("runtime_path");
            runtime = new File(runtime_path);
            LOGGER.info(runtime.getAbsolutePath());
            if(!runtime.exists())
            {
                LOGGER.error("LoadLibrary runtime not found");
                return false;
            }
            File[] files = runtime.listFiles();
            if (files == null) {
                LOGGER.error("files is null");
                return false;
            }
            List<File> failedFiles = new ArrayList<File>();
            for (int i = 0; i < 10; ++i) {
                if (i > 0 && files.length <= 0) {
                    break;
                }
                for (File file : files) {
                    if (file.isFile() && file.getName().contains("dll")) {
                        if (!SafeLoadLibrary(file.getPath())) {
                            failedFiles.add(file);
                        }
                    }
                }

                files = failedFiles.toArray(new File[failedFiles.size()]);
                failedFiles.clear();
                LOGGER.info(files.length);
            }
        } catch (Exception e) {
            LOGGER.error("LoadLibrary", e);
            return false;
        }
        return true;
    }

    public static native void GetToken(byte[] token, int len);

    public static native long NewChaCha(int lv, byte[] key);

    public static native void DeleteChaCha(long ctx);

    static public native void ChaChaProcess(long ctx, byte[] data, int len);

    static public native int Skip32(boolean en, byte[] key, int word);

}

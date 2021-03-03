package com.netease.mc.mod.network.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.UnsupportedEncodingException;

public class Encrption {
	private static final Logger LOGGER = LogManager.getLogger();
	public byte[] token;
	public long recv_pointer = 0;
	public long send_pointer = 0;

	public Encrption() {
		try {
			token = new byte[32];
			Library.GetToken(token, 32);
			byte[] sendkey = new byte[32];
			System.arraycopy(token, 16, sendkey, 0, 16);
			System.arraycopy(token, 0, sendkey, 16, 16);
			recv_pointer = Library.NewChaCha(8, token);
			send_pointer = Library.NewChaCha(8, sendkey);
		} catch (Exception e) {
			LOGGER.error("Network", e);
		}
	}

	public String GetSendToken() throws UnsupportedEncodingException {
		return new String(token, Common.STRING_ENCODE);
	}

	public byte[] Encrypt(byte[] plaintext) {
		Library.ChaChaProcess(send_pointer, plaintext, plaintext.length);
		return plaintext;
	}

	public byte[] Decrypt(byte[] encryptText) {
		Library.ChaChaProcess(recv_pointer, encryptText, encryptText.length);
		return encryptText;
	}
}

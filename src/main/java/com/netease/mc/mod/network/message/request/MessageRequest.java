package com.netease.mc.mod.network.message.request;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.netease.mc.mod.network.common.Common;
import com.netease.mc.mod.network.common.LittleEndian;
import com.netease.mc.mod.network.socket.NetworkSocket;

public class MessageRequest {
	public DataOutputStream stream = null;
	Map<String, Method> writeDataMap = null;

	public MessageRequest() {
		writeDataMap = new HashMap<String, Method>();
		try {
			Class<?>[] argClasses = new Class<?>[1];
			argClasses[0] = byte.class;
			writeDataMap.put("byte", this.getClass().getMethod("writeByte", argClasses));
			writeDataMap.put(Byte.class.getName(), this.getClass().getMethod("writeByte", argClasses));
			argClasses[0] = boolean.class;
			writeDataMap.put(Boolean.class.getName(),
					this.getClass().getMethod("writeBoolean", argClasses));
			argClasses[0] = short.class;
			writeDataMap.put("short", this.getClass().getMethod("writeShort", argClasses));
			writeDataMap.put(Short.class.getName(), this.getClass().getMethod("writeShort", argClasses));
			argClasses[0] = int.class;
			writeDataMap.put("int", this.getClass().getMethod("writeInt", argClasses));
			writeDataMap.put(Integer.class.getName(), this.getClass().getMethod("writeInt", argClasses));
			argClasses[0] = long.class;
			writeDataMap.put("long", this.getClass().getMethod("writeLong", argClasses));
			writeDataMap.put(Long.class.getName(), this.getClass().getMethod("writeLong", argClasses));
			argClasses[0] = float.class;
			writeDataMap.put(Float.class.getName(), this.getClass().getMethod("writeFloat", argClasses));
			argClasses[0] = double.class;
			writeDataMap
					.put(Double.class.getName(), this.getClass().getMethod("writeDouble", argClasses));
			argClasses[0] = String.class;
			writeDataMap
					.put(String.class.getName(), this.getClass().getMethod("writeString", argClasses));

		} catch (Exception e) {
			System.out.print(e.toString());
		}
	}

	public void writeByte(byte b) throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		stream.writeByte(b);
	}

	public void writeBoolean(boolean bool) throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		stream.writeBoolean(bool);
	}

	public void writeShort(short s) throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		stream.writeShort(LittleEndian.littleShort(s));
	}

	public void writeInt(int i) throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		stream.writeInt(LittleEndian.littleInt(i));
	}

	public void writeLong(long i) throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		stream.writeLong(LittleEndian.littleLong(i));
	}

	public void writeFloat(float f) throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		stream.writeFloat(f);
	}

	public void writeDouble(double d) throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		stream.writeDouble(d);
	}

	public void writeString(String str) throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		byte[] tmp = str.getBytes("UTF-8");
		int length = tmp.length;
		stream.writeShort(LittleEndian.littleShort(length));
		stream.write(tmp);
	}

	public void send(int smid, Object... args) {
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		stream = new DataOutputStream(bstream);

		try {
			stream.writeShort(smid);

			Object[] objects = new Object[1];
			for (int i = 0; i < args.length; i++) {
				objects[0] = args[i];
				writeDataMap.get(args[i].getClass().getName()).invoke(this, objects);
			}
			NetworkSocket.sendMesg(new String(bstream.toByteArray(), Common.STRING_ENCODE));

			stream = null;
		} catch (Exception e) {
			stream = null;
		}
	}
}

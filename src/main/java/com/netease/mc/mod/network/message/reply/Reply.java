package com.netease.mc.mod.network.message.reply;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.netease.mc.mod.network.common.Common;
import com.netease.mc.mod.network.common.LittleEndian;


public class Reply implements MessageReply {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final int SMID = 0x0000;
	public static final String HANDLER = "handler";
	private Method handlerMethod = null;
	DataInputStream stream = null;
	Map<String, Method> getDataMap = null;

	public Reply() {
		handlerMethod = gethandler();

		getDataMap = new HashMap<String, Method>();
		try {
			getDataMap.put("byte", this.getClass().getMethod("getByte"));
			getDataMap.put(Byte.class.getName(), this.getClass().getMethod("getByte"));
			getDataMap.put("boolean", this.getClass().getMethod("getBoolean"));
			getDataMap.put(Boolean.class.getName(), this.getClass().getMethod("getBoolean"));
			getDataMap.put("short", this.getClass().getMethod("getShort"));
			getDataMap.put(Short.class.getName(), this.getClass().getMethod("getShort"));
			getDataMap.put("int", this.getClass().getMethod("getInt"));
			getDataMap.put(Integer.class.getName(), this.getClass().getMethod("getInt"));
			getDataMap.put("long", this.getClass().getMethod("getLong"));
			getDataMap.put(Long.class.getName(), this.getClass().getMethod("getLong"));
			getDataMap.put("float", this.getClass().getMethod("getFloat"));
			getDataMap.put(Float.class.getName(), this.getClass().getMethod("getFloat"));
			getDataMap.put("double", this.getClass().getMethod("getDouble"));
			getDataMap.put(Double.class.getName(), this.getClass().getMethod("getDouble"));
			getDataMap.put("String", this.getClass().getMethod("getString"));
			getDataMap.put(String.class.getName(), this.getClass().getMethod("getString"));
		} catch (Exception e) {
			LOGGER.error("Reply", e);
		}
	}

	public byte getByte() throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		return stream.readByte();
	}

	public boolean getBoolean() throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		return stream.readBoolean();
	}

	public short getShort() throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		return (short) LittleEndian.littleShort(stream.readShort());
	}

	public int getInt() throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		return LittleEndian.littleInt(stream.readInt());
	}

	public long getLong() throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		return LittleEndian.littleLong(stream.readLong());
	}

	public float getFloat() throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		return stream.readFloat();
	}

	public double getDouble() throws Exception {
		if (stream == null) {
			throw new IOException();
		}

		byte[] bytes = new byte[8];
		stream.read(bytes, 0, 8);

		double d = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
		return d;
	}

	public String getString() throws Exception {
		if (stream == null) {
			throw new IOException();
		}
		int length = LittleEndian.littleShort(stream.readUnsignedShort());
		byte[] tmp = new byte[length];
		stream.readFully(tmp);
		return new String(tmp, "UTF-8");
	}


	public Method gethandler() {
		Method[] methods = this.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().compareTo(HANDLER) == 0) {
				return method;
			}
		}
		return null;
	}

	public void handMessage(String msg) {
		if (handlerMethod == null) {
			return;
		}
		try {
			byte[] bytes = msg.getBytes(Common.STRING_ENCODE);
			ByteArrayInputStream bstream = new ByteArrayInputStream(bytes);
			stream = new DataInputStream(bstream);

			int smid = stream.readUnsignedShort();
			if ((Integer) (this.getClass().getField("SMID").get(null)) != smid) {
				return;
			}

			Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
			Object[] args = new Object[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				String parameterName = parameterTypes[i].getName();
				args[i] = getDataMap.get(parameterName).invoke(this);
				LOGGER.info("[Network] " + parameterName + " " + i + " " + args[i].toString());
			}
			handlerMethod.invoke(this, args);
		} catch (Exception e) {
			LOGGER.error("handMessage", e);
		}
	}
}

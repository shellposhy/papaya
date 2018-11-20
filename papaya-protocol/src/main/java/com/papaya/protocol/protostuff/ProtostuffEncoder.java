package com.papaya.protocol.protostuff;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.papaya.protocol.protostuff.Serializations.serialize;

/**
 * Encoders based on {@code Protostuff}
 * 
 * @author shellpo shih
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class ProtostuffEncoder extends MessageToByteEncoder {

	private Class<?> clazz;

	public ProtostuffEncoder(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
		if (clazz.isInstance(in)) {
			byte[] data = serialize(in);
			// Provider Decoder {@code ByteBuf.readInt()} use
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}
}

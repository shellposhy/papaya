package com.papaya.protocol.protostuff;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * Decoders based on {@code Protostuff}
 * 
 * @author shellpo shih
 * @version 1.0
 */
public class ProtostuffDecoder extends ByteToMessageDecoder {

	private Class<?> clazz;

	public ProtostuffDecoder(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 4) {
			return;
		}
		in.markReaderIndex();
		// Consume Encoder {@code ByteBuf.writeInt()}
		int dataLength = in.readInt();

		// if current {@code ByteBuf.readableBytes()} less real byte array
		// length ,reset readerIndex
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}

		// read data bytes
		byte[] data = new byte[dataLength];
		in.readBytes(data);
		Object obj = Serializations.deserialize(data, clazz);
		out.add(obj);
	}

}

package com.generallycloud.nio.protocol;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BufferedOutputStream;

public class ProtocolEncoderImpl implements ProtocolEncoder {

	public ChannelWriteFuture encode(BaseContext context, ChannelReadFuture future) throws IOException {

		BufferedOutputStream os = future.getWriteBuffer();

		int size = os.size();

		byte[] array = os.array();

		ByteBuf buf = context.getByteBufAllocator().allocate(size);

		buf.put(array, 0, size);

		buf.flip();

		return new ChannelWriteFutureImpl(future, buf);
	}
}

package com.generallycloud.nio.codec.http11;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

//WebSocket规定服务端不准向客户端发送mask过的数据
//A server MUST NOT mask any frames that it sends to the client.
public class WebSocketProtocolEncoder implements ProtocolEncoder {

	public ChannelWriteFuture encode(BaseContext context, ChannelReadFuture readFuture) throws IOException {
		
		WebSocketReadFuture future = (WebSocketReadFuture) readFuture;

		BufferedOutputStream o = future.getWriteBuffer();

		byte [] header;
		
		int size = o.size();
		
		byte header0 = (byte) (0x8f & (future.getType() | 0xf0));
		
		if (size < 126) {
			header = new byte[2];
			header[0] = header0;
			header[1] = (byte) size;
		}else if(size < ((1 << 16) -1)){
			header = new byte[4];
			header[0] = header0;
			header[1] = 126;
			header[3] = (byte)(size & 0xff);
			header[2] = (byte)((size >> 8) & 0x80);
		}else{
			header = new byte[6];
			header[0] = header0;
			header[1] = 127;
			MathUtil.int2Byte(header, size, 2);
		}
		
		ByteBuf buf = context.getByteBufAllocator().allocate(header.length + size);
		
		buf.put(header);
		
		buf.put(o.array(),0,size);
		
		buf.flip();

		return new ChannelWriteFutureImpl(readFuture, buf);
	}
	
//	public IOWriteFuture encodeWithMask(BaseContext context, IOReadFuture readFuture) throws IOException {
//		
//		WebSocketReadFuture future = (WebSocketReadFuture) readFuture;
//
//		BufferedOutputStream o = future.getWriteBuffer();
//
//		byte [] header;
//		
//		int size = o.size();
//		
//		byte header0 = (byte) (0x8f & (future.getType() | 0xf0));
//		
//		if (size < 126) {
//			header = new byte[2];
//			header[0] = header0;
//			header[1] = (byte)(size | 0x80);
//		}else if(size < ((1 << 16) -1)){
//			header = new byte[4];
//			header[0] = header0;
//			header[1] = (byte) (126 | 0xff);
//			header[3] = (byte)(size & 0xff);
//			header[2] = (byte)((size >> 8) & 0x80);
//		}else{
//			header = new byte[6];
//			header[0] = header0;
//			header[1] = (byte) (127 | 0x80);
//			MathUtil.int2Byte(header, size, 2);
//		}
//		
//		ByteBuf buffer = context.getHeapByteBufferPool().allocate(header.length + size + 4);
//		
//		buffer.put(header);
//		
//		byte [] array = o.array();
//		
//		byte [] mask = MathUtil.int2Byte(size);
//		
//		for (int i = 0; i < size; i++) {
//			
//			array[i] = (byte)(array[i] ^ mask[i % 4]);
//		}
//		
//		buffer.put(mask);
//		
//		buffer.put(array,0,size);
//		
//		buffer.flip();
//
//		return new IOWriteFutureImpl(readFuture, buffer);
//	}

}

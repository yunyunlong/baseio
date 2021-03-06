package com.generallycloud.nio.codec.http2.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;

public class Http2PrefaceReadFuture extends AbstractIOReadFuture {

	private ByteBuf	buf;

	private boolean	isComplete;
	
	private static byte [] PREFACE_BINARY = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(); 
	
	private static ByteBuf PREFACE_BUF;
	
	static{
		
		PREFACE_BUF = UnpooledByteBufAllocator.wrap(ByteBuffer.wrap(PREFACE_BINARY));
		
	}

	public Http2PrefaceReadFuture(BaseContext context,ByteBuf buf) {
		super(context);
		this.buf = buf;
	}

	public boolean isSilent() {
		return true;
	}

	private void doComplete(Http2SocketSession session, ByteBuf buf) throws IOException {

		this.isComplete = true;
		
		session.setFrameWillBeRead(Http2FrameType.FRAME_TYPE_FRAME_HEADER);

		if (!isPreface(buf)) {
			throw new IOException("not http2 preface");
		}
		
		ChannelWriteFuture f = new ChannelWriteFutureImpl(this, PREFACE_BUF.duplicate());
		
		session.flush(f);
	}
	
	private boolean isPreface(ByteBuf buf){
		
		if(PREFACE_BINARY.length > buf.remaining()){
			return false;
		}
		
		for (int i = 0; i < PREFACE_BINARY.length; i++) {
			
			if(PREFACE_BINARY[i] != buf.getByte()){
				return false;
			}
		}
		
		return true;
	}

	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!isComplete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doComplete((Http2SocketSession) session, buf);
		}

		return true;
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

}

package com.generallycloud.nio.balance;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.generallycloud.nio.acceptor.ChannelAcceptor;
import com.generallycloud.nio.balance.router.FrontRouter;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class FrontReverseAcceptorHandler extends IOEventHandleAdaptor {

	private Logger			logger	= LoggerFactory.getLogger(FrontReverseAcceptorHandler.class);
	private FrontContext	frontContext;
	private FrontRouter		frontRouter;

	public FrontReverseAcceptorHandler(FrontContext frontContext) {
		this.frontContext = frontContext;
		this.frontRouter = frontContext.getFrontRouter();
	}

	private void broadcast(final BalanceReadFuture future) {

		FrontFacadeAcceptor frontFacadeAcceptor = frontContext.getFrontFacadeAcceptor();

		ChannelAcceptor acceptor = frontFacadeAcceptor.getAcceptor();

		acceptor.offerSessionMEvent(new SessionMEvent() {

			public void fire(BaseContext context, Map<Integer, Session> sessions) {

				if (sessions == null || sessions.size() == 0) {
					return;
				}

				ChannelWriteFuture writeFuture;
				try {
					writeFuture = future.translate();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					return;
				}
				
				Iterator<Session> ss = sessions.values().iterator();

				for (; ss.hasNext();) {

					SocketSession s = (SocketSession) ss.next();

					if (s.getAttribute(FrontContext.FRONT_RECEIVE_BROADCAST) == null) {

						continue;
					}

					ChannelWriteFuture copy = writeFuture.duplicate();

					try {

						s.flush(copy);

					} catch (Exception e) {

						logger.error(e.getMessage(), e);
					}
				}

				ReleaseUtil.release(writeFuture);
			}
		});
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		logger.info("报文来自负载均衡：[ {} ]，报文：{}", session.getRemoteSocketAddress(), future);

		BalanceReadFuture f = (BalanceReadFuture) future;

		if (f.isBroadcast()) {

			broadcast(f);

			logger.info("广播报文");

			return;
		}

		int sessionID = f.getSessionID();

		SocketSession response = frontRouter.getClientSession(sessionID);

		if (response != null) {

			if (response.isClosed()) {

				logger.info("回复报文到客户端失败，连接已丢失：[ {} ],{} ", sessionID, f);

				return;
			}

			ChannelWriteFuture writeFuture = f.translate();

			response.flush(writeFuture);

			logger.info("回复报文到客户端,{}", response);

			return;
		}

		logger.info("回复报文到客户端失败，连接已丢失，且连接已经被移除：[ {} ],{} ", sessionID, f);
	}

	public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
		
		BufferedOutputStream os = future.getWriteBuffer();
		
		String msg;
		
		if (os.size() > 120) {
			msg = new String(os.array(),0,120);
		}else{
			msg = os.toString(session.getEncoding());
		}
		
		logger.error("exceptionCaught,msg="+msg,cause);
	}
	
}

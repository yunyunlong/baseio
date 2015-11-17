package com.yoocent.mtp.server;

import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.LifeCycle;
import com.yoocent.mtp.common.LifeCycleUtil;
import com.yoocent.mtp.server.context.ServletContext;
import com.yoocent.mtp.servlet.ServletContextImpl;

public class ServletContextFactory extends AbstractLifeCycle implements LifeCycle{

	public ServletContextFactory (MTPServer server){
		ContextHolder.server = server;
	}
	
	protected void doStart() throws Exception {
		ContextHolder.initialize();
		factory = this;
	}

	protected void doStop() throws Exception {
		ContextHolder.unpackContext();
		factory = null;
	}
	
	private static ServletContextFactory factory = null;
	
	protected static ServletContext getServletContext() {
		if (factory != null && factory.isRunning()) {
			return ContextHolder.context;
		}
		return null;
	}

	private static class ContextHolder {
		
		private static MTPServer server = null;
		
		private static ServletContext context = null;
		
		static void initialize() throws Exception{
			context = new ServletContextImpl(server);
			context.start();
		}
		
		static void unpackContext() throws Exception{
			LifeCycleUtil.stop(context);
		}
	}
	
}

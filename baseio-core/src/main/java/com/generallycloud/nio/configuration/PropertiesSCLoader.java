package com.generallycloud.nio.configuration;

import java.nio.charset.Charset;

import com.generallycloud.nio.common.SharedBundle;

public class PropertiesSCLoader implements ServerConfigurationLoader{

	public ServerConfiguration loadConfiguration(SharedBundle bundle) throws Exception {

		ServerConfiguration cfg = new ServerConfiguration();

		String encoding = bundle.getProperty("SERVER.ENCODING", "GBK");

		cfg.setSERVER_CORE_SIZE(Runtime.getRuntime().availableProcessors());
		cfg.setSERVER_IO_EVENT_QUEUE(bundle.getIntegerProperty("SERVER_IO_EVENT_QUEUE"));
		cfg.setSERVER_HOST(bundle.getProperty("SERVER.HOST"));
		cfg.setSERVER_TCP_PORT(bundle.getIntegerProperty("SERVER.TCP_PORT"));
		cfg.setSERVER_UDP_PORT(bundle.getIntegerProperty("SERVER.UDP_PORT"));
		cfg.setSERVER_ENCODING(Charset.forName(encoding));
		cfg.setSERVER_SESSION_IDLE_TIME(bundle.getLongProperty("SERVER.SESSION_IDLE_TIME"));
		cfg.setSERVER_MEMORY_POOL_UNIT(bundle.getIntegerProperty("SERVER.MEMORY_POOL_UNIT"));
		cfg.setSERVER_MEMORY_POOL_CAPACITY(bundle.getIntegerProperty("SERVER.MEMORY_POOL_CAPACITY"));
		cfg.setSERVER_MEMORY_POOL_CAPACITY_RATE(bundle.getDoubleProperty("SERVER.MEMORY_POOL_CAPACITY_RATE"));
		
		return cfg;
	}
	
}

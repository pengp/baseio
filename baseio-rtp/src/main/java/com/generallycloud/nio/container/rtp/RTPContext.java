package com.generallycloud.nio.container.rtp;

import java.util.Map;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.container.AbstractPluginContext;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.rtp.server.RTPCreateRoomServlet;
import com.generallycloud.nio.container.rtp.server.RTPJoinRoomServlet;
import com.generallycloud.nio.container.rtp.server.RTPLeaveRoomServlet;
import com.generallycloud.nio.container.rtp.server.RTPRoomFactory;
import com.generallycloud.nio.container.rtp.server.RTPSessionAttachment;
import com.generallycloud.nio.container.rtp.server.RTPSessionEventListener;
import com.generallycloud.nio.container.service.FutureAcceptorService;

public class RTPContext extends AbstractPluginContext {
	
private ServerConfiguration socketChannelConfig;
	
	private ServerConfiguration datagramChannelConfig;

	public ServerConfiguration getSocketChannelConfig() {
		return socketChannelConfig;
	}

	public void setSocketChannelConfig(ServerConfiguration socketChannelConfig) {
		this.socketChannelConfig = socketChannelConfig;
	}

	public ServerConfiguration getDatagramChannelConfig() {
		return datagramChannelConfig;
	}

	public void setDatagramChannelConfig(ServerConfiguration datagramChannelConfig) {
		this.datagramChannelConfig = datagramChannelConfig;
	}

	private RTPRoomFactory		rtpRoomFactory	= new RTPRoomFactory();
	private static RTPContext	instance		;

	public static RTPContext getInstance() {
		return instance;
	}

	public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {

		acceptors.put(RTPJoinRoomServlet.SERVICE_NAME, new RTPJoinRoomServlet());
		acceptors.put(RTPCreateRoomServlet.SERVICE_NAME, new RTPCreateRoomServlet());
		acceptors.put(RTPLeaveRoomServlet.SERVICE_NAME, new RTPLeaveRoomServlet());
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		context.addSessionEventListener(new RTPSessionEventListener());
		
		instance = this;
	}
	
	public RTPSessionAttachment getSessionAttachment(SocketSession session){
		return (RTPSessionAttachment) session.getAttachment(this.getPluginIndex());
	}

	public RTPRoomFactory getRTPRoomFactory() {
		return rtpRoomFactory;
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		instance = null;
	}

}

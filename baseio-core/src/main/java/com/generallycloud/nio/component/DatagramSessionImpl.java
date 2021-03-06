package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketAddress;

import com.generallycloud.nio.protocol.DatagramPacket;

public class DatagramSessionImpl extends SessionImpl implements DatagramSession {

	protected DatagramChannel		channel;

	protected DatagramChannelContext	context;

	public DatagramSessionImpl(DatagramChannel channel, Integer sessionID) {
		super(sessionID);
		this.channel = channel;
		this.context = channel.getContext();
	}

	public void sendPacket(DatagramPacket packet, SocketAddress socketAddress) throws IOException {
		channel.sendPacket(packet, socketAddress);
	}

	public void sendPacket(DatagramPacket packet) throws IOException {
		channel.sendPacket(packet);
	}

	public DatagramChannelContext getContext() {
		return context;
	}

	protected Channel getChannel() {
		return channel;
	}

}

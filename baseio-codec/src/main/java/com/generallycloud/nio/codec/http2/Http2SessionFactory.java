package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.component.SocketSessionFactory;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSocketSession;

public class Http2SessionFactory implements SocketSessionFactory {

	public UnsafeSocketSession newUnsafeSession(SocketChannel channel) {

		return new Http2SocketSessionImpl(channel, channel.getChannelID());
	}
}

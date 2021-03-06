package com.generallycloud.nio.component;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.ByteBuf;

public abstract class LinkableChannelByteBufReader implements ChannelByteBufReader{

	private Linkable<ChannelByteBufReader> next;
	
	public Linkable<ChannelByteBufReader> getNext() {
		return next;
	}

	public void setNext(Linkable<ChannelByteBufReader> next) {
		this.next = next;
	}

	public ChannelByteBufReader getValue() {
		return this;
	}
	
	protected ByteBuf allocate(Session session,int capacity){
		return session.getByteBufAllocator().allocate(capacity);
	}
	
	protected void nextAccept(SocketChannel channel,ByteBuf buffer) throws Exception{
		getNext().getValue().accept(channel, buffer);
	}
	
}

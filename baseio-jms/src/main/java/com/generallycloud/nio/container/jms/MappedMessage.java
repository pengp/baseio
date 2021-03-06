package com.generallycloud.nio.container.jms;

import java.util.Map;

public interface MappedMessage extends Message{
	
	public abstract void put(String key,Object value);
	
	@SuppressWarnings({"rawtypes" })
	public abstract void put(Map value);
	
}

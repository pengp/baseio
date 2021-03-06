package com.generallycloud.test.nio.protobase;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestListenSimple {
	
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");
		String serviceKey = "TestListenSimpleServlet";
		String param = "ttt";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = new FixedSession(connector.connect());

		ProtobaseReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getReadText());
		
		session.listen(serviceKey,new OnReadFuture() {
			
			public void onResponse(SocketSession session, ReadFuture future) {
				ProtobaseReadFuture f = (ProtobaseReadFuture) future;
				System.out.println(f.getReadText());
			}
		});
		
		session.write(serviceKey, param);
		
		ThreadUtil.sleep(1000);
		CloseUtil.close(connector);
		
	}
}

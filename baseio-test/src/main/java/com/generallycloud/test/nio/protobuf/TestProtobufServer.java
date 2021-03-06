package com.generallycloud.test.nio.protobuf;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.protobuf.ProtobufProtocolFactory;
import com.generallycloud.nio.codec.protobuf.future.ProtobufIOEventHandle;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFuture;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest;

public class TestProtobufServer {

	public static void main(String[] args) throws Exception {

		ProtobufIOEventHandle eventHandleAdaptor = new ProtobufIOEventHandle() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				
				ProtobufReadFuture f = (ProtobufReadFuture) future;
				
				SearchRequest req =  (SearchRequest) f.getMessage();

				String message = "yes server already accept your message:\n" + req;

				System.out.println(message);
				
				
				SearchRequest res = SearchRequest.newBuilder().mergeFrom(req).setQuery("query_______").build();
				
				f.writeProtobuf(res.getClass().getName(), res);
				
				session.flush(future);
			}
		};
		
		eventHandleAdaptor.regist(SearchRequest.getDefaultInstance());
		
		SocketChannelContext context = new SocketChannelContextImpl(new ServerConfiguration(18300));

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		context.addSessionEventListener(new LoggerSocketSEListener());

//		context.addSessionEventListener(new SessionAliveSEListener());

		context.setIoEventHandleAdaptor(eventHandleAdaptor);

//		context.setBeatFutureFactory(new NIOBeatFutureFactory());

		context.setProtocolFactory(new ProtobufProtocolFactory());

		acceptor.bind();
	}
}

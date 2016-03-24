package com.gifisan.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.ServerEndpointFactory;
import com.gifisan.nio.server.selector.SelectionAccept;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;
import com.gifisan.nio.server.session.InnerSession;

public class NIOSelectionReader implements SelectionAccept {

	private ServerContext	context			= null;
	private ThreadPool		acceptorDispatcher	= null;

	public NIOSelectionReader(ServerContext context,ThreadPool acceptorDispatcher) {
		this.context = context;
		this.acceptorDispatcher = acceptorDispatcher;
	}

	protected boolean isEndPoint(Object object) {
		
		return object != null && 
				(object.getClass() == ServerNIOEndPoint.class || object instanceof EndPoint);

	}
	
	private ServerEndPoint getEndPoint(ServerContext context,SelectionKey selectionKey) throws SocketException {

		Object attachment = selectionKey.attachment();

		if (isEndPoint(attachment)) {
			return (ServerEndPoint) attachment;
		}
		
		ServerEndpointFactory factory = context.getServerEndpointFactory();

		ServerEndPoint endPoint = factory.manager(context, selectionKey);

		selectionKey.attach(endPoint);

		return endPoint;

	}

	public void accept(SelectionKey selectionKey) throws IOException {
		
		ServerContext context = this.context;

		ServerEndPoint endPoint = getEndPoint(context,selectionKey);

		if (endPoint.isEndConnect() || endPoint.inStream()) {
			return;
		}

		ProtocolDecoder decoder = context.getProtocolDecoder();
		
		ServerProtocolData data = new ServerProtocolData();

		boolean decoded = decoder.decode(endPoint,data,context.getEncoding());

		if (!decoded) {
			if (endPoint.isEndConnect()) {
				CloseUtil.close(endPoint);
			}
			return;
		}

		if (data.isBeat()) {
			return;
		}

//		String sessionID = decoder.getSessionID();

//		NIOSessionFactory factory = context.getNIOSessionFactory();

//		InnerSession session = factory.getSession(endPoint, sessionID,service);
		
//		Request request = session.getRequest(endPoint);
		
//		Response response = session.getResponse(endPoint);
		
		InnerSession session = endPoint.getSession(data.getSessionID());
		
		ServiceAcceptorJob job = session.updateAcceptor(data);
		
//		ServletAcceptJob job = session.updateServletAcceptJob(endPoint);
		
		acceptorDispatcher.dispatch(job);

	}

}

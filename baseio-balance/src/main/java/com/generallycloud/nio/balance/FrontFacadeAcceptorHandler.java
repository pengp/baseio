package com.generallycloud.nio.balance;

import com.generallycloud.nio.balance.router.FrontRouter;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class FrontFacadeAcceptorHandler extends IoEventHandleAdaptor {

	private Logger			logger	= LoggerFactory.getLogger(FrontFacadeAcceptorHandler.class);
	private FrontRouter		frontRouter;
	private byte[]		V		= {};

	public FrontFacadeAcceptorHandler(FrontContext context) {
		this.frontRouter = context.getFrontRouter();
	}

	public void accept(SocketSession session, ReadFuture future) throws Exception {

		BalanceReadFuture f = (BalanceReadFuture) future;

		logger.info("报文来自客户端：[ {} ]，报文：{}", session.getRemoteSocketAddress(), f);

		//FIXME 是否需要设置取消接收广播
		if (f.isReceiveBroadcast()) {
			session.setAttribute(FrontContext.FRONT_RECEIVE_BROADCAST, V);
			return;
		}

		SocketSession routerSession = frontRouter.getRouterSession((SocketSession) session, f);

		if (routerSession == null) {
			logger.info("未发现负载节点，报文分发失败：{} ", f);
			return;
		}
		
		f.setSessionID(session.getSessionID());

		f = f.translate();

		routerSession.flush(f);

		logger.info("分发请求到：[ {} ]", routerSession.getRemoteSocketAddress());
	}

}

package com.generallycloud.nio.container.implementation;

import com.generallycloud.nio.acceptor.ChannelAcceptor;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public class SYSTEMStopServerServlet extends FutureAcceptorService {

	private Logger				logger		= LoggerFactory.getLogger(SYSTEMStopServerServlet.class);

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		
		SocketChannelContext context = session.getContext();
		
		new Thread(new StopServer(context)).start();
		
		future.write("服务端正在处理停止服务命令...");
		
		session.flush(future);
	}

	private class StopServer implements Runnable {
		
		private SocketChannelContext context = null;

		public StopServer(SocketChannelContext context) {
			this.context = context;
		}

		public void run() {

			ThreadUtil.sleep(500);

			logger.info("   [NIOServer] 执行命令：<停止服务>");

			String[] words = new String[] { "五", "四", "三", "二", "一" };

			for (int i = 0; i < 5; i++) {

				logger.info("   [NIOServer] 服务将在" + words[i] + "秒后开始停止，请稍等");

				ThreadUtil.sleep(1000);

			}
			
			ChannelAcceptor acceptor = (ChannelAcceptor) context.getChannelService();
			
			CloseUtil.unbind(acceptor);
		}
	}
}

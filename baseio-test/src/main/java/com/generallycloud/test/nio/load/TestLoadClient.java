package com.generallycloud.test.nio.load;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestLoadClient {

	final static int	time	= 200000;

	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		final Logger logger = LoggerFactory.getLogger(TestLoadClient.class);

		final CountDownLatch latch = new CountDownLatch(time);
		
		final AtomicInteger res = new AtomicInteger();
		final AtomicInteger req = new AtomicInteger();

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			public void accept(SocketSession session, ReadFuture future) throws Exception {
				latch.countDown();
				long count = latch.getCount();
//				if (count % 10 == 0) {
//					if (count < 50) {
//						logger.info("************************================" + count);
//					}
//				}
//				logger.info("res==========={}",res.getAndIncrement());
			}
			
			public void futureSent(SocketSession session, ReadFuture future) {
//				NIOReadFuture f = (NIOReadFuture) future;
//				System.out.println(f.getWriteBuffer());
//				System.out.println("req======================"+req.getAndIncrement());
				
			}
		};

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor);
		
		connector.getContext().setProtocolFactory(new FixedLengthProtocolFactory());
		
		connector.getContext().getServerConfiguration().setSERVER_CORE_SIZE(1);

		SocketSession session = connector.connect();

		System.out.println("################## Test start ####################");

		long old = System.currentTimeMillis();

		for (int i = 0; i < time; i++) {
			
			FixedLengthReadFuture future = new FixedLengthReadFutureImpl(session.getContext());

			future.write("hello server!");

			session.flush(future);
		}

		latch.await();

		long spend = (System.currentTimeMillis() - old);
		System.out.println("## Execute Time:" + time);
		System.out.println("## OP/S:"
				+ new BigDecimal(time * 1000).divide(new BigDecimal(spend), 2, BigDecimal.ROUND_HALF_UP));
		System.out.println("## Expend Time:" + spend);

		CloseUtil.close(connector);

	}
}

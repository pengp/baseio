package com.generallycloud.test.nio.front;

import java.io.IOException;

import com.generallycloud.nio.balance.FrontServerBootStrap;
import com.generallycloud.nio.balance.router.SimpleNextRouter;
import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class TestFrontMain {

	public static void main(String[] args) throws IOException {

		FrontServerBootStrap f = new FrontServerBootStrap();
		
		f.setFrontProtocolFactory(new ProtobaseProtocolFactory());
		f.setFrontReverseProtocolFactory(new ProtobaseProtocolFactory());
		
		
		ServerConfiguration fc = new ServerConfiguration();
		fc.setSERVER_PORT(8600);
		
		ServerConfiguration frc = new ServerConfiguration();
		frc.setSERVER_PORT(8800);
		
		f.setFrontServerConfiguration(fc);
		f.setFrontReverseServerConfiguration(frc);
//		f.setFrontRouter(new HashedFrontRouter(10240));
		f.setFrontRouter(new SimpleNextRouter());
		
		f.startup();
	}
}

package com.generallycloud.nio.container.http11;

import com.generallycloud.nio.codec.http11.HttpContext;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.container.AbstractPluginContext;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.configuration.Configuration;

public class FixedHttpContext extends AbstractPluginContext{
	
	private HttpContext httpContext = new HttpContext();

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		this.httpContext.start();
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		
		LifeCycleUtil.stop(httpContext);
		
		super.destroy(context, config);
	}
}

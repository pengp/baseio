package com.generallycloud.nio.configuration;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

import com.generallycloud.nio.common.SharedBundle;

public class PropertiesSCLoader implements ServerConfigurationLoader {

	public ServerConfiguration loadConfiguration(SharedBundle bundle) throws Exception {

		ServerConfiguration cfg = new ServerConfiguration();

		Method[] methods = cfg.getClass().getDeclaredMethods();

		for (Method method : methods) {

			String name = method.getName();

			if (!name.startsWith("set")) {
				continue;
			}

			if ("setSERVER_ENCODING".equals(name) || "setSERVER_CORE_SIZE".equals(name)) {
				continue;
			}

			if (!method.isAccessible()) {
				method.setAccessible(true);
			}

			Class<?> type = method.getParameterTypes()[0];

			String temp = name.replace("setSERVER_", "SERVER.");

			if (type == String.class) {
				method.invoke(cfg, bundle.getProperty(temp));
			} else if (type == int.class) {
				method.invoke(cfg, bundle.getIntegerProperty(temp));
			} else if (type == double.class) {
				method.invoke(cfg, bundle.getDoubleProperty(temp));
			} else if (type == boolean.class) {
				method.invoke(cfg, bundle.getBooleanProperty(temp));
			} else if (type == long.class) {
				method.invoke(cfg, bundle.getLongProperty(temp));
			} else {
				throw new Exception("unknow type " + type);
			}
		}

		String encoding = bundle.getProperty("SERVER.ENCODING", "GBK");

		cfg.setSERVER_CORE_SIZE(Runtime.getRuntime().availableProcessors());
		cfg.setSERVER_ENCODING(Charset.forName(encoding));

		return cfg;
	}

}

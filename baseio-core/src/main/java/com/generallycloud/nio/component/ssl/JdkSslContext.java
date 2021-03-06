package com.generallycloud.nio.component.ssl;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;

/**
 * An {@link SslContext} which uses JDK's SSL/TLS implementation.
 */
public class JdkSslContext extends SslContext {

	private static final Logger	logger	= LoggerFactory.getLogger(JdkSslContext.class);

	static final String			PROTOCOL	= "TLS";
	static final String[]		PROTOCOLS;
	static final List<String>	DEFAULT_CIPHERS;
	static final Set<String>		SUPPORTED_CIPHERS;

	static {
		SSLContext context;
		int i;
		try {
			context = SSLContext.getInstance(PROTOCOL);
			context.init(null, null, null);
		} catch (Exception e) {
			throw new Error("failed to initialize the default SSL context", e);
		}

		SSLEngine engine = context.createSSLEngine();

		// Choose the sensible default list of protocols.
		final String[] supportedProtocols = engine.getSupportedProtocols();
		Set<String> supportedProtocolsSet = new HashSet<String>(supportedProtocols.length);
		for (i = 0; i < supportedProtocols.length; ++i) {
			supportedProtocolsSet.add(supportedProtocols[i]);
		}
		List<String> protocols = new ArrayList<String>();
		addIfSupported(supportedProtocolsSet, protocols, "TLSv1.2", "TLSv1.1", "TLSv1");

		if (!protocols.isEmpty()) {
			PROTOCOLS = protocols.toArray(new String[protocols.size()]);
		} else {
			PROTOCOLS = engine.getEnabledProtocols();
		}

		// Choose the sensible default list of cipher suites.
		final String[] supportedCiphers = engine.getSupportedCipherSuites();
		SUPPORTED_CIPHERS = new HashSet<String>(supportedCiphers.length);
		for (i = 0; i < supportedCiphers.length; ++i) {
			SUPPORTED_CIPHERS.add(supportedCiphers[i]);
		}
		List<String> ciphers = new ArrayList<String>();
		addIfSupported(SUPPORTED_CIPHERS,
				ciphers,
				// XXX: Make sure to sync this list with
				// OpenSslEngineFactory.
				// GCM (Galois/Counter Mode) requires JDK 8.
				"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
				// AES256 requires JCE unlimited strength jurisdiction
				// policy files.
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
				// GCM (Galois/Counter Mode) requires JDK 8.
				"TLS_RSA_WITH_AES_128_GCM_SHA256", "TLS_RSA_WITH_AES_128_CBC_SHA",
				// AES256 requires JCE unlimited strength jurisdiction
				// policy files.
				"TLS_RSA_WITH_AES_256_CBC_SHA");

		if (ciphers.isEmpty()) {
			// Use the default from JDK as fallback.
			for (String cipher : engine.getEnabledCipherSuites()) {
				if (cipher.contains("_RC4_")) {
					continue;
				}
				ciphers.add(cipher);
			}
		}
		DEFAULT_CIPHERS = Collections.unmodifiableList(ciphers);

		LoggerUtil.prettyNIOServerLog(logger, "Default protocols (JDK): {} ", Arrays.asList(PROTOCOLS));
		LoggerUtil.prettyNIOServerLog(logger, "Default cipher suites (JDK): {}", DEFAULT_CIPHERS);
	}

	private static void addIfSupported(Set<String> supported, List<String> enabled, String... names) {
		for (String n : names) {
			if (supported.contains(n)) {
				enabled.add(n);
			}
		}
	}

	private final String[]						cipherSuites;
	private final List<String>					unmodifiableCipherSuites;
	private final JdkApplicationProtocolNegotiator	apn;
	private final ClientAuth						clientAuth;
	private final SSLContext						sslContext;
	private final boolean						isClient;

	/**
	 * Creates a new {@link JdkSslContext} from a pre-configured
	 * {@link SSLContext}.
	 *
	 * @param sslContext
	 *             the {@link SSLContext} to use.
	 * @param isClient
	 *             {@code true} if this context should create {@link SSLEngine}
	 *             s for client-side usage.
	 * @param clientAuth
	 *             the {@link ClientAuth} to use. This will only be used when
	 * @param isClient
	 *             is {@code false}.
	 */
	public JdkSslContext(SSLContext sslContext, boolean isClient, ClientAuth clientAuth) {
		this(sslContext, isClient, null, IdentityCipherSuiteFilter.INSTANCE,
				JdkDefaultApplicationProtocolNegotiator.INSTANCE, clientAuth, false);
	}

	/**
	 * Creates a new {@link JdkSslContext} from a pre-configured
	 * {@link SSLContext}.
	 *
	 * @param sslContext
	 *             the {@link SSLContext} to use.
	 * @param isClient
	 *             {@code true} if this context should create {@link SSLEngine}
	 *             s for client-side usage.
	 * @param ciphers
	 *             the ciphers to use or {@code null} if the standart should be
	 *             used.
	 * @param cipherFilter
	 *             the filter to use.
	 * @param apn
	 *             the {@link ApplicationProtocolConfig} to use.
	 * @param clientAuth
	 *             the {@link ClientAuth} to use. This will only be used when
	 * @param isClient
	 *             is {@code false}.
	 */
	public JdkSslContext(SSLContext sslContext, boolean isClient, Iterable<String> ciphers,
			CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apn, ClientAuth clientAuth) {
		this(sslContext, isClient, ciphers, cipherFilter, toNegotiator(apn, !isClient), clientAuth, false);
	}

	JdkSslContext(SSLContext sslContext, boolean isClient, Iterable<String> ciphers, CipherSuiteFilter cipherFilter,
			JdkApplicationProtocolNegotiator apn, ClientAuth clientAuth, boolean startTls) {
		this.apn = apn;
		this.clientAuth = clientAuth;
		cipherSuites = cipherFilter.filterCipherSuites(ciphers, DEFAULT_CIPHERS,
				SUPPORTED_CIPHERS);
		unmodifiableCipherSuites = Collections.unmodifiableList(Arrays.asList(cipherSuites));
		this.sslContext = sslContext;
		this.isClient = isClient;
	}

	/**
	 * Returns the JDK {@link SSLContext} object held by this context.
	 */
	public final SSLContext context() {
		return sslContext;
	}

	@Override
	public final boolean isClient() {
		return isClient;
	}

	/**
	 * Returns the JDK {@link SSLSessionContext} object held by this context.
	 */
	@Override
	public final SSLSessionContext sessionContext() {
		if (isServer()) {
			return context().getServerSessionContext();
		} else {
			return context().getClientSessionContext();
		}
	}

	@Override
	public final List<String> cipherSuites() {
		return unmodifiableCipherSuites;
	}

	@Override
	public final long sessionCacheSize() {
		return sessionContext().getSessionCacheSize();
	}

	@Override
	public final long sessionTimeout() {
		return sessionContext().getSessionTimeout();
	}

	public final SSLEngine newEngine() {
		return configureAndWrapEngine(context().createSSLEngine());
	}

	public final SSLEngine newEngine(String peerHost, int peerPort) {
		return configureAndWrapEngine(context().createSSLEngine(peerHost, peerPort));
	}

	private SSLEngine configureAndWrapEngine(SSLEngine engine) {
		engine.setEnabledCipherSuites(cipherSuites);
		engine.setEnabledProtocols(PROTOCOLS);
		engine.setUseClientMode(isClient());
		if (isServer()) {
			switch (clientAuth) {
			case OPTIONAL:
				engine.setWantClientAuth(true);
				break;
			case REQUIRE:
				engine.setNeedClientAuth(true);
				break;
			default:
				break;
			}
		}
		return apn.wrapperFactory().wrapSslEngine(engine, apn, isServer());
	}

	@Override
	public final JdkApplicationProtocolNegotiator applicationProtocolNegotiator() {
		return apn;
	}

	/**
	 * Translate a {@link ApplicationProtocolConfig} object to a
	 * {@link JdkApplicationProtocolNegotiator} object.
	 * 
	 * @param config
	 *             The configuration which defines the translation
	 * @param isServer
	 *             {@code true} if a server {@code false} otherwise.
	 * @return The results of the translation
	 */
	static JdkApplicationProtocolNegotiator toNegotiator(ApplicationProtocolConfig config, boolean isServer) {
		if (config == null) {
			return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
		}

		switch (config.protocol()) {
		case NONE:
			return JdkDefaultApplicationProtocolNegotiator.INSTANCE;
		case ALPN:
			if (isServer) {
				switch (config.selectorFailureBehavior()) {
				case FATAL_ALERT:
					return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
				case NO_ADVERTISE:
					return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
				default:
					throw new UnsupportedOperationException(new StringBuilder("JDK provider does not support ")
							.append(config.selectorFailureBehavior()).append(" failure behavior").toString());
				}
			} else {
				switch (config.selectedListenerFailureBehavior()) {
				case ACCEPT:
					return new JdkAlpnApplicationProtocolNegotiator(false, config.supportedProtocols());
				case FATAL_ALERT:
					return new JdkAlpnApplicationProtocolNegotiator(true, config.supportedProtocols());
				default:
					throw new UnsupportedOperationException(new StringBuilder("JDK provider does not support ")
							.append(config.selectedListenerFailureBehavior()).append(" failure behavior")
							.toString());
				}
			}
		case NPN:
			if (isServer) {
				switch (config.selectedListenerFailureBehavior()) {
				case ACCEPT:
					return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
				case FATAL_ALERT:
					return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
				default:
					throw new UnsupportedOperationException(new StringBuilder("JDK provider does not support ")
							.append(config.selectedListenerFailureBehavior()).append(" failure behavior")
							.toString());
				}
			} else {
				switch (config.selectorFailureBehavior()) {
				case FATAL_ALERT:
					return new JdkNpnApplicationProtocolNegotiator(true, config.supportedProtocols());
				case NO_ADVERTISE:
					return new JdkNpnApplicationProtocolNegotiator(false, config.supportedProtocols());
				default:
					throw new UnsupportedOperationException(new StringBuilder("JDK provider does not support ")
							.append(config.selectorFailureBehavior()).append(" failure behavior").toString());
				}
			}
		default:
			throw new UnsupportedOperationException(new StringBuilder("JDK provider does not support ")
					.append(config.protocol()).append(" protocol").toString());
		}
	}

}

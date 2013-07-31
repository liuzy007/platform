package runtime;

//import java.io.File;

//import javax.management.MBeanServer;

//import java.lang.management.ManagementFactory;

//import javax.management.MBeanServer;

//import java.lang.management.ManagementFactory;
import java.util.Properties;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.core.io.ClassPathResource;

//import org.mortbay.management.MBeanContainer;
//import org.mortbay.thread.BoundedThreadPool;

/**
 * x32 JAVA_OPTS='-Xms256m -Xmx512m -XX:PermSize=256M -XX:MaxPermSize=256M'
 * 
 * x64 JAVA_OPTS='-server -d64 -Xms2g -Xmx20g -XX:PermSize=1g -XX:MaxPermSize=4g -XX:-UseGCOverheadLimit'
 * 
 * @see <url>http://docs.codehaus.org/display/JETTY/Embedding+Jetty</url>
 * @see $ JETTY_HOME}\examples\embedded\src\main\java\org\mortbay\jetty\example\ OneWebApp.java
 * 
 *      Using embedded mode for OneWebApp
 * 
 *      <p>
 *      dependencies: jasper-compiler-5.5.15.jar jasper-compiler-jdt-5.5.15.jar jasper-runtime-5.5.15.jar
 * 
 *      jetty-6.1.7.jar jetty-util-6.1.7.jar
 * 
 *      jsp-api-2.0.jar servlet-api-2.5-6.1.7.jar
 * 
 *      slf4j-api-1.3.1.jar slf4j-simple-1.3.1.jar
 * 
 *      xercesImpl-2.6.2.jar xmlParserAPIs-2.6.2.jar
 * 
 * @author nathanleewei public static void main(String[] args) throws Exception { new RuntimeServer().start(); }
 */
public class RuntimeServer {

	public static final String DEFAULT_PATH = "applicationContext.properties";

	private int port = 8000;

	private String host = "127.0.0.1";
	private String propertyPath = DEFAULT_PATH;
	private String contextPath = "/web";

	private String warApp = "web";

	/**
	 * 
	 * @param propertyPath
	 * @return
	 */
	public RuntimeServer propertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
		return this;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public RuntimeServer port(int port) {
		this.port = port;
		return this;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public RuntimeServer host(String host) {
		this.host = host;
		return this;
	}

	/**
	 * @param warApp
	 *            the warApp to set
	 */
	public RuntimeServer warApp(String warApp) {
		this.warApp = warApp;
		return this;
	}

	public void start() throws Exception {

		//
		long begin = System.nanoTime();// .currentTimeMillis();
		//
		Server server = new Server();

		// thread pool
		// BoundedThreadPool threadPool = new BoundedThreadPool();
		// threadPool.setMaxThreads(100);
		// server.setThreadPool(threadPool);
		if (null != propertyPath) {
			ClassPathResource classPathResource = new ClassPathResource(propertyPath);
			Properties properties = new Properties();
			properties.load(classPathResource.getInputStream());

			port = Integer.parseInt(properties.getProperty("http.port"));
			contextPath = properties.getProperty("http.context.path");
			warApp = properties.getProperty("http.web.root.path");
		}
		//
		Connector connector = new SelectChannelConnector();

		/**
		 * log.warn(76) | header full: java.lang.ArrayIndexOutOfBoundsException: 4096
		 * java.lang.ArrayIndexOutOfBoundsException: 4096 at
		 * org.mortbay.io.ByteArrayBuffer.poke(ByteArrayBuffer.java:268)
		 */
		// connector.setHeaderBufferSize(8192);

		connector.setPort(Integer.getInteger("jetty.port", port).intValue());
		// connector.setHost("127.0.0.1");
		server.setConnectors(new Connector[] { connector });

		//
		WebAppContext webAppContext = new WebAppContext(warApp, contextPath);
		// webAppContext.setContextPath(contextPath);
		// webAppContext.setWar(warUrl);
		/* Solving files are locked on Windows and can't be replaced */
		// if reference by jar, so the "webdefault.xml" must move out
		webAppContext.setDefaultsDescriptor("./runtime/webdefault.xml");
		server.setHandler(webAppContext);

		host = (null == connector.getHost() ? host : connector.getHost());

		// RmiRegistry
		// new org.springframework.remoting.rmi.RmiRegistryFactoryBean().
		// afterPropertiesSet();

		// jmx support
		/*
		 * An example of how to start up Jetty with JMX programatically: http://docs.codehaus.org/display/JETTY/JMX
		 */
		// MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		// if (null == mBeanServer) {
		// mBeanServer = MBeanServerFactory.createMBeanServer();
		// }
		// MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		// server.getContainer().addEventListener(mBeanContainer);
		// mBeanContainer.start();
		// JMXServiceURL jmxServiceURL = new JMXServiceURL(
		// "service:jmx:rmi:///jndi/rmi://127.0.0.1:1099/jmxconnector");
		// JMXConnectorServer jmxConnectorServer =
		// JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceURL, null,
		// mBeanServer);
		// jmxConnectorServer.start();
		/* jetty log */
		// RequestLogHandler requestLogHandler = new RequestLogHandler();
		// File logFile = new File(new File("").getAbsoluteFile(), "jetty.log");
		// logFile.createNewFile();
		// NCSARequestLog requestLog = new
		// NCSARequestLog(logFile.getAbsolutePath());
		// requestLog.setExtended(false);
		// requestLogHandler.setRequestLog(requestLog);
		// server.addHandler(requestLogHandler);
		/* */
		// server.setStopAtShutdown(true);
		// server.setSendServerVersion(true);
		// SslListener listener = new SslListener();
		// listener.setMinThreads(10);
		// listener.setMaxThreads(200);
		// String strUrl = LoadPath.getRootPath(null);
		// listener.setKeystore(strUrl+"etc/maxnet.store");
		// listener.setKeyPassword("maxnet");
		// listener.setPassword("maxnet");
		// //listener.setHost(addr.getHostAddress());
		// listener.setPort(httpsPort);
		// listener.setProtocol("SSL");
		// listener.setConfidentialScheme("https");
		// service.addListener(listener);
		try {
			server.start();
			String url = "http://" + host + ":" + port + contextPath;
			System.out.println("[Jetty Server started in " + (System.nanoTime() - begin) / 1000 / 1000 / 1000 + "s]: " + url);
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}

	}
}

package se.unlogic.eagledns.resolvers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.xbill.DNS.Message;

import p2p.basic.P2P;
import p2p.basic.P2PFactory;
import p2p.basic.Peer;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;
import se.unlogic.eagledns.Request;
import se.unlogic.eagledns.SystemInterface;
import se.unlogic.eagledns.plugins.Plugin;

public class PGridResolver implements Plugin, Resolver {

	protected Logger log = Logger.getLogger(this.getClass());
	protected String name;
	protected SystemInterface systemInterface;
	
	private Properties properties;
	private P2PFactory p2pFactory;
	private P2P p2p;
	private Peer bootstrap;

	public void init(String name) throws Exception {
		this.name = name;
		
		// connect to p-grid
		properties = createProperties();
		p2pFactory = PGridP2PFactory.sharedInstance();
		p2p = p2pFactory.createP2P(properties);
		bootstrap = createBootstrapHost();
		p2p.join(bootstrap);
	}
	
	public Properties createProperties() throws IOException {
		Properties properties = new Properties();
		properties.setProperty(PGridP2P.PROP_LOCAL_PORT, String.valueOf(1337));
		properties.setProperty(PGridP2P.PROP_DEBUG_LEVEL, "0");
		properties.setProperty(PGridP2P.PROP_VERBOSE_MODE, "true");
		return properties;
	}

	public Peer createBootstrapHost() throws UnknownHostException {
		InetAddress ip = InetAddress.getByName("localhost");
		int port = 1805;
		return p2pFactory.createPeer(ip, port);
	}

	public void shutdown() throws Exception {
	}

	public Message generateReply(Request request) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSystemInterface(SystemInterface systemInterface) {
		this.systemInterface = systemInterface;
	}
}

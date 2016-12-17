package se.unlogic.eagledns.zoneproviders.pgrid;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Zone;
import org.xml.sax.SAXException;

import p2p.basic.GUID;
import p2p.basic.P2P;
import p2p.basic.P2PFactory;
import p2p.basic.Peer;
import p2p.storage.DataItem;
import p2p.storage.Query;
import p2p.storage.Storage;
import p2p.storage.StorageFactory;
import p2p.storage.Type;
import p2p.storage.TypeHandler;
import p2p.storage.events.SearchListener;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;
import pgrid.interfaces.storage.PGridStorageFactory;
import se.unlogic.eagledns.SecondaryZone;
import se.unlogic.eagledns.SystemInterface;
import se.unlogic.eagledns.zoneproviders.ZoneProvider;
import se.unlogic.standardutils.xml.XMLParser;
import test.SimpleTypeHandler;

public class PGridZoneProvider implements ZoneProvider, SearchListener {

	private final Logger log = Logger.getLogger(this.getClass());
	private String name;
	private Collection<Zone> zones;
	
	private Properties properties;
	private P2PFactory p2pFactory;
	private StorageFactory storageFactory;
	private P2P p2p;
	private Peer bootstrap;
	private Storage storage;
	private Type type;
	private TypeHandler handler;

	public void init(String name) throws Exception {
		this.name = name;
		properties = createProperties();
		p2pFactory = PGridP2PFactory.sharedInstance();
		p2p = p2pFactory.createP2P(properties);
		bootstrap = createBootstrapHost();
		p2p.join(bootstrap);

		storageFactory = PGridStorageFactory.sharedInstance();
		storage = storageFactory.createStorage(p2p);
		type = storageFactory.createType("SimpleType");
		handler = new SimpleTypeHandler(type);
		storageFactory.registerTypeHandler(type, handler);
		insertZones();
	}

	public Collection<Zone> getPrimaryZones() {
		zones = new ArrayList<Zone>();
		Query query = storageFactory.createQuery(type, "a", "z");
		storage.search(query, this);
		try {
			Thread.sleep(1000 * 5);
		} catch (InterruptedException e) {}
		System.out.println("P-Grid zone provider found " + zones.size() + " zones.");
		if (zones.isEmpty() == false)
			return zones;
		return null;
	}

	public Collection<SecondaryZone> getSecondaryZones() {
		return null;
	}

	public void zoneUpdated(SecondaryZone secondaryZone) {
	}

	public void zoneChecked(SecondaryZone secondaryZone) {
	}

	public void shutdown() throws Exception {
	}

	public void newSearchResult(GUID guid, Collection results) {
		for (Iterator it = results.iterator(); it.hasNext();) {
			DataItem item = (DataItem) it.next();
			String data = (String)item.getData();
			System.out.println("Found item: " + data);
			String first = data.split("\n")[0];
			String name = first.split("\t")[0];
			name = name.substring(0, name.length());
			
			Path file = Paths.get("temp.txt");
			List<String> lines = Arrays.asList(data);
			Charset charset = Charset.forName("UTF-8");
			try {
				Files.write(file, lines, charset);
				Zone zone = new Zone(new Name(name), "temp.txt");
				zones.add(zone);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void noResultsFound(GUID guid) {
	}

	public void searchFailed(GUID guid) {
		System.out.println("Search failed.");
	}

	public void searchFinished(GUID guid) {
	}

	public void searchStarted(GUID guid, String message) {
	}

	private Properties createProperties() throws IOException, SAXException, ParserConfigurationException {
		Properties properties = new Properties();
		properties.setProperty(PGridP2P.PROP_LOCAL_PORT, String.valueOf(getPort()));
		properties.setProperty(PGridP2P.PROP_DEBUG_LEVEL, "0");
		properties.setProperty(PGridP2P.PROP_VERBOSE_MODE, "false");
		return properties;
	}

	private Peer createBootstrapHost() throws UnknownHostException {
		InetAddress ip = InetAddress.getByName("localhost");
		int port = 1805;
		return p2pFactory.createPeer(ip, port);
	}
	
	// inserts local zones into the p-grid network
	private void insertZones() {
		File zoneDir = new File("zones");
		File[] files = zoneDir.listFiles();

		Collection<Zone> zones = new ArrayList<Zone>(files.length);
		for (File zoneFile : files) {
			if (!zoneFile.canRead()) {
				log.error("PGridZoneProvider " + name + " unable to access zone file " + zoneFile);
				continue;
			}
			Name origin;
			try {
				origin = Name.fromString(zoneFile.getName(), Name.root);
				Zone zone = new Zone(origin, zoneFile.getPath());
				log.debug("PGridZoneProvider " + name + " successfully parsed zone file " + zoneFile.getName());
				zones.add(zone);
			} catch (TextParseException e) {
				log.error("PGridZoneProvider " + name + " unable to parse zone file " + zoneFile.getName(), e);
			} catch (IOException e) {
				log.error("Unable to parse zone file " + zoneFile + " in FileZoneProvider " + name, e);
			}
		}
		
		Collection<DataItem> items = new ArrayList<DataItem>();
		for (Zone zone : zones) {
			DataItem item = handler.createDataItem(zone);
			items.add(item);
		}
	
		System.out.println("P-Grid zone provider inserting " + items.size() + " zones into network...");
		storage.insert(items);
		((PGridP2P)p2p).setInitExchanges(true);
	}
	
	private String getPort() throws SAXException, IOException, ParserConfigurationException {
		XMLParser configFile;
		configFile = new XMLParser("conf/config.xml");
		Integer port = configFile.getIntegers("/Config/System/Port").get(0);
		return String.valueOf(port);
	}

	public void setSystemInterface(SystemInterface systemInterface) {
	}
}

package se.unlogic.eagledns.zoneproviders.pgrid;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import org.xbill.DNS.Zone;

import p2p.basic.GUID;
import p2p.basic.Key;
import p2p.basic.Message;
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
import test.SimpleTypeHandler;
import test.WaitingArea;

public class PGridZoneProvider implements ZoneProvider, SearchListener {

	private Properties properties;
	private P2PFactory p2pFactory;
	private StorageFactory storageFactory;
	private P2P p2p;
	private Peer bootstrap;
	private Storage storage;

	public void init(String name) throws Exception {
		properties = createProperties();
		p2pFactory = PGridP2PFactory.sharedInstance();
		p2p = p2pFactory.createP2P(properties);
		bootstrap = createBootstrapHost();
		p2p.join(bootstrap);

		// checking peers
		Peer local = p2p.getLocalPeer();
		System.out.println("Local: " + local.getIP() + ":" + local.getPort());
		Peer[] neighbours = p2p.getNeighbors();
		for (Peer neighbour : neighbours) {
			System.out.println("Neighbour: " + neighbour.getIP() + ":" + neighbour.getPort());
		}

		storageFactory = PGridStorageFactory.sharedInstance();
		storage = storageFactory.createStorage(p2p);
		Type type = storageFactory.createType("SimpleType");
		TypeHandler handler = new SimpleTypeHandler(type);
		storageFactory.registerTypeHandler(type, handler);

		// testing insert and search
		Collection<DataItem> items = new ArrayList<DataItem>();
		items.add(storageFactory.createDataItem(type, "Test item 1"));
		items.add(storageFactory.createDataItem(type, "Pest item 2"));
		items.add(storageFactory.createDataItem(type, "Test item 3"));
		storage.insert(items);

		Query query = storageFactory.createQuery(type, "");
		storage.search(query, this);
	}

	public Collection<Zone> getPrimaryZones() {
		// TODO
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
		System.out.println("Search result received:");
		for (Iterator it = results.iterator(); it.hasNext();) {
			DataItem item = (DataItem) it.next();
			System.out.println(item.getData().toString());
		}
	}

	public void noResultsFound(GUID guid) {
		System.out.println("No results found for search.");
	}

	public void searchFailed(GUID guid) {
		System.out.println("Search failed.");
	}

	public void searchFinished(GUID guid) {
		System.out.println("Search finished.");
	}

	public void searchStarted(GUID guid, String message) {
		System.out.println("Search started.");
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

	public void setSystemInterface(SystemInterface systemInterface) {
	}
}

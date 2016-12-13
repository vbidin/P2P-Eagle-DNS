package test.demo;

import p2p.basic.*;
import p2p.basic.events.P2PListener;
import p2p.storage.*;
import p2p.storage.events.SearchListener;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;
import pgrid.interfaces.storage.PGridStorageFactory;
import pgrid.PGridHost;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.io.UnsupportedEncodingException;

import test.SimpleTypeHandler;
import test.WaitingArea;

/**
 * Sample peer listens for messages and displays them on the command line.
 *
 * @author A. Nevski and Renault JOHN
 */
public class HelloWorld implements SearchListener, P2PListener {

	private P2PFactory p2pFactory;
	private P2P p2p;

	private StorageFactory storageFactory;
	private Storage storage;

	/**
	 * Create an instance of sample receiving peer.
	 *
	 */
	public HelloWorld() {
	}


	/**
	 * Wait indefinitely for messages.
	 *
	 * @param bootIP     address of host to bootstrap from
	 * @param bootPort   service port of the boostrapping host
	 * @param properties additional properties needed to initialize the peer-to-peer layer
	 */
	public void run(InetAddress bootIP, int bootPort, Properties properties) {
		/** P2P INITIALIZATION **/

		// Set the debug mode to the minimum. Debug can be set to a number between 0-3
		properties.setProperty(PGridP2P.PROP_DEBUG_LEVEL, "0");
		// Use a verbose mode
		properties.setProperty(PGridP2P.PROP_VERBOSE_MODE, "true");

		// Get an instance of the P2PFactory
		p2pFactory = PGridP2PFactory.sharedInstance();
		System.out.println("Acquired P-Grid factory reference. ");

		// Get an instance of the P2P object, aka P-Grid
		p2p = p2pFactory.createP2P(properties);
		System.out.println("Created a P2P instance. ");

		// Create an instance of the bootstrap host that will be use to bootstrap the
		// network
		Peer bootstrap = null;
		try {
			bootstrap = p2pFactory.createPeer(bootIP, bootPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		System.out.println("Bootstrap Peer instance Created.");

		// Try to join the network
		p2p.join(bootstrap);
		System.out.println("Network joined. Current key range (path) is: " + ((PGridHost)p2p.getLocalPeer()).getPath());

		// Get an instance of the StorageFactory
		storageFactory = PGridStorageFactory.sharedInstance();
		System.out.println("Storage factory reference acquired. ");


		// Get an instance of the Storage object, aka PGridStorage
		storage = storageFactory.createStorage(p2p);
		System.out.println("Storage instance acquired. ");

		// creating and registering data type.
		Type type = storageFactory.createType("SimpleType");
		TypeHandler handler = new SimpleTypeHandler(type);
		storageFactory.registerTypeHandler(type, handler);

		// add this object as a p2p listener
		p2p.addP2PListener(this);

		/** Storage Demo **/
		// In this small demo program we want to insert some data inside
		// the network, update some of them and perform
		// a query and a range query.
		// This demo comes first since the network is build upon data items
		// inserted in the network
		storageDemo(type);

		// wait for the result set
		System.out.println("Wait a bit to start the routing demo.");
		WaitingArea.waitTillSignal(1000 * 60 * 60);

		/** P2P Demo **/
		// In this demo, we want to send a custom message to an other
		// peer to illustrate the routing facilities of the common interface.
		p2pDemo();

		// wait for the result set
		System.out.println("Demo over, will shutdown in 30s.");
		test.WaitingArea.waitTillSignal(1000 * 60 * 60);

		// shuting down the p2p. This is not a mandatory phase,
		// but it can avoid some manager to throw exceptions
		System.out.println("shutdown ...");
		p2p.leave();
		storage.shutdown();

	}

	/**
	 * In this small demo program we want to insert some data inside
	 * the network, update some of them and perform
	 * a query and a range query.
	 */
	protected void storageDemo(Type type) {
		Random rnd = new Random(System.currentTimeMillis()); // random generator

		// creating 10 data items. In order to have an interesting trie, some data items
		// are created with a number as prefix and other with a lettre. This will be useful
		// for the small search at the end of this method.
		Vector items = new Vector();
		String data;
		DataItem item;
		int number = 10;
		int counter = 0;
		boolean side=false;
		for (int i=0; i<number; i++) {
			if (rnd.nextFloat()>=0.5) {
				++counter;
				side = true;
			} else {
				side = false;
			}

			data = (side?counter+" ":"")+"This is a data object ("+rnd.nextInt(10000)+").";

			// create a data item object through the storage interface
			item = storageFactory.createDataItem(type, data);
			items.add(item);
		}
		System.out.println(number+" data items have been created.");


		//insert our date items in the system
		storage.insert(items);
		
		System.out.println("Inserted data items.");

		// ensure that PGrid will build the network (this is optional, by default P-Grid build the network)
		// you can have a look at PGrid.ini at the root of you home directory to see if initExchange is set
		// to true.
		((PGridP2P)p2p).setInitExchanges(true);


		// wait for the network to be created.
		System.out.println("Waiting while structuring the network.");
		WaitingArea.waitTillSignal(1000 * 60 * 5);


		System.out.println("Network joined. Current key range (path) is: "
				+ ((PGridHost)p2p.getLocalPeer()).getPath());

		// perform an update on a randomly chosen data item
		item = (DataItem)items.get(rnd.nextInt(number));
		item.setData("Updated data object ("+rnd.nextInt(10000)+").");
		Vector toUpdate = new Vector();
		toUpdate.add(item);

		System.out.println("Update one data item.");
		// do the actual update
		storage.update(toUpdate);

		System.out.println("Wait to ensure that the update has been propagated.");
		WaitingArea.waitTillSignal(1000 * 30);

		// Create a query to retrieve all data items starting with "Updated"
		Query query = storageFactory.createQuery(type, "Updated");
		System.out.println("Creating a query with keyword '" + query.getLowerBound() + "'");
		System.out.println("Searched for data items.");
		storage.search(query, this);

		// wait for the result set
		WaitingArea.waitTillSignal(1000 * 20);

		// Create a range query to retrieve all data items with a value between 1* and 5*
		Query rangeQuery = storageFactory.createQuery(type, "1", "2");
		System.out.println("Creating a range query with keywords '" + rangeQuery.getLowerBound() + "-"+ rangeQuery.getHigherBound() +"'");
		System.out.println("Searched for data items.");
		storage.search(rangeQuery, this);

		// wait for the result set
		WaitingArea.waitTillSignal(1000 * 20);


		// Create a range query to retrieve all data items with a value between 1* and 5*
		rangeQuery = storageFactory.createQuery(type, "3", "5");
		System.out.println("Creating a range query with keywords '" + rangeQuery.getLowerBound() + "-"+ rangeQuery.getHigherBound() +"'");
		System.out.println("Searched for data items.");
		storage.search(rangeQuery, this);

		// wait for the result set
		test.WaitingArea.waitTillSignal(1000 * 20);


		// Create a range query to retrieve all data items with a value between 1* and 5*
		rangeQuery = storageFactory.createQuery(type, "a", "t");
		System.out.println("Creating a range query with keywords '" + rangeQuery.getLowerBound() + "-"+ rangeQuery.getHigherBound() +"'");
		System.out.println("Searched for data items.");
		storage.search(rangeQuery, this);


	}

	/**
	 * In this demo, we want to send a custom message to an other
	 * peer to illustrate the routing facilities of the common interface.
	 */
	protected void p2pDemo() {


		String s = "Welcome to P-Grid! I'm peer "+p2p.getLocalPeer().getIP().getCanonicalHostName()+
				" on port "+p2p.getLocalPeer().getPort()+".";

		// create a custom message
		Message message = null;
		String str="";
		try {
			message = p2pFactory.createMessage(s.getBytes("ISO-8859-1"));
			str = new String(s.getBytes("ISO-8859-1"), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		System.out.println("Message created: " + str);
		// generate the key that will be used to route the message
		Key key = p2pFactory.generateKey(s);
		System.out.println("Destination key: " + key);
		// route the message
		p2p.route(key, message);
		System.out.println("Message sent. ");

	}

	/**
	 * Let the sample peer send a message.
	 *
	 * @param args the command line arguments to be passed on to the peer-to-peer layer
	 * @see test.CommandLineArgs usage
	 */
	public static void main(String[] args) {
		test.CommandLineArgs cla = new test.CommandLineArgs(args);
		HelloWorld storage = new HelloWorld();
		storage.run(cla.getAddress(), cla.getPort(), cla.getOtherProperties());
	}

	/**
	 * Implementation of the P2PListener interface. Is invoked
	 * when a new message is received and this node is responsible
	 * for the message's destination key. Reassembles the message
	 * text and displays it on the command line.
	 */
	public void newMessage(Message message, Peer origin) {
		String str="";
		try {
			str = new String(message.getData(), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println("Received message: " + str + " from " + origin.toString());
	}


	/**
	 * Invoked when a new search result is available
	 *
	 * @param guid    the GUID of the original query
	 * @param results a Collection of DataItems matching the original query
	 */
	public void newSearchResult(GUID guid, Collection results) {
		System.out.println("Search result received:");
		for (Iterator it = results.iterator(); it.hasNext();) {
			DataItem item = (DataItem)it.next();
			System.out.println(item.getData().toString());
		}
	}

	/**
	 * Invoked when a search resulted in no results.
	 *
	 * @param guid the GUID of the original query
	 */
	public void noResultsFound(GUID guid) {
		System.out.println("No results found for search.");
	}

	/**
	 * Invoked when a search failed.
	 *
	 * @param guid the GUID of the original query
	 */
	public void searchFailed(GUID guid) {
		System.out.println("Search failed.");
	}

	/**
	 * Invoked when a search finished.
	 *
	 * @param guid the GUID of the original query
	 */
	public void searchFinished(GUID guid) {
		System.out.println("Search finished.");
	}

	/**
	 * Invoked when a search started (reached a responsible peer).
	 *
	 * @param guid the GUID of the original query
	 * @param message the explanation message.
	 */
	public void searchStarted(GUID guid, String message) {
		System.out.println("Search started.");
	}
}

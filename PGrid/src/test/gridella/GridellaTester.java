package test.gridella;

import p2p.basic.P2P;
import p2p.basic.P2PFactory;
import p2p.storage.DataItem;
import p2p.storage.Storage;
import p2p.storage.StorageFactory;
import p2p.storage.TypeHandler;
import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;
import pgrid.interfaces.storage.PGridStorageFactory;
import pgrid.util.Tokenizer;
import pgrid.util.logging.LogFormatter;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.security.SecureRandom;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class GridellaTester implements Runnable {

	public static final Logger LOGGER = Logger.getLogger("PGrid.GridellaTester");
	private final int DEBUG_LEVEL = 2;
	private final int RANDOM_FILES = 10;
	private final int INIT_TIME_FRAME = 10; // in sec.

	private PGridP2P pGrid = null;
	private static java.util.Properties properties = new java.util.Properties();
	private static java.security.SecureRandom rnd = null;

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	private static final GridellaTester SHARED_INSTANCE = new GridellaTester();

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static GridellaTester sharedInstance() {
		return SHARED_INSTANCE;
	}

	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		Constants.initChildLogger(LOGGER, formatter, null);
	}

	protected GridellaTester() {
		pGrid = PGridP2P.sharedInstance();
		rnd = new SecureRandom();
	}


	public void run() {
		// GridellaTesterFiles testFiles = new GridellaTesterFiles();
		// testFiles.createFiles("C:/Documents/Papers");

		properties.setProperty(PGridP2P.PROP_DEBUG_LEVEL, String.valueOf(DEBUG_LEVEL));
		properties.setProperty(PGridP2P.PROP_VERBOSE_MODE, "false");

		P2PFactory p2pFactory = PGridP2PFactory.sharedInstance();
		LOGGER.finer("Acquired P-Grid factory reference.");
		P2P p2pService = p2pFactory.createP2P(properties);
		LOGGER.finer("Created a P2P instance.");

		// init storage interface
		StorageFactory storageFactory = PGridStorageFactory.sharedInstance();
		LOGGER.finer("Acquired Storage factory reference. ");

		Storage storageService = storageFactory.createStorage(p2pService);
		LOGGER.finer("Created a Storage instance. ");

		LOGGER.finer("init Gridella tester ...");

		// if local peer is a bootstrap peers => no wait
		if (!PGridP2P.sharedInstance().getMaintenanceManager().isBootstrapHost()) {
			// start to bootstrap
			if (PGridP2P.sharedInstance().getMaintenanceManager().getBootstrapHosts().size() == 0) {
				LOGGER.severe("no bootstrap host(s) to bootstrap ...");
				System.exit(-1);
			}

			long initDelay = rnd.nextInt(INIT_TIME_FRAME);
			LOGGER.finer("sleep " + initDelay + " sec. before bootstrapping ...");
			try {
				Thread.sleep(initDelay * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}

		// creating and registering data type
		p2p.storage.Type type = storageFactory.createType("text/file");
		TypeHandler handler = new FileTypeHandler(type);
		storageFactory.registerTypeHandler(type, handler);

		// creating data item
		GridellaTesterFiles testFiles = new GridellaTesterFiles();
		String[] filenames = testFiles.uniformFilenames(RANDOM_FILES);
		Vector items = new Vector();
		for (int i = 0; i < (int)(RANDOM_FILES * 1.0); i++) {
			String fileName = filenames[i];
			String[] parts = Tokenizer.tokenize(fileName, "\t");
			DataItem dataItem = new XMLFileDataItem(p2pFactory.generateGUID(), type, p2pFactory.generateKey(parts[1]), pGrid.getLocalHost(), 0, parts[0], parts[1], Integer.parseInt(parts[2]), "", parts[1]);
			items.add(dataItem);

			StringTokenizer st = new StringTokenizer(parts[1], " .:;_-");
			while(st.hasMoreElements()) {
				String tmp = st.nextToken().trim();
				if (tmp.length() == 0) continue;
				items.add(new XMLFileDataItem(p2pFactory.generateGUID(), type, p2pFactory.generateKey(tmp), pGrid.getLocalHost(), 0, parts[0], parts[1], Integer.parseInt(parts[2]), "", tmp));
			}
		}
		// inserting the data items
		storageService.insert(items);
		pGrid.getStorageManager().writeDataTable();
		LOGGER.finer("Inserted data items.");

		LOGGER.finer("start to bootstrap ...");
		pGrid.join();

		LOGGER.finer("wait till shutdown ...");
		// this main makes something and catch Ctrl-C in order to clean its work env before to leave.
		final Thread mainThread = Thread.currentThread();
		// makes a signal holder which will interrupt the main thread if Ctrl-C is typed
		try {
			Signal.handle(new Signal("TERM"), new SignalHandler() {
				public void handle(Signal sig) {
					System.out.println("Shutdown Gridella tester ...");
					pGrid.shutdown();
					mainThread.interrupt();
				}
			});
		} catch (IllegalArgumentException exc) {
			// do nothing
		}
		Object sync = new Object();
		synchronized (sync) {
			try {
				sync.wait();
			} catch (InterruptedException exc) {
				System.exit(0);
			}
		}
	}

	/**
	 * Let the sample peer send a message.
	 *
	 * @param args the command line arguments to be passed on to the peer-to-peer layer
	 */
	public static void main(String[] args) {
		GridellaTester tester = GridellaTester.sharedInstance();
		if (args.length > 0) {
			int localPort = Integer.parseInt(args[0]);
			properties.setProperty(PGridP2P.PROP_LOCAL_PORT, String.valueOf(localPort));
		}
		tester.run();
	}

}

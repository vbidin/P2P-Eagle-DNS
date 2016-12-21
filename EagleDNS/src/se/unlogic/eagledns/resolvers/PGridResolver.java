package se.unlogic.eagledns.resolvers;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.DNAMERecord;
import org.xbill.DNS.ExtendedFlags;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Header;
import org.xbill.DNS.Message;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.NameTooLongException;
import org.xbill.DNS.OPTRecord;
import org.xbill.DNS.Opcode;
import org.xbill.DNS.RRset;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.SetResponse;
import org.xbill.DNS.TSIG;
import org.xbill.DNS.TSIGRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Zone;
import org.xbill.DNS.Type;
import org.xml.sax.SAXException;

import p2p.basic.GUID;
import p2p.basic.P2P;
import p2p.basic.P2PFactory;
import p2p.basic.Peer;
import p2p.storage.DataItem;
import p2p.storage.Query;
import p2p.storage.Storage;
import p2p.storage.StorageFactory;
import p2p.storage.TypeHandler;
import p2p.storage.events.SearchListener;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;
import pgrid.interfaces.storage.PGridStorageFactory;
import se.unlogic.eagledns.EagleDNS;
import se.unlogic.eagledns.Request;
import se.unlogic.eagledns.SystemInterface;
import se.unlogic.eagledns.plugins.Plugin;
import se.unlogic.standardutils.net.SocketUtils;
import se.unlogic.standardutils.xml.XMLParser;
import test.SimpleTypeHandler;

public class PGridResolver implements Plugin, Resolver, SearchListener {

	private Logger log = Logger.getLogger(this.getClass());
	private SystemInterface systemInterface;
	private String name;
	private List<Zone> zones;

	private Properties properties;
	private P2PFactory p2pFactory;
	private StorageFactory storageFactory;
	private P2P p2p;
	private Peer bootstrap;
	private Storage storage;
	private p2p.storage.Type type;
	private TypeHandler handler;

	public void init(String name) throws Exception {
		this.name = name;
		log.setLevel(Level.DEBUG);
		
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

	public Message generateReply(Request request) throws Exception {

		Message query = request.getQuery();
		Record queryRecord = query.getQuestion();

		if (queryRecord == null) {
			return null;
		}

		Name name = queryRecord.getName();
		Zone zone = findBestZone(name);

		if (zone != null) {

			log.debug("Resolver " + this.name + " processing request for " + name + ", matching zone found ");

			Header header;
			// boolean badversion;
			int flags = 0;

			header = query.getHeader();
			if (header.getFlag(Flags.QR)) {
				return null;
			}
			if (header.getRcode() != Rcode.NOERROR) {
				return null;
			}
			if (header.getOpcode() != Opcode.QUERY) {
				return null;
			}

			TSIGRecord queryTSIG = query.getTSIG();
			TSIG tsig = null;
			if (queryTSIG != null) {
				tsig = systemInterface.getTSIG(queryTSIG.getName());
				if (tsig == null || tsig.verify(query, request.getRawQuery(), request.getRawQueryLength(),
						null) != Rcode.NOERROR) {
					return null;
				}
			}

			OPTRecord queryOPT = query.getOPT();
			// if (queryOPT != null && queryOPT.getVersion() > 0) {
			// // badversion = true;
			// }

			if (queryOPT != null && (queryOPT.getFlags() & ExtendedFlags.DO) != 0) {
				flags = EagleDNS.FLAG_DNSSECOK;
			}

			Message response = new Message(query.getHeader().getID());
			response.getHeader().setFlag(Flags.QR);
			if (query.getHeader().getFlag(Flags.RD)) {
				response.getHeader().setFlag(Flags.RD);
			}

			response.addRecord(queryRecord, Section.QUESTION);

			int type = queryRecord.getType();
			int dclass = queryRecord.getDClass();
			if (type == Type.AXFR && request.getSocket() != null) {
				return doAXFR(name, query, tsig, queryTSIG, request.getSocket());
			}
			if (!Type.isRR(type) && type != Type.ANY) {
				return null;
			}

			byte rcode = addAnswer(response, name, type, dclass, 0, flags, zone);

			if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN) {
				return EagleDNS.errorMessage(query, rcode);
			}

			addAdditional(response, flags);

			if (queryOPT != null) {
				int optflags = (flags == EagleDNS.FLAG_DNSSECOK) ? ExtendedFlags.DO : 0;
				OPTRecord opt = new OPTRecord((short) 4096, rcode, (byte) 0, optflags);
				response.addRecord(opt, Section.ADDITIONAL);
			}

			response.setTSIG(tsig, Rcode.NOERROR, queryTSIG);

			return response;

		} else {

			log.debug("Resolver " + this.name + " ignoring request for " + name + ", no matching zone found");

			return null;
		}
	}

	private final void addAdditional(Message response, int flags) {

		addAdditional2(response, Section.ANSWER, flags);
		addAdditional2(response, Section.AUTHORITY, flags);
	}

	private byte addAnswer(Message response, Name name, int type, int dclass, int iterations, int flags, Zone zone) {

		SetResponse sr;
		byte rcode = Rcode.NOERROR;

		if (iterations > 6) {
			return Rcode.NOERROR;
		}

		if (type == Type.SIG || type == Type.RRSIG) {
			type = Type.ANY;
			flags |= EagleDNS.FLAG_SIGONLY;
		}

		if (zone == null) {

			zone = findBestZone(name);
		}

		if (zone != null) {
			sr = zone.findRecords(name, type);

			if (sr.isNXDOMAIN()) {
				response.getHeader().setRcode(Rcode.NXDOMAIN);
				if (zone != null) {
					addSOA(response, zone);
					if (iterations == 0) {
						response.getHeader().setFlag(Flags.AA);
					}
				}
				rcode = Rcode.NXDOMAIN;
			} else if (sr.isNXRRSET()) {
				if (zone != null) {
					addSOA(response, zone);
					if (iterations == 0) {
						response.getHeader().setFlag(Flags.AA);
					}
				}
			} else if (sr.isDelegation()) {
				RRset nsRecords = sr.getNS();
				addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
			} else if (sr.isCNAME()) {
				CNAMERecord cname = sr.getCNAME();
				RRset rrset = new RRset(cname);
				addRRset(name, response, rrset, Section.ANSWER, flags);
				if (zone != null && iterations == 0) {
					response.getHeader().setFlag(Flags.AA);
				}
				rcode = addAnswer(response, cname.getTarget(), type, dclass, iterations + 1, flags, null);
			} else if (sr.isDNAME()) {
				DNAMERecord dname = sr.getDNAME();
				RRset rrset = new RRset(dname);
				addRRset(name, response, rrset, Section.ANSWER, flags);
				Name newname;
				try {
					newname = name.fromDNAME(dname);
				} catch (NameTooLongException e) {
					return Rcode.YXDOMAIN;
				}
				rrset = new RRset(new CNAMERecord(name, dclass, 0, newname));
				addRRset(name, response, rrset, Section.ANSWER, flags);
				if (zone != null && iterations == 0) {
					response.getHeader().setFlag(Flags.AA);
				}
				rcode = addAnswer(response, newname, type, dclass, iterations + 1, flags, null);
			} else if (sr.isSuccessful()) {
				RRset[] rrsets = sr.answers();
				for (RRset rrset : rrsets) {
					addRRset(name, response, rrset, Section.ANSWER, flags);
				}
				if (zone != null) {
					addNS(response, zone, flags);
					if (iterations == 0) {
						response.getHeader().setFlag(Flags.AA);
					}
				}
			}
		}

		return rcode;
	}

	private Message doAXFR(Name name, Message query, TSIG tsig, TSIGRecord qtsig, Socket socket) {

		boolean first = true;

		Zone zone = this.findBestZone(name);

		if (zone == null) {

			return EagleDNS.errorMessage(query, Rcode.REFUSED);

		}

		// Check that the IP requesting the AXFR is present as a NS in this zone
		boolean axfrAllowed = false;

		Iterator<?> nsIterator = zone.getNS().rrs();

		while (nsIterator.hasNext()) {

			NSRecord record = (NSRecord) nsIterator.next();

			try {
				String nsIP = InetAddress.getByName(record.getTarget().toString()).getHostAddress();

				if (socket.getInetAddress().getHostAddress().equals(nsIP)) {

					axfrAllowed = true;
					break;
				}

			} catch (UnknownHostException e) {

				log.warn("Unable to resolve hostname of nameserver " + record.getTarget() + " in zone "
						+ zone.getOrigin() + " while processing AXFR request from " + socket.getRemoteSocketAddress());
			}
		}

		if (!axfrAllowed) {
			log.warn("AXFR request of zone " + zone.getOrigin() + " from " + socket.getRemoteSocketAddress()
					+ " refused!");
			return EagleDNS.errorMessage(query, Rcode.REFUSED);
		}

		Iterator<?> it = zone.AXFR();

		try {
			DataOutputStream dataOut;
			dataOut = new DataOutputStream(socket.getOutputStream());
			int id = query.getHeader().getID();
			while (it.hasNext()) {
				RRset rrset = (RRset) it.next();
				Message response = new Message(id);
				Header header = response.getHeader();
				header.setFlag(Flags.QR);
				header.setFlag(Flags.AA);
				addRRset(rrset.getName(), response, rrset, Section.ANSWER, EagleDNS.FLAG_DNSSECOK);
				if (tsig != null) {
					tsig.applyStream(response, qtsig, first);
					qtsig = response.getTSIG();
				}
				first = false;
				byte[] out = response.toWire();
				dataOut.writeShort(out.length);
				dataOut.write(out);
			}
		} catch (IOException ex) {
			log.warn("AXFR failed", ex);
		} finally {
			SocketUtils.closeSocket(socket);
		}

		return null;
	}

	private final void addSOA(Message response, Zone zone) {

		response.addRecord(zone.getSOA(), Section.AUTHORITY);
	}

	private final void addNS(Message response, Zone zone, int flags) {

		RRset nsRecords = zone.getNS();
		addRRset(nsRecords.getName(), response, nsRecords, Section.AUTHORITY, flags);
	}

	private void addGlue(Message response, Name name, int flags) {

		RRset a = findExactMatch(name, Type.A, DClass.IN, true);
		if (a == null) {
			return;
		}
		addRRset(name, response, a, Section.ADDITIONAL, flags);
	}

	private void addAdditional2(Message response, int section, int flags) {

		Record[] records = response.getSectionArray(section);
		for (Record r : records) {
			Name glueName = r.getAdditionalName();
			if (glueName != null) {
				addGlue(response, glueName, flags);
			}
		}
	}

	private RRset findExactMatch(Name name, int type, int dclass, boolean glue) {

		Zone zone = findBestZone(name);

		if (zone != null) {
			return zone.findExactMatch(name, type);
		}

		return null;
	}

	private void addRRset(Name name, Message response, RRset rrset, int section, int flags) {

		for (int s = 1; s <= section; s++) {
			if (response.findRRset(name, rrset.getType(), s)) {
				return;
			}
		}
		if ((flags & EagleDNS.FLAG_SIGONLY) == 0) {
			Iterator<?> it = rrset.rrs();
			while (it.hasNext()) {
				Record r = (Record) it.next();
				if (r.getName().isWild() && !name.isWild()) {
					r = r.withName(name);
				}
				response.addRecord(r, section);
			}
		}
		if ((flags & (EagleDNS.FLAG_SIGONLY | EagleDNS.FLAG_DNSSECOK)) != 0) {
			Iterator<?> it = rrset.sigs();
			while (it.hasNext()) {
				Record r = (Record) it.next();
				if (r.getName().isWild() && !name.isWild()) {
					r = r.withName(name);
				}
				response.addRecord(r, section);
			}
		}
	}

	private Zone findBestZone(Name name) {

		zones = new ArrayList<Zone>();
		Query query = storageFactory.createQuery(type, name.toString());
		storage.search(query, this);

		// wait a bit for the searching to end
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		if (zones.isEmpty() == true)
			return null;

		return zones.get(0);
	}

	public void newSearchResult(GUID guid, Collection results) {
		for (Iterator it = results.iterator(); it.hasNext();) {
			DataItem item = (DataItem) it.next();
			String data = (String) item.getData();
			log.info("Found item: " + data.split("\n")[0]);
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
		log.info("No results found.");
	}

	public void searchFailed(GUID guid) {
		log.info("Search failed.");
	}

	public void searchFinished(GUID guid) {
		log.info("Search finished.");
	}

	public void searchStarted(GUID guid, String message) {
		log.info("Search started.");
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
				log.error(name + " unable to access zone file " + zoneFile);
				continue;
			}
			Name origin;
			try {
				origin = Name.fromString(zoneFile.getName(), Name.root);
				Zone zone = new Zone(origin, zoneFile.getPath());
				log.debug(name + " successfully parsed zone file " + zoneFile.getName());
				zones.add(zone);
			} catch (TextParseException e) {
				log.error(name + " unable to parse zone file " + zoneFile.getName(), e);
			} catch (IOException e) {
				log.error("Unable to parse zone file " + zoneFile + " in FileZoneProvider " + name, e);
			}
		}

		Collection<DataItem> items = new ArrayList<DataItem>();
		for (Zone zone : zones) {
			DataItem item = handler.createDataItem(zone);
			items.add(item);
		}

		log.info("P-Grid resolver inserting " + items.size() + " zones into network...");
		storage.insert(items);
		((PGridP2P) p2p).setInitExchanges(true);
	}

	public void shutdown() throws Exception {
	}

	private String getPort() throws SAXException, IOException, ParserConfigurationException {
		XMLParser configFile;
		configFile = new XMLParser("conf/config.xml");
		Integer port = configFile.getIntegers("/Config/System/Port").get(0);
		return String.valueOf(port);
	}

	public void setSystemInterface(SystemInterface systemInterface) {
		this.systemInterface = systemInterface;
	}
}

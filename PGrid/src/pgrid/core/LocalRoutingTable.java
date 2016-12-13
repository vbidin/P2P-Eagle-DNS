/**
 * $Id: LocalRoutingTable.java,v 1.3 2005/11/16 14:10:46 john Exp $
 *
 * Copyright (c) 2002 The P-Grid Team,
 *                    All Rights Reserved.
 *
 * This file is part of the P-Grid package.
 * P-Grid homepage: http://www.p-grid.org/
 *
 * The P-Grid package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file LICENSE.
 * If not you can find the GPL at http://www.gnu.org/copyleft/gpl.html
 */

package pgrid.core;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import p2p.basic.GUID;
import pgrid.Constants;
import pgrid.PGridHost;
import pgrid.interfaces.basic.PGridP2P;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.logging.Level;

/**
 * This class represents the Routing Table of the P-Grid facility.
 * It includes the fidget hosts, the hosts for each level of a path, and the replicas.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class LocalRoutingTable extends XMLRoutingTable {

	/**
	 * The file to store the routing table.
	 */
	private File mFile = null;

	/**
	 * The P-Grid facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * If set, no more saves are allowed.
	 */
	private boolean mShutdownFlag = false;

	/**
	 * True iif the local peer had no ID
	 */
	private boolean mNewIdentity = false;

	/**
	 * True iif the local peer has changed its IP
	 */
	private boolean mModifiedIP = false;

	/**
	 * Create the routing table with the given file.
	 * If the given file exists, it is read in, otherwise it is created.
	 *
	 * @param routeFile the file to store the routing table.
	 * @param port      the current listening port.
	 */
	public LocalRoutingTable(String routeFile, int port) {
		super();


		mFile = new File(routeFile);
		// create all dirs if they are not already created
		createHierachy();

		// if no file exists, store the initial values
		if ((!mFile.exists()) || (mFile.length() <= 0)) {
			try {
				mLocalHost = PGridHost.getHost(InetAddress.getLocalHost(), port, true);
			} catch (UnknownHostException e) {
				Constants.LOGGER.log(Level.SEVERE, null, e);
				System.exit(-1);
			}
			mNewIdentity = true;
			// mLocalHost.setIPAddress(mLocalHost.getAddress());
			mLocalHost.setPath("");
			addFidget(mLocalHost);
			save();
			return;
		}
		// file exists => read routing table
		try {
			Constants.LOGGER.config("reading P-Grid Routing Table from '" + routeFile + "' ...");
			BufferedReader in = new BufferedReader(new FileReader(mFile));
			char[] content = new char[(int)mFile.length()];
			in.read(content, 0, content.length);
			in.close();

			SAXParserFactory spf = SAXParserFactory.newInstance();
			XMLReader parser = spf.newSAXParser().getXMLReader();
			//XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(this);
			parser.parse(new InputSource(new StringReader(new String(content))));
			
			//Try to find whether or not the local IP has changed
			InetAddress addr = mLocalHost.getIP();
			String name = null;
			if (addr != null)
				name = addr.getHostName();

			try {
				mLocalHost.setIP(InetAddress.getLocalHost());
			} catch (UnknownHostException e) {
				Constants.LOGGER.log(Level.SEVERE, null, e);
				System.exit(-1);
			}
			
			
			/*FIXME: addr.getHostAddress() returns the local IP so if we are behind a firewall
			 * or a router, the IP wont be the right one, this implies that even if the 
			 * address has not change, a query will be send to P-Grid in order to update 
			 * the ID-IP mMapping.
			 * This has no implication on the functionnality of P-Grid but will generate
			 * useless messages and will corrupt experiments if they are launched on machine
			 * that does not have an external IP as IP...
			 */ 
			
			if (port != mLocalHost.getPort() || addr == null || !addr.equals(InetAddress.getLocalHost()))
				mModifiedIP = true;

			mLocalHost.setPort(port);

			save();
		} catch (SAXException e) {
			Constants.LOGGER.log(Level.SEVERE, null, e);
			System.exit(-1);
		} catch (FileNotFoundException e) {
			Constants.LOGGER.log(Level.SEVERE, null, e);
			System.exit(-1);
		} catch (IOException e) {
			Constants.LOGGER.log(Level.SEVERE, null, e);
			System.exit(-1);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	private void createHierachy() {
		if (!mFile.getParentFile().exists() && !mFile.getParentFile().mkdirs()) {
			Constants.LOGGER.config("Cannot create subfolder: "+mFile.getParentFile());
			System.exit(-1);
		}
	}

	/**
	 * Saves the routing table to the defined file.
	 */
	synchronized public void save() {
		if (mShutdownFlag)
			return;
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(mFile));
			String content = super.toXMLString("", Constants.LINE_SEPERATOR);
			out.write(content);
			out.close();
		} catch (FileNotFoundException e) {
			Constants.LOGGER.log(Level.WARNING, null, e);
		} catch (IOException e) {
			Constants.LOGGER.log(Level.WARNING, null, e);
		}
	}

	/**
	 * Adds a new host at the delivered level.
	 *
	 * @param level the level of the path.
	 * @param host  the host.
	 */
	synchronized public void addLevel(int level, PGridHost host) {
		if (!host.equals(mLocalHost))
			super.addLevel(level, host);
	}

	/**
	 * Adds new hosts at the the delivered level.
	 *
	 * @param level the level of the path.
	 * @param hosts the hosts.
	 */
	synchronized public void addLevel(int level, Collection hosts) {
		hosts.remove(mLocalHost);
		super.addLevel(level, hosts);
	}

	/**
	 * Adds the delivered host to the replicas.
	 *
	 * @param host the new host.
	 */
	synchronized public void addReplica(PGridHost host) {
		if (!host.equals(mLocalHost))
			super.addReplica(host);
	}

	/**
	 * Adds the delivered hosts to the replicas.
	 *
	 * @param hosts the new hosts.
	 */
	synchronized public void addReplicas(Collection hosts) {
		hosts.remove(mLocalHost);
		super.addReplicas(hosts);
	}

	/**
	 * Sets the whole level with the new delivered hosts.
	 *
	 * @param level the level to set.
	 * @param hosts the new hosts.
	 * @throws IllegalArgumentException if an illegal level is given.
	 */
	synchronized public void setLevel(int level, Collection hosts) throws IllegalArgumentException {
		hosts.remove(mLocalHost);
		super.setLevel(level, hosts);
	}

	/**
	 * Sets the whole level with the new delivered hosts.
	 *
	 * @param level the level to set.
	 * @param hosts the new hosts.
	 */
	synchronized public void setLevel(int level, PGridHost[] hosts) {
		setLevels(level);
		if (level >= mLevels.size()) {
			Constants.LOGGER.log(Level.WARNING, "Illegal Argument in LocalRoutingTable.setLevels() for level " + level, new Throwable());
			return;
		}
		((Collection)mLevels.get(level)).clear();
		for (int i = 0; i < hosts.length; i++)
			addLevel(level, hosts[i]);
	}

	/**
	 * Sets the replicas with the given hosts.
	 *
	 * @param hosts the new hosts.
	 */
	synchronized public void setReplicas(Collection hosts) {
		hosts.remove(mLocalHost);
		super.setReplicas(hosts);
	}

	/**
	 * Sets the replicas with the given hosts.
	 *
	 * @param hosts the new hosts.
	 */
	synchronized public void setReplicas(PGridHost[] hosts) {
		mReplicas.clear();
		for (int i = 0; i < hosts.length; i++)
			addReplica(hosts[i]);
	}

	/**
	 * Performs a union of the delivered and this Routing Table.
	 *
	 * @param routingTable a Routing Table.
	 */
	synchronized public void union(RoutingTable routingTable) {
		unionFidgets(routingTable);
	}

	/**
	 * Performs a union of the hosts at the delivered level of the delivered and this Routing Table.
	 *
	 * @param level        the level.
	 * @param routingTable a Routing Table.
	 */
	synchronized public void unionLevel(int level, RoutingTable routingTable) {
		if (routingTable != null) {
			setLevel(level, union(getLevelVector(level), routingTable.getLevelVector(level)));
		}
	}

	/**
	 * If P-Grid is shutdown the routing table is saved to a file.
	 */
	public synchronized void shutdown() {
		save();
		mShutdownFlag = true;
	}

	/**
	 * True iff the local peer has not inserted its information (id, ip, E, ts, D(id, ip, E, ts))
	 * into P-Grid.
	 *
	 * @return true if local peer is unknown for P-Grid
	 */
	public boolean isNewIdentity() {
		return mNewIdentity;
	}

	/**
	 * True iff the new IP or port has changed since the last time this peer was
	 * run.
	 *
	 * @return if this peer has a new IP or port
	 */
	public boolean isModifiedIp() {
		return mModifiedIP;
	}

	/**
	 * Return a reference to the host with a given guid
	 *
	 * @param guid the guid
	 * @return the host
	 */
	public PGridHost getHost(GUID guid) {
		return (PGridHost)mHosts.get(guid.toString());
	}

}

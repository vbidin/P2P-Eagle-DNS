/**
 * $Id: PGridHost.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

package pgrid;

import p2p.basic.KeyRange;
import p2p.basic.Peer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

/**
 * This class represents a host of the P-Grid Network.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class PGridHost extends Host implements Peer, Comparable {

	/**
	 * The seperator between GUID and Internet address.
	 */
	protected static final String EXCL_MARK = "!";

	/**
	 * Stale peer
	 */
	public static final int HOST_OK = 1;

	/**
	 * Stale peer
	 */
	public static final int HOST_STALE = 2;

	/**
	 * Offline peer
	 */
	public static final int HOST_OFFLINE = 3;

	/**
	 * This host is getting updated
	 */
	public static final int HOST_UPDATING = 4;

	/**
	 * The minimum exchange delay.
	 */
	private static final int MIN_EXCHANGE_DELAY = 1000 * 60; // 1 min.

	/**
	 * The time a PGrid Exchange can be processed.
	 */
	protected long mExchangeTime = 0;

	/**
	 * The global unique id.
	 */
	protected GUID mGUID = null;

	/**
	 * The flag if the global unique id is temporary.
	 */
	protected boolean mGUIDTempFlag = false;

	/**
	 * The list of already created Hosts.
	 */
	protected static Hashtable mHosts = new Hashtable();

	/**
	 * The path of the host.
	 */
	protected String mPath = "";

	/**
	 * The last time the path of the host was set.
	 */
	protected long mPathTimestamp = 0;

	/**
	 * The public key of this host.
	 */
	private String mPublicKey = "";

	/**
	 * The speed of the connection.
	 */
	protected int mSpeed = 0;

	/**
	 * State of this host
	 */
	protected int mState = HOST_OK;

	/**
	 * Mapping attemps for this host
	 */
	protected int mMappingAttemps = 0;

	/**
	 * The number of successive offline period
	 */
	protected int mOffline = 0;

	/**
	 * Creates a new host.
	 */
	protected PGridHost() {
	}

	/**
	 * Creates a new host with an address string.
	 *
	 * @param addr the address of the host.
	 * @param port the port of the host.
	 */
	protected PGridHost(String addr, int port) {
		super(addr, port);
	}

	/**
	 * Creates a new host with an GUID, address, and ip string.
	 *
	 * @param guid the guid string of the host.
	 * @param addr the address of the host.
	 * @param port the port as string of the host.
	 */
	protected PGridHost(String guid, String addr, String port) {
		super(addr, port);
		mGUID = GUID.getGUID(guid);
	}

	/**
	 * Creates a new host.
	 *
	 * @param netAddr the internet address.
	 * @param port    the port.
	 */
	protected PGridHost(InetAddress netAddr, int port) {
		super(netAddr, port);
		mGUID = GUID.getGUID();
	}

	/**
	 * Creates a new host.
	 *
	 * @param guid    the GUID.
	 * @param netAddr the internet address.
	 * @param port    the port.
	 */
	protected PGridHost(GUID guid, InetAddress netAddr, int port) {
		super(netAddr, port);
		mGUID = guid;
	}

	/**
	 * Returns a host for the given GUID.
	 *
	 * @param guid the GUID.
	 * @return the host.
	 */
	public static PGridHost getHost(GUID guid) {
		if (guid == null)
			throw new NullPointerException("GUID is null");
		return (PGridHost)mHosts.get(guid);
	}

	/**
	 * Returns a host for the given values.
	 *
	 * @param guid the GUID.
	 * @param addr the internet address.
	 * @param port the port.
	 * @return the created host.
	 */
	public static PGridHost getHost(String guid, String addr, String port) {
		GUID g = GUID.getGUID(guid);
		PGridHost host = (PGridHost)mHosts.get(GUID.getGUID(guid));
		if (host == null) {
			host = new PGridHost(guid, addr, port);
			mHosts.put(g, host);
		}
		return host;
	}

	/**
	 * Returns a host for the given values.
	 *
	 * @param netAddr the internet address.
	 * @param port    the port.
	 * @return the created host.
	 */
	public static PGridHost getHost(InetAddress netAddr, int port) {
		PGridHost host = new PGridHost(netAddr, port);
		if (host.isValid())
			mHosts.put(host.getGUID(), host);
		return host;
	}

	/**
	 * Returns a host for the given values.
	 *
	 * @param netAddr the internet address.
	 * @param port    the port.
	 * @param cache   if this host should be added to the cache.
	 * @return the created host.
	 */
	public static PGridHost getHost(InetAddress netAddr, int port, boolean cache) {
		PGridHost host = new PGridHost(netAddr, port);
		if (cache)
			mHosts.put(host.getGUID(), host);
		else
			host.setGUIDisTmp();
		return host;
	}

	/**
	 * Returns a host for the given values.
	 *
	 * @param guid    the GUID.
	 * @param netAddr the internet address.
	 * @param port    the port.
	 * @return the created host.
	 */
	public static PGridHost getHost(GUID guid, InetAddress netAddr, int port) {
		PGridHost host = (PGridHost)mHosts.get(guid);
		if (host == null) {
			host = new PGridHost(guid, netAddr, port);
			mHosts.put(guid, host);
		}
		return host;
	}

	/**
	 * Returns the amount of known hosts.
	 *
	 * @return the amount of known hosts.
	 */
	public static int getHostsCount() {
		return mHosts.size();
	}

	/**
	 * Tests if the host can be contacted for an Exchange.
	 *
	 * @return <code>true</code> if allowed, <code>false</code> otherwise.
	 */
	public boolean isExchangeTime() {
		return (mExchangeTime < System.currentTimeMillis());
	}

	/**
	 * Sets the next possible exchange time when a host was invited for an exchange.
	 */
	public void exchanged() {
		mExchangeTime = System.currentTimeMillis() + MIN_EXCHANGE_DELAY;
	}

	/**
	 * Sets the next possible exchange time when a host was invited for an exchange.
	 */
	public void invited() {
		mExchangeTime = System.currentTimeMillis() + MIN_EXCHANGE_DELAY / 2;
	}

	/**
	 * Forces to resolve the network address of this host.
	 *
	 * @throws UnknownHostException if the host could not be found.
	 */
	public void resolve() throws UnknownHostException {
		super.resolve(mAddrString);
	}

	/**
	 * Resolves the given string to set the host values.
	 *
	 * @param guid    the GUID.
	 * @param address the host address.
	 * @param port    the host port.
	 * @throws UnknownHostException if the given address is unknown.
	 */
	protected void resolve(String guid, String address, String port) throws UnknownHostException {
		mGUID = GUID.getGUID(guid);
		super.resolve(address, port);
	}

	/**
	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
	 * as this object is less than, equal to, or greater than the specified object.
	 *
	 * @param obj the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
	 *         specified object.
	 */
	public int compareTo(Object obj) {
		return mGUID.compareTo(((PGridHost)obj).getGUID());
	}

	/**
	 * Tests if the delivered host equals the host.
	 *
	 * @param host the host to compare.
	 * @return <code>true</code> if the hosts are equal, else <code>false</code>.
	 */
	public boolean equals(PGridHost host) {
		if ((mGUID != null) && (host.getGUID() != null))
			return mGUID.equals(host.getGUID());
		return (super.equals(host));
	}

	/**
	 * Test if this host is a valid host.
	 *
	 * @return <code>true</code> if valid, <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return (!(mGUID == null) && super.isValid());
	}

	/**
	 * Returns a string represantation of this host.
	 *
	 * @return a string.
	 */
	public String toHostString() {
		return super.toString();
	}

	/**
	 * Returns a string represantation of this host.
	 *
	 * @return a string.
	 */
	public String toString() {
		return (mGUID != null ? mGUID.toString() + EXCL_MARK : "") + super.getAddressString() + COLON + mPort;
	}

	/**
	 * Returns the GUID of this host.
	 *
	 * @return the GUID of this host.
	 */
	public p2p.basic.GUID getGUID() {
		return mGUID;
	}

	/**
	 * Sets the GUID of this host.
	 *
	 * @param guid the GUID of this host.
	 */
	public void setGUID(GUID guid) {
		mGUID = guid;
	}

	/**
	 * Checks if the used host guid as temporary.
	 */
	public boolean isGUIDTmp() {
		return mGUIDTempFlag;
	}

	/**
	 * Sets the used host guid as temporary.
	 */
	private void setGUIDisTmp() {
		mGUIDTempFlag = true;
	}

	/**
	 * Get the range for which the peer is responsible.
	 *
	 * Due to the structure of P-Grid, giving a range does not make sens.
	 * To be compliant with the basic Interface, a key range is constructed
	 * with a lower bound and higher bound that are equal.
	 *
	 * @return the KeyRange
	 */
	public KeyRange getKeyRange() {
		return new PGridKeyRange(new PGridKey(mPath), new PGridKey(mPath));
	}

	/**
	 * Returns the path of the host.
	 *
	 * @return the path of the host.
	 */
	public String getPath() {
		return mPath;
	}

	/**
	 * Sets the path of the host.
	 *
	 * @param path the path of the host.
	 */
	public void setPath(String path) {
		mPath = path;
		refreshPathTimestamp();
	}

	/**
	 * Sets the path of the host, if the timestamp is later than the current.
	 *
	 * @param path      the path of the host.
	 * @param timestamp the timestamp for the path.
	 */
	public void setPath(String path, long timestamp) {
		if (timestamp >= mPathTimestamp) {
			mPath = path;
			mPathTimestamp = timestamp;
		}
	}

	/**
	 * Returns the last time the path of the host was set.
	 *
	 * @return the time.
	 */
	public long getPathTimestamp() {
		return mPathTimestamp;
	}

	/**
	 * Refreshes the timestamp for the path.
	 */
	public void refreshPathTimestamp() {
		mPathTimestamp = System.currentTimeMillis();
	}

	/**
	 * Returns the corresponding XMLPGridHost object for the Peer object.
	 * @param peer the peer object.
	 * @return the XMLPGridHost object.
	 */
	public static PGridHost toPGridHost(Peer peer) {
		GUID guid = GUID.getGUID(peer.getGUID().toString());
		return getHost(guid, peer.getIP(), peer.getPort());
	}

	/**
	 * Returns the public key of this host
	 *
	 * @return the public key of this host
	 */
	public String getPublicKey() {
		return mPublicKey;
	}

	/**
	 * Set the public key of this host
	 */
	public void setPublicKey(String key) {
		mPublicKey = key;
	}

	/**
	 * Returns the connection speed of the host.
	 *
	 * @return the connection speed of the host.
	 */
	public int getSpeed() {
		return mSpeed;
	}

	/**
	 * Sets the connection speed of the host.
	 *
	 * @param speed the connection speed of the host.
	 */
	public void setSpeed(int speed) {
		mSpeed = speed;
	}

	/**
	 * Set the state of the host.
	 */
	public void setState(int state) {
		mState = state;
	}

	/**
	 * Get the state of the host.
	 *
	 * @return the state.
	 */
	public int getState() {
		return mState;
	}

	/**
	 * Increment the mapping attemps
	 */
	public void incMappingAttemps() {
		++mMappingAttemps;
	}

	/**
	 * Increment the offline
	 */
	public void incOfflineTime() {
		++mOffline;
	}

	/**
	 * Reset the mapping attemps
	 */
	public void resetMappingAttemps() {
		mMappingAttemps = 0;
	}

	/**
	 * Reset the number of time this peer was offline
	 */
	public void resetOfflineTime() {
		mOffline = 0;
	}

	/**
	 * Returns the mapping attemps
	 *
	 * @return the mapping attemps
	 */
	public int getMappingAttemps() {
		return mMappingAttemps;
	}

	/**
	 * Returns the number of time this host was successively set offline
	 *
	 * @return the mapping attemps
	 */
	public int getOfflineTime() {
		return mOffline;
	}

}
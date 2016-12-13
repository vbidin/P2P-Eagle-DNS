/**
 * $Id: Host.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

import pgrid.interfaces.basic.PGridP2P;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class represents a host of the P-Grid Network.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public abstract class Host {

	/**
	 * The seperator between Internet address and port.
	 */
	protected static final String COLON = ":";

	/**
	 * The string of the whole internet address ({GUID}ip:port).
	 */
	protected String mAddrString = null;

	/**
	 * The resolved host address.
	 */
	protected String mAddrStringResolved = null;

	/**
	 * The internet address.
	 */
	protected InetAddress mNetAddr = null;

	/**
	 * The PGrid facility.
	 */
	private PGridP2P mPGrid = PGridP2P.sharedInstance();

	/**
	 * The listening port of the host.
	 */
	protected int mPort = -1;

	/**
	 * Creates a new dummy host.
	 */
	protected Host() {
	}

	/**
	 * Creates a new host with an address, and ip string.
	 *
	 * @param addr the address of the host.
	 * @param port the port as string of the host.
	 */
	public Host(String addr, String port) {
		mAddrString = addr;
		mAddrStringResolved = addr;
		try {
			mPort = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			mPort = Constants.DEFAULT_PORT;
		}
	}

	/**
	 * Creates a new host with an address, and ip string.
	 *
	 * @param addr the address of the host.
	 * @param port the port as string of the host.
	 */
	public Host(String addr, int port) {
		mAddrString = addr;
		mAddrStringResolved = addr;
		mPort = port;
	}

	/**
	 * Creates a new host.
	 *
	 * @param netAddr the internet address.
	 * @param port    the port.
	 */
	public Host(InetAddress netAddr, int port) {
		mNetAddr = netAddr;
		mPort = port;
		mAddrString = netAddr.getHostAddress();
		mAddrStringResolved = netAddr.getCanonicalHostName();
	}

	/**
	 * Tests if the delivered host equals the host.
	 *
	 * @param host the host to compare.
	 * @return <code>true</code> if the hosts are equal, else <code>false</code>.
	 */
	public boolean equals(Host host) {
		if (host == null)
			return false;

		// if both network addresses are known => compare them
		if ((mNetAddr != null) && (host.getIP() != null)) {
			byte[] addr1 = mNetAddr.getAddress();
			byte[] addr2 = host.getIP().getAddress();
			if (addr1.length != addr2.length)
				return false;
			for (int i = 0; i < addr1.length; i++)
				if (addr1[i] != addr2[i])
					return false;
			if (mPort != host.getPort())
				return false;
			return true;
		} else {
			if (toString().equals(host.toString())) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Test if this host is a valid host.
	 *
	 * @return <code>true</code> if valid, <code>false</code> otherwise.
	 */
	public boolean isValid() {
		if (mPort <= 0)
			return false;
		if (mNetAddr != null) {
			return true;
		} else {
			try {
				resolve(mAddrString);
			} catch (UnknownHostException e) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Resolves the given string to set the host values.
	 *
	 * @param address the host address.
	 * @throws UnknownHostException if the given address is unknown.
	 */
	protected void resolve(String address) throws UnknownHostException {
		mNetAddr = InetAddress.getByName(address);
	}

	/**
	 * Resolves the given string to set the host values.
	 *
	 * @param address the host address.
	 * @param port    the host port.
	 * @throws UnknownHostException if the given address is unknown.
	 */
	protected void resolve(String address, String port) throws UnknownHostException {
		resolve(address);
		try {
			mPort = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			mPort = Constants.DEFAULT_PORT;
		}
	}

	/**
	 * Returns a string represantation of this host.
	 *
	 * @return a string.
	 */
	public String toString() {
		return getAddressString() + COLON + mPort;
	}

	/**
	 * Returns the Internet address.
	 *
	 * @return the Internet address.
	 */
	public InetAddress getIP() {
		return mNetAddr;
	}

	/**
	 * Sets the internet address of the host.
	 *
	 * @param netAddr the internet address.
	 */
	public void setIP(InetAddress netAddr) {
		mNetAddr = netAddr;
	}

	/**
	 * Returns the Internet address of the host.
	 *
	 * @return the Internet address of the host.
	 */
	public int getPort() {
		return mPort;
	}

	/**
	 * Sets the listening port.
	 *
	 * @param port the port.
	 */
	public void setPort(int port) {
		mPort = port;
	}

	/**
	 * Returns the Internet name.
	 *
	 * @return the Internet name.
	 */
	public String getAddressString() {
		if (mPGrid.propertyBoolean(Properties.RESOLVE_IP)) {
			if (mAddrStringResolved == null) {
				if (mNetAddr == null) {
					mAddrStringResolved = "0.0.0.0";
				} else {
					mAddrStringResolved = mNetAddr.getCanonicalHostName();
				}
			}
			return mAddrStringResolved;
		} else {
			if (mAddrString == null) {
				if (mNetAddr == null)
					mAddrString = "0.0.0.0";
				else
					mAddrString = mNetAddr.getHostAddress();
			}
			return mAddrString;
		}
	}

}
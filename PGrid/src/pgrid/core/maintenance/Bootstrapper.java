/**
 * $Id: Bootstrapper.java,v 1.7 2005/12/16 09:08:06 john Exp $
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

package pgrid.core.maintenance;

import pgrid.Constants;
import pgrid.PGridHost;
import pgrid.Properties;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.network.MessageManager;
import pgrid.network.protocol.BootstrapMessage;
import pgrid.util.Tokenizer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * This class bootstraps with one of the know bootstrap hosts or a designated bootstrap host.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class Bootstrapper {

	/**
	 * The average. exchange delay.
	 */
	private static final int AVG_EXCHANGE_DELAY = 1000 * 60; // 1 min.

	/**
	 * The maximum exchange delay.
	 */
	private static final int MAX_EXCHANGE_DELAY = 1000 * 60 * 10; // 10 min.

	/**
	 * The minimum exchange delay.
	 */
	private static final int MIN_EXCHANGE_DELAY = 1000 * 10; // 10 sec.

	/**
	 * The addBootstrapHost message.
	 */
	private BootstrapMessage mBootstrapMsg = null;

	/**
	 * True if the local host is a bootstrap host
	 */
	private boolean mIsBootstrapHost = false;

	/**
	 * The addBootstrapHost hosts.
	 */
	private Vector mHosts = new Vector();

	/**
	 * The new addBootstrapHost hosts.
	 */
	private Vector mHostsNew = new Vector();

	/**
	 * The Message Manager.
	 */
	private MessageManager mMsgMgr = MessageManager.sharedInstance();

	/**
	 * The next time for an exchange.
	 */
	private long mNextExchangeTime = 0;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The random number generator.
	 */
	private SecureRandom mRandomizer = new SecureRandom();

	/**
	 * The already used hosts.
	 */
	private Vector mUsedHosts = new Vector();


	private boolean isCondition = true;

	/**
	 * Creates a new router.
	 *
	 * @param p2p the P2P facility.
	 */
	Bootstrapper(PGridP2P p2p) {
		mPGridP2P = p2p;
		mBootstrapMsg = new BootstrapMessage(mPGridP2P.getLocalHost());

		// create bootstrap hosts from the property file
		String[] hostStr = Tokenizer.tokenize(mPGridP2P.propertyString(pgrid.Properties.BOOTSTRAP_HOSTS), ";");
		for (int i = 0; i < hostStr.length; i++) {
			String[] parts = Tokenizer.tokenize(hostStr[i], ":");
			PGridHost host = null;
			int port = Constants.DEFAULT_PORT;
			if (parts.length > 1) {
				try {
					port = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					port = Constants.DEFAULT_PORT;
				}
			}
			// get Internet address
			try {
				InetAddress addr = InetAddress.getByName(parts[0]);
				host = PGridHost.getHost(addr, port, false);
				host.resolve();
			} catch (UnknownHostException e) {
				continue;
			}
			// check if host is the local host
			if (mPGridP2P.isLocalHost(host)) {
				mIsBootstrapHost = true;
				continue;
			}
			// add the host if it's a valid host
			if ((host.getAddressString() != null) && (host.getPort() > 0)) {
				Constants.LOGGER.finer("add host " + host.toHostString() + " as bootstrap host.");
				mHosts.add(host);
				// addBootstrapHost(host); INFO this caused to kill the program because hosts are not in the bootstrap list before bootstrap() is called 
			}
		}
	}

	/**
	 * Adds the given host to the list of addBootstrapHost hosts.
	 *
	 * @param host the new addBootstrapHost host.
	 */
	void addBootstrapHost(PGridHost host) {
		if (mPGridP2P.isLocalHost(host)) {
			mIsBootstrapHost = true;
		}
		mHostsNew.add(host);
	}

	/**
	 * Tries to bootstrap with the known bootstrap hosts.
	 * @throws Exception any exception.
	 */
	void bootstrap() throws Exception {
		// try to bootstrapp with new hosts
		synchronized(mHostsNew) {
			for (Iterator it = mHostsNew.iterator(); it.hasNext();) {
				PGridHost host = (PGridHost)it.next();
				mHosts.add(host);
				if (sendBootstrapMsg(host)) {
					try {
						Thread.sleep(1000 * 10);
					} catch (InterruptedException e) {
					}
					break;
				}
			}
			// remove all hosts which are already in the addBootstrapHost list
			mHostsNew.removeAll(mHosts);
		}

		// return if no further bootstrapping is required
		if (mPGridP2P.getMaintenanceManager().getPhase() != MaintenanceManager.PHASE_BOOTSTRAP)
			return;

		// try to addBootstrapHost using the addBootstrapHost list
		synchronized(mHosts) {
			List list = new Vector();
			list.addAll(mHosts);
			Collections.shuffle(list);
			for (Iterator it = list.iterator(); it.hasNext();) {
				if (sendBootstrapMsg((PGridHost)it.next()))
					break;
			}
		}
	}

	/**
	 * Exchanges the fidget list with one of the hosts of the fidget list.
	 */
	public void fidgetExchange() throws Exception {
		// return if it's too early for the next exchange
		if (System.currentTimeMillis() < mNextExchangeTime)
			return;

		synchronized(mPGridP2P.getRoutingTable().getFidgetVector()) {
			List list = new Vector();
			list.addAll(mPGridP2P.getRoutingTable().getFidgetVector());
			Collections.shuffle(list);
			for (Iterator it = list.iterator(); it.hasNext();) {
				PGridHost host = (PGridHost)it.next();
				if (host.equals(mPGridP2P.getLocalHost()))
					continue;
				// if host was already used for an exchange => try next one
				if (mUsedHosts.contains(host))
					continue;
				if (sendFidgetMsg(host)) {
					mUsedHosts.add(host);
					setNextExchangeTime();
					break;
				}
			}
		}
	}

	/**
	 * Returns the used bootstrap hosts.
	 * @return the bootstrap hosts.
	 */
	public Collection getHosts() {
		return mHosts;
	}

	protected boolean isCondition() {
		return isCondition;
	}

	/**
	 * Sends the addBootstrapHost message to the given hosts.
	 * @param host the host used for bootstrapping.
	 * @return <tt>true</tt> if bootstrapping was successful, <tt>false</tt> otherwise.
	 */
	private boolean sendBootstrapMsg(PGridHost host) {
		if (mMsgMgr.sendMessage(host, mBootstrapMsg)) {
			// message sent successfully => wait for reply
			Constants.LOGGER.finer("Bootstrapping with host " + host.toString() + " ... waiting for reply.");
			// break if no further bootstrapping is required
			return true;
		} else {
			Constants.LOGGER.finer("Bootstrapping with host " + host.toString() + " ... failed.");
		}
		return false;
	}

	/**
	 * Sends the fidget exchange message to the given host.
	 * @param host the host.
	 * @return <tt>true</tt> if sending was successful, <tt>false</tt> otherwise.
	 */
	private boolean sendFidgetMsg(PGridHost host) {
		BootstrapMessage msg = new BootstrapMessage(mPGridP2P.getLocalHost(), mPGridP2P.getRoutingTable());
		if (mMsgMgr.sendMessage(host, msg)) {
			// message sent successfully => wait for reply
			Constants.LOGGER.finer("Fidget list exchange with host " + host.toString() + " ... waiting for reply.");
			return true;
		} else {
			Constants.LOGGER.finer("Fidget list exchange with host " + host.toString() + " ... failed.");
		}
		return false;
	}

	/**
	 * Sets the next time for an exchange.
	 */
	private void setNextExchangeTime() {
		long currTime = System.currentTimeMillis();
		// check in which phase we are
		long time;
		long timeLeft = mPGridP2P.getMaintenanceManager().getReplicationStartTime() - currTime;
		int fidgets = mPGridP2P.getRoutingTable().getFidgetVector().size();
		int fidgetsMissing = mPGridP2P.propertyInteger(Properties.MAX_FIDGETS) - fidgets;
		if (fidgetsMissing <= 0) {
			time = currTime + MAX_EXCHANGE_DELAY;
		} else {
			time = AVG_EXCHANGE_DELAY * fidgets;
			if (time > timeLeft)
				time = AVG_EXCHANGE_DELAY;
			if (time > timeLeft)
				time = MIN_EXCHANGE_DELAY;
		}
		mNextExchangeTime = currTime + time + mRandomizer.nextInt(1000 * 10);
	}

	/**
	 * Returns true iff the local host is a bootstrap host
	 * @return true if the localhost is a bootstrap host
	 */
	public boolean isBootstrapHost() {
		return mIsBootstrapHost;
	}

	protected void setReady(boolean cond) {
		isCondition = cond;
	}

}
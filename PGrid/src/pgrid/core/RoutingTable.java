/**
 * $Id: RoutingTable.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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

import org.xml.sax.helpers.DefaultHandler;
import pgrid.Constants;
import pgrid.PGridHost;
import pgrid.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;

/**
 * This class represents the Routing Table of the P-Grid facility.
 * It includes the fidget hosts, the hosts for each level of a path, and the replicas.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class RoutingTable extends DefaultHandler {

	/**
	 * Hashtable of all hosts in the routing table by GUID
	 */
	protected Hashtable mHosts = new Hashtable();

	/**
	 * The list of fidget hosts.
	 */
	protected Collection mFidgets = Collections.synchronizedCollection(new TreeSet());

	/**
	 * The list of references.
	 */
	protected Vector mLevels = new Vector();

	/**
	 * The local host.
	 */
	protected PGridHost mLocalHost = null;

	/**
	 * The list of replica hosts.
	 */
	protected Collection mReplicas = Collections.synchronizedCollection(new TreeSet());

	/**
	 * Create a new empty routing table.
	 */
	public RoutingTable() {
		// do nothing
	}

	/**
	 * Returns a random subset of the given collection of the given size.
	 *
	 * @param col   the collection.
	 * @param count the size of the random subset.
	 * @return the random subset.
	 */
	private Collection randomSelect(Collection col, int count) {
		if (col == null)
			throw new NullPointerException();
		if (col.size() <= count)
			return col;
		Vector list = new Vector(col);
		Collections.shuffle(list);
		return list.subList(0, count);
	}

	/**
	 * Refreshes the routing table by building the union with the given routing table.
	 *
	 * @param routingTable the routing table used to refresh this routing table.
	 * @param commonLen    the common length of the peer's path.
	 * @param lLen         the local path length.
	 * @param rLen         the remote path length.
	 * @param maxFidgets   the amount of fidgets to manage.
	 * @param maxRef       the amount of references to manage per level.
	 */
	synchronized public void refresh(RoutingTable routingTable, int commonLen, int lLen, int rLen, int maxFidgets, int maxRef) {
		if (routingTable == null)
			throw new NullPointerException();
		// 1:
		Collection commonFidgets = union(getFidgetVector(), routingTable.getFidgetVector());
		// 2:
		setFidgets(randomSelect(commonFidgets, maxFidgets));
		// 4:
		if (commonLen > 0) {
			// 5:
			for (int i = 0; i < commonLen; i++) {
				try {
					// 6:
					Collection commonRef = union(getLevelVector(i), routingTable.getLevelVector(i));
					// 7: At the most REF_MAX routing references are randomly and independently chosen from commonRef in order to
					//    ensure that the network is uniformly connected, and routing references are random.
					setLevel(i, randomSelect(commonRef, maxRef));
				} catch (IllegalArgumentException e) {
					// do nothing
				}
			}
			// 10:
			if ((lLen > commonLen) && (rLen > commonLen)) {
				// 11: Peers add mutual entries in their routing tables for level commonLen + 1
				addLevel(commonLen, routingTable.getLocalHost());
			}
		}
		//NEW: check all levels and replicas if their path has changed
		for (int i = 0; i < getLevelCount(); i++) {
			PGridHost[] refs = getLevel(i);
			for (int j = 0; j < refs.length; j++) {
				if (refs[j].getPathTimestamp() == 0)
					continue;
				String commonPrefix = Utils.commonPrefix(mLocalHost.getPath(), refs[j].getPath());
				commonLen = commonPrefix.length();
				// if the host has another path than it should have
				if ((commonLen != i) || (refs[j].getPath().length() == i)) {
					lLen = mLocalHost.getPath().length() - commonLen;
					rLen = refs[j].getPath().length() - commonLen;
					// peers have incompatible paths
					if ((lLen > 0) && (rLen > 0)) {
						addLevel(commonLen, refs[j]);
					} else if ((lLen == 0) && (rLen == 0)) {
						addReplica(refs[j]);
					} else {
						removeLevel(refs[j]);
					}
				}
			}
		}
		PGridHost[] refs = getReplicas();
		for (int i = 0; i < refs.length; i++) {
			if (refs[i].getPathTimestamp() == 0)
				continue;
			if (!mLocalHost.getPath().equals(refs[i].getPath())) {
				String commonPrefix = Utils.commonPrefix(mLocalHost.getPath(), refs[i].getPath());
				commonLen = commonPrefix.length();
				lLen = mLocalHost.getPath().length() - commonLen;
				rLen = refs[i].getPath().length() - commonLen;
				// peers have incompatible paths
				if ((lLen > 0) && (rLen > 0)) {
					addLevel(commonLen, refs[i]);
				} else {
					removeReplica(refs[i]);
				}
			}
		}
	}

	/**
	 * Adds the delivered host to the list of Fidget hosts.
	 *
	 * @param host the host.
	 */
	synchronized public void addFidget(PGridHost host) {
		if (host == null)
			throw new NullPointerException();
		mHosts.put(host.getGUID().toString(), host);
		synchronized (mFidgets) {
			if (!mFidgets.contains(host))
				mFidgets.add(host);
		}
	}

	/**
	 * Adds a new host at the delivered level.
	 *
	 * @param level the level of the path.
	 * @param host  the host.
	 */
	synchronized public void addLevel(int level, PGridHost host) {
		if (host == null)
			throw new NullPointerException();
		remove(host);
		mHosts.put(host.getGUID().toString(), host);
		setLevels(level);
		Collection lev = (Collection)mLevels.get(level);
		lev.add(host);
	}

	/**
	 * Adds new hosts at the the delivered level.
	 *
	 * @param level the level of the path.
	 * @param hosts the hosts.
	 */
	synchronized public void addLevel(int level, Collection hosts) {
		if (hosts == null)
			throw new NullPointerException();
		setLevels(level);
		setLevel(level, union(getLevelVector(level), hosts));
	}

	/**
	 * Adds the delivered host to the replicas.
	 *
	 * @param host the new host.
	 */
	synchronized public void addReplica(PGridHost host) {
		if (host == null)
			throw new NullPointerException();
		removeLevel(host);
		mHosts.put(host.getGUID().toString(), host);
		mReplicas.add(host);
	}

	/**
	 * Adds the delivered hosts to the replicas.
	 *
	 * @param hosts the new hosts.
	 */
	synchronized public void addReplicas(Collection hosts) {
		if (hosts == null)
			throw new NullPointerException();
		setReplicas(union(getReplicaVector(), hosts));
	}

	/**
	 * Removes all known fidgets, level hosts and replicas.
	 */
	synchronized public void clear() {
		mFidgets.clear();
		mLevels.clear();
		mReplicas.clear();
	}

	/**
	 * Removes all known replicas.
	 */
	synchronized public void clearReplicas() {
		mReplicas.clear();
	}

	/**
	 * Returns an array of all fidget hosts.
	 *
	 * @return an array of all fidget hosts.
	 */
	public PGridHost[] getFidgets() {
		if (mFidgets.size() > 0) {
			PGridHost[] hosts = new PGridHost[mFidgets.size()];
			System.arraycopy(mFidgets.toArray(), 0, hosts, 0, hosts.length);
			return hosts;
		} else {
			return new PGridHost[0];
		}
	}

	/**
	 * Returns a list of all fidget hosts.
	 *
	 * @return a list of all fidget hosts.
	 */
	public Collection getFidgetVector() {
		return mFidgets;
	}

	/**
	 * Returns an array of all references.
	 *
	 * @return an array of all references.
	 */
	public PGridHost[][] getLevels() {
		if (mLevels.size() == 0)
			return new PGridHost[0][0];
		PGridHost[][] refs = new PGridHost[mLevels.size()][];
		for (int i = 0; i < refs.length; i++)
			refs[i] = getLevel(i);
		return refs;
	}

	/**
	 * Returns the amount of levels of this Routing Table.
	 *
	 * @return the amount of levels.
	 */
	public int getLevelCount() {
		return mLevels.size();
	}

	/**
	 * Returns an array of the delivered level.
	 *
	 * @param level the level to return.
	 * @return an array of the level.
	 */
	public PGridHost[] getLevel(int level) {
		synchronized (mLevels) {
			if (level >= mLevels.size()) {
				Constants.LOGGER.log(Level.WARNING, "Illegal Argument in RoutingTable.getLevel() for level " + level, new Throwable());
				return new PGridHost[0];
			}
			if (((Collection)mLevels.get(level)).size() > 0) {
				PGridHost[] hosts = new PGridHost[((Collection)mLevels.get(level)).size()];

				System.arraycopy(((Collection)mLevels.get(level)).toArray(), 0, hosts, 0, hosts.length);

				return hosts;
			} else {
				return new PGridHost[0];
			}
		}
	}

	/**
	 * Returns a list of the delivered level.
	 *
	 * @param level the level to return.
	 * @return a list of the level.
	 * @throws IllegalArgumentException if an illegal level is given.
	 */
	public Collection getLevelVector(int level) {
		if (level >= mLevels.size()) {
			Constants.LOGGER.log(Level.WARNING, "Illegal Argument in RoutingTable.getLevelVector() for level " + level, new Throwable());
			return new Vector();
		}
		return (Collection)mLevels.get(level);
	}

	/**
	 * Returns the local host.
	 *
	 * @return the local host.
	 */
	public PGridHost getLocalHost() {
		return mLocalHost;
	}

	/**
	 * Sets the local host.
	 *
	 * @param host the local host.
	 */
	synchronized public void setLocalHost(PGridHost host) {
		if (host == null)
			throw new NullPointerException();
		mLocalHost = host;
		if (host.getGUID() != null)
			mHosts.put(host.getGUID().toString(), host);
	}

	/**
	 * Returns an array of all replicas.
	 *
	 * @return an array of all replicas.
	 */
	public PGridHost[] getReplicas() {
		if (mReplicas.size() > 0) {
			PGridHost[] hosts = new PGridHost[mReplicas.size()];
			System.arraycopy(mReplicas.toArray(), 0, hosts, 0, hosts.length);
			return hosts;
		} else {
			return new PGridHost[0];
		}
	}

	/**
	 * Returns a list of all known references.
	 *
	 * @return a list of all known references.
	 */
	public List getAllReferences() {
		int size = mLevels.size();
		ArrayList array = new ArrayList();
		Collection tmp = null;

		for (int i = 0; i < size; i++) {
			tmp = (Collection)mLevels.get(i);

			if (tmp != null)
				array.addAll(tmp);
		}
		return array;
	}

	/**
	 * Returns a list of all replicas.
	 *
	 * @return a list of all replicas.
	 */
	public Collection getReplicaVector() {
		return mReplicas;
	}

	/**
	 * Removes the delivered host from the list of leveled hosts and replicas.
	 *
	 * @param host the host to remove.
	 */
	synchronized public void remove(PGridHost host) {
		if (host == null)
			throw new NullPointerException();
		removeLevel(host);
		removeReplica(host);
	}

	/**
	 * Removes a level.
	 *
	 * @param i the level index.
	 */
	synchronized public void removeLevel(int i) {
		synchronized (mLevels) {
			if (i != (mLevels.size() - 1))
				throw new IllegalArgumentException();
			mLevels.remove(i);
		}
	}

	/**
	 * Removes the delivered host from the list of leveled hosts.
	 *
	 * @param host the host to remove.
	 */
	synchronized public void removeLevel(PGridHost host) {
		if (host == null)
			throw new NullPointerException();
		synchronized (mLevels) {
			for (int i = 0; i < mLevels.size(); i++) {
				Collection hosts = (Collection)mLevels.get(i);
				hosts.remove(host);
			}
		}
	}

	/**
	 * Removes the delivered host from the list of replicas.
	 *
	 * @param host the host to remove.
	 */
	synchronized public void removeReplica(PGridHost host) {
		if (host == null)
			throw new NullPointerException();
		mReplicas.remove(host);
	}

	/**
	 * Sets the fidget hosts with the given hosts.
	 *
	 * @param hosts the new hosts.
	 */
	synchronized public void setFidgets(Collection hosts) {
		if (hosts == null)
			throw new NullPointerException();
		mFidgets = Collections.synchronizedCollection(new TreeSet(hosts));
		for (Iterator iter = hosts.iterator(); iter.hasNext();) {
			PGridHost element = (PGridHost)iter.next();
			mHosts.put(element.getGUID().toString(), element);
		}
	}

	/**
	 * Sets the fidget hosts with the given hosts.
	 *
	 * @param hosts the new hosts.
	 */
	synchronized public void setFidgets(PGridHost[] hosts) {
		if (hosts == null)
			throw new NullPointerException();
		synchronized (mFidgets) {
			mFidgets.clear();
			for (int i = 0; i < hosts.length; i++) {
				mHosts.put(hosts[i].getGUID().toString(), hosts[i]);
				addFidget(hosts[i]);
			}
		}
	}

	/**
	 * Sets the whole level with the new delivered hosts.
	 *
	 * @param level the level to set.
	 * @param hosts the new hosts.
	 * @throws IllegalArgumentException if an illegal level is given.
	 */
	synchronized public void setLevel(int level, Collection hosts) throws IllegalArgumentException {
		if (hosts == null)
			throw new NullPointerException();
		for (Iterator it = hosts.iterator(); it.hasNext();) {
			PGridHost host = (PGridHost)it.next();
			mHosts.put(host.getGUID().toString(), host);
			remove(host);
		}
		setLevels(level);
		Collection coll = (Collection)mLevels.get(level);
		coll.clear();
		coll.addAll(hosts);
	}

	/**
	 * Sets the amount of levels.
	 *
	 * @param levels the amount of levels.
	 */
	synchronized public void setLevels(int levels) {
		synchronized (mLevels) {
			if (levels < -1) {
				Constants.LOGGER.log(Level.WARNING, "Illegal Argument in RoutingTable.setLevels() for level " + levels, new Throwable());
				return;
			}
			if (levels == -1) {
				mLevels.clear();
				return;
			}
			if (levels >= mLocalHost.getPath().length()) {
				Constants.LOGGER.log(Level.WARNING, "Illegal Argument in RoutingTable.setLevels() for level " + levels, new Throwable());
				return;
			}
			for (int i = mLevels.size() - 1; i < levels; i++)
				mLevels.add(new TreeSet());
		}
	}

	/**
	 * Sets the whole level with the new delivered hosts.
	 *
	 * @param level the level to set.
	 * @param hosts the new hosts.
	 */
	synchronized public void setLevel(int level, PGridHost[] hosts) {
		if (hosts == null)
			throw new NullPointerException();
		synchronized (mLevels) {
			setLevels(level);
			((Collection)mLevels.get(level)).clear();
			for (int i = 0; i < hosts.length; i++) {
				addLevel(level, hosts[i]);
				mHosts.put(hosts[i].getGUID().toString(), hosts[i]);
			}
		}
	}

	/**
	 * Sets the replicas with the given hosts.
	 *
	 * @param hosts the new hosts.
	 */
	synchronized public void setReplicas(Collection hosts) {
		if (hosts == null)
			throw new NullPointerException();
		for (Iterator it = hosts.iterator(); it.hasNext();) {
			PGridHost host = (PGridHost)it.next();
			mHosts.put(host.getGUID().toString(), host);
			removeLevel(host);
		}
		mReplicas = Collections.synchronizedCollection(new TreeSet(hosts));
	}

	/**
	 * Sets the replicas with the given hosts.
	 *
	 * @param hosts the new hosts.
	 */
	synchronized public void setReplicas(PGridHost[] hosts) {
		if (hosts == null)
			throw new NullPointerException();
		synchronized (mReplicas) {
			mReplicas.clear();
			for (int i = 0; i < hosts.length; i++) {
				mHosts.put(hosts[i].getGUID().toString(), hosts[i]);
				addReplica(hosts[i]);
			}
		}
	}

	/**
	 * Performs a union of the delivered and this Routing Table.
	 *
	 * @param routingTable a Routing Table.
	 */
	synchronized public void union(RoutingTable routingTable) {
		if (routingTable == null)
			throw new NullPointerException();
		unionFidgets(routingTable);
	}

	/**
	 * Performs a union of the Fidget hosts of the delivered and this Routing Table.
	 *
	 * @param routingTable a Routing Table.
	 */
	synchronized public void unionFidgets(RoutingTable routingTable) {
		if (routingTable == null)
			throw new NullPointerException();
		setFidgets(union(getFidgetVector(), routingTable.getFidgetVector()));
	}

	/**
	 * Performs a union of the hosts at the delivered level of the delivered and this Routing Table.
	 *
	 * @param level        the level.
	 * @param routingTable a Routing Table.
	 */
	synchronized public void unionLevel(int level, RoutingTable routingTable) {
		if (routingTable == null)
			throw new NullPointerException();
		setLevel(level, union(getLevelVector(level), routingTable.getLevelVector(level)));
	}

	/**
	 * Returns the union of the two delivered lists of hosts.
	 *
	 * @param refs1 the first list of hosts.
	 * @param refs2 the second list of hosts.
	 * @return the union of the two lists.
	 */
	protected Collection union(Collection refs1, Collection refs2) {
		if ((refs1 == null) || (refs2 == null))
			throw new NullPointerException();
		Collection set;

		if (refs1.isEmpty())
			set = Collections.synchronizedCollection(new TreeSet());
		else
			set = Collections.synchronizedCollection(new TreeSet(refs1));

		set.addAll(refs2);
		return set;
	}

}

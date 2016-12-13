/**
 * $Id: Exchange.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

import p2p.basic.GUID;
import pgrid.core.XMLRoutingTable;
import pgrid.core.storage.DBDataTable;
import pgrid.util.LexicalDefaultHandler;

/**
 * This class represents a P-Grid Exchange.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public abstract class Exchange extends LexicalDefaultHandler {

	/**
	 * The list of data items.
	 */
	protected DBDataTable mDataTable = null;

	/**
	 * The message GUID.
	 */
	protected GUID mGUID = null;

	/**
	 * The creating host.
	 */
	protected PGridHost mHost = null;

	/**
	 * The current common length.
	 */
	protected int mLenCurrent = 0;

	/**
	 * The min storage
	 */
	protected int mMinStorage = 0;

	/**
	 * The random number.
	 */
	protected double mRandomNumber = Double.MIN_VALUE;

	/**
	 * The recursion.
	 */
	protected int mRecursion = 0;

	/**
	 * The replication estimate.
	 */
	protected double mReplicateEstimate = 0;

	/**
	 * The routing table of the creating host.
	 */
	protected XMLRoutingTable mRoutingTable = null;

	/**
	 * Creates a new empty exchange.
	 */
	protected Exchange() {
	}

	/**
	 * Creates a new exchange with given values.
	 *
	 * @param guid         the Exchange GUID.
	 * @param host         the creating host.
	 * @param recursion    the recursion.
	 * @param lCurrent     the current common length.
	 * @param replicaEst   the replication estimate.
	 * @param routingTable the Routing Table of the creating host.
	 * @param dataTable    the list of data items.
	 */
	protected Exchange(GUID guid, PGridHost host, int recursion, int lCurrent, int minStorage, double replicaEst, XMLRoutingTable routingTable, DBDataTable dataTable) {
		mGUID = guid;
		mHost = host;
		mRecursion = recursion;
		mLenCurrent = lCurrent;
		mMinStorage = minStorage;
		mReplicateEstimate = replicaEst;
		mRoutingTable = routingTable;
		mDataTable = dataTable;
	}

	/**
	 * Returns the list of data items.
	 *
	 * @return the list of data items.
	 */
	public DBDataTable getDataTable() {
		return mDataTable;
	}

	/**
	 * Sets the list of data items.
	 *
	 * @param dataTable the list of data items.
	 */
	public void setDataTable(DBDataTable dataTable) {
		mDataTable = dataTable;
	}

	/**
	 * Returns the Exchange GUID.
	 *
	 * @return the Exchange GUID.
	 */
	public GUID getGUID() {
		return mGUID;
	}

	/**
	 * Returns the creating host.
	 *
	 * @return the creating host.
	 */
	public PGridHost getHost() {
		return mHost;
	}

	/**
	 * Returns the current common length.
	 *
	 * @return the current common length.
	 */
	public int getLenCurrent() {
		return mLenCurrent;
	}

	/**
	 * Returns the random number.
	 *
	 * @return the random number.
	 */
	public double getRandomNumber() {
		return mRandomNumber;
	}

	/**
	 * Sets the random number.
	 *
	 * @param randomNumber the random number.
	 */
	public void setRandomNumber(double randomNumber) {
		mRandomNumber = randomNumber;
	}

	/**
	 * Returns the recursion.
	 *
	 * @return the recursion.
	 */
	public int getRecursion() {
		return mRecursion;
	}

	/**
	 * Returns the replication estimate.
	 *
	 * @return the replication estimate.
	 */
	public double getReplicaEstimate() {
		return mReplicateEstimate;
	}

	/**
	 * Returns the Routing Table.
	 *
	 * @return the Routing Table.
	 */
	public XMLRoutingTable getRoutingTable() {
		return mRoutingTable;
	}

	/**
	 * Return the minimum storage before a split
	 * @return the minimum storage before a split
	 */
	public int getMinStorage() {
		return mMinStorage;
	}

}
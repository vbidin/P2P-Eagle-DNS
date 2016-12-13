/**
 * $Id: RangeQuery.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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
import p2p.basic.KeyRange;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;

/**
 * This class represent a range query message
 *
 * @author <a href="mailto:Renault JOHN <renault.john@epfl.ch>">Renault JOHN</a>
 */
public class RangeQuery extends AbstractQuery implements RangeQueryInterface {

	/**
	 * Sequential algorithm which consist in finding dynamicaly
	 * the next neighbor and send the range query to it.
	 */
	public static String MINMAX_ALGORITHM = "MinMax";

	/**
	 * Parallel algorithm which consist in sending the range query
	 * to all sub-tree this peer is responsible of.
	 */
	public static String SHOWER_ALGORITHM = "Shower";

	/**
	 * Bounds keys of the range query
	 */
	protected KeyRange mBoundsKeys = null;

	/**
	 * The first search string.
	 */
	protected String mFirstQueryString = null;

	/**
	 * The second search string.
	 */
	protected String mSecondQueryString = null;

	/**
	 * Indicate which algorithm to choose. Legal values are  sequential or parallel
	 */
	protected String mAlgorithm = null;

	/**
	 * prefix of the expected path for the current range query
	 */
	protected String mPrefix = "";

	/**
	 * Range query separator
	 */
	public static final String SEPARATOR = "@:-:@";


	/**
	 * Creates a new empty range Query.
	 */
	protected RangeQuery() {

	}

	/**
	 * @return Returns the key range.
	 */
	public KeyRange getKeyRange() {
		return mBoundsKeys;
	}

	/**
	 * @param kr The HigherBoundKey to set.
	 */
	public void setKeyRange(KeyRange kr) {
		mBoundsKeys = kr;
	}

	/**
	 * @return Returns the mAlgorithm.
	 */
	public String getAlgorithm() {
		return mAlgorithm;
	}

	/**
	 * @param algorithm The mAlgorithm to set.
	 */
	public void setAlgorithm(String algorithm) {
		mAlgorithm = algorithm;
	}

	/**
	 * @return Returns the SecondQueryString.
	 */
	public String getLowerBound() {
		return mFirstQueryString;
	}

	/**
	 * @return Returns the SecondQueryString.
	 */
	public String getHigherBound() {
		return mSecondQueryString;
	}

	public String getPrefix() {
		return mPrefix;
	}

	public void setPrefix(String prefix) {
		mPrefix = prefix;
	}

	/**
	 * Construct a range query message
	 *
	 * @param guid        the unique range query number
	 * @param type        the type of the query
	 * @param minQuery    least bound
	 * @param maxQuery    max bound
	 * @param kr          bounds of the range query
	 * @param minSpeed    the connection speed
	 * @param hops        number of hops taken by this query
	 * @param initialHost host that should recieve the result set
	 */
	public RangeQuery(GUID guid, p2p.storage.Type type, String algorithm, String minQuery, String maxQuery, KeyRange kr, String prefix, int minSpeed, int hops, PGridHost initialHost) {
		super(initialHost, guid, type, minSpeed, hops);
		mGUID = guid;
		mType = type;
		mFirstQueryString = minQuery;
		mSecondQueryString = maxQuery;
		mBoundsKeys = kr;
		mIndex = 0;
		mAlgorithm = algorithm;
		mPrefix = prefix;
	}

	/**
	 * Construct a range query message
	 *
	 * @param guid        the unique range query number
	 * @param type        the type of the query
	 * @param algorithm   to use
	 * @param minQuery    least bound
	 * @param maxQuery    max bound
	 * @param index       represent the resolved portion of the query
	 * @param minSpeed    the connection speed
	 * @param initialHost host that should recieve the result set
	 */
	public RangeQuery(GUID guid, p2p.storage.Type type, int hops, String algorithm, String minQuery, String maxQuery, KeyRange kr, int index, String prefix, int minSpeed, PGridHost initialHost) {
		this(guid, type, algorithm, minQuery, maxQuery, kr, prefix, minSpeed, hops, initialHost);
		mIndex = index;
	}

	/**
	 * Creates a new Query for a given search string. It is assumed that this peer
	 * is the initiator of the query and its address will be bound with the query as
	 * the destination for the result set. This constructor should not be used to route
	 * a query.
	 *
	 * @param type     the type of Query.
	 * @param firstQuery  lower bound string
	 * @param secondQuery higher bound string
	 * @param minSpeed the mininum connection speed for responding host.
	 */
	public RangeQuery(p2p.storage.Type type, KeyRange key, String firstQuery, String secondQuery, int minSpeed) {
		mGUID = new pgrid.GUID();
		mType = type;
		mFirstQueryString = firstQuery;
		mSecondQueryString = secondQuery;
		mBoundsKeys = key;
		mMinSpeed = minSpeed;
		mRequestingHost = PGridP2P.sharedInstance().getLocalHost();
		mIndex = 0;
		mAlgorithm = PGridP2P.sharedInstance().propertyString(Properties.RANGE_QUERY_ALGORITHM);
		mHops = 1;
		mPrefix = "";
	}

	/**
	 * Creates a new Query for a given search string. It is assumed that this peer
	 * is the initiator of the query and its address will be bound with the query as
	 * the destination for the result set. This constructor should not be used to route
	 * a query.
	 *
	 * @param guid the guid
	 * @param type     the type of Query.
	 * @param firstQuery  lower bound string
	 * @param secondQuery higher bound string
	 * @param minSpeed the mininum connection speed for responding host.
	 */
	public RangeQuery(GUID guid, p2p.storage.Type type, KeyRange key, String firstQuery, String secondQuery, int minSpeed) {
		mGUID = guid;
		mType = type;
		mFirstQueryString = firstQuery;
		mSecondQueryString = secondQuery;
		mBoundsKeys = key;
		mMinSpeed = minSpeed;
		mRequestingHost = PGridP2P.sharedInstance().getLocalHost();
		mIndex = 0;
		mHops = 1;
		mAlgorithm = PGridP2P.sharedInstance().propertyString(Properties.RANGE_QUERY_ALGORITHM);
	}

		/**
	 * Creates a new Query for a given search string. It is assumed that this peer
	 * is the initiator of the query and its address will be bound with the query as
	 * the destination for the result set. This constructor should not be used to route
	 * a query.
	 *
	 * @param host  	Initiator host
	 * @param type     the type of Query.
	 * @param firstQuery  lower bound string
	 * @param secondQuery higher bound string
	 */
	public RangeQuery(PGridHost host, p2p.storage.Type type, KeyRange key, String firstQuery, String secondQuery) {
		mGUID = PGridP2PFactory.sharedInstance().generateGUID();
		mType = type;
		mFirstQueryString = firstQuery;
		mSecondQueryString = secondQuery;
		mBoundsKeys = key;
		mMinSpeed = 0;
		mRequestingHost = host;
		mIndex = 0;
		mHops = 1;
		mAlgorithm = PGridP2P.sharedInstance().propertyString(Properties.RANGE_QUERY_ALGORITHM);
	}

	/**
	 * @see pgrid.QueryInterface#getRepresentation()
	 */
	public String getRepresentation() {
		return mFirstQueryString + " - " + mSecondQueryString;
	}

	/**
	 * Return true if the host is responsable for this query
	 * @param host
	 * @return true if responsible
	 */
	public boolean isHostResponsible(PGridHost host) {
		if (host.getPath().equals("")) return true;
		return getKeyRange().withinRange(new PGridKey(host.getPath()));
	}

}

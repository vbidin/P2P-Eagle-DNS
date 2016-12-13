/**
 * $Id: FileTypeHandler.java,v 1.1 2006/01/09 03:06:17 rschmidt Exp $
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

package test.gridella;

import p2p.basic.*;
import p2p.storage.*;
import p2p.storage.events.SearchListener;
import pgrid.PGridHost;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;
import pgrid.interfaces.storage.PGridStorage;

import java.util.Vector;

/**
 * This class represents the file manager for all shared and downloaded
 * files.
 * This class implements the <code>Singleton</code> pattern as defined by
 * Gamma et.al. As there could only exist one instance of this class, other
 * clients must use the <code>sharedInstance</code> function to use this class.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class FileTypeHandler implements TypeHandler {

	/**
	 * The P2P facility.
	 */
	protected P2P mP2P = PGridP2P.sharedInstance();

	/**
	 * The P2P Factory.
	 */
	protected P2PFactory mP2PFactory = null;

	/**
	 * The Storage facility.
	 */
	protected Storage mStorage = PGridStorage.sharedInstance();

	/**
	 * The data type this manager is responsible for.
	 */
	protected Type mType = null;

	/**
	 * Constructs the handler for the responsible type.
	 *
	 * @param type the responsible type.
	 */
	public FileTypeHandler(Type type) {
		mType = type;
		mP2PFactory = PGridP2PFactory.sharedInstance();
	}

  /**
   * Create a DataItem instance compatible with the Storage implementation.
   *
   * @return a DataItem instance
   */
  public DataItem createDataItem() {
	return new XMLFileDataItem();
  }

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param data the encapsulated data
	 * @return a DataItem instance
	 */
	public DataItem createDataItem(Object data) {
		GUID guid = mP2PFactory.generateGUID();
		Key key = mP2PFactory.generateKey(data.toString());
		Peer peer = mP2P.getLocalPeer();

		return (XMLFileDataItem)createDataItem(guid, key, peer, data);
	}

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @return a DataItem instance
	 */
	public DataItem createDataItem(PGridHost host, int qoS, String path, String name, int size, String infos, String desc) {
		GUID guid = mP2PFactory.generateGUID();
		Key key = mP2PFactory.generateKey(name);

		return new XMLFileDataItem(guid, mType, key, host, qoS, path, name, size, infos, desc);
	}

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param guid the guid of the data.
	 * @param key the key generated of the data.
	 * @param host the host.
	 * @param data the encapsulated data.
	 * @return a DataItem instance
	 */
	public DataItem createDataItem(GUID guid, Key key, Peer host, Object data) {
		return new XMLFileDataItem(guid, mType, key, host, data);
	}

	/**
	 * Searches for given query.
	 *
	 * @param query the query.
	 * @param listener the search listener.
	 */
	public void handleLocalSearch(Query query, SearchListener listener) {
		Vector result;
		String lower = query.getLowerBound().toUpperCase();
		String higher = query.getHigherBound().toUpperCase();
		boolean equal = lower.equals(higher);

		if (equal) {
			result = (Vector) PGridStorage.sharedInstance().getLocalDataItems(query.getLowerBound());
		} else {
			result = (Vector) PGridStorage.sharedInstance().getLocalDataItems(query.getLowerBound(),query.getHigherBound());
		}

		if (result.size() > 0)
			listener.newSearchResult(query.getGUID(), result);
		else
			listener.noResultsFound(query.getGUID());
		//listener.searchFinished(query.getGUID());
	}

	/**
	 * Construct the string out of the lowerbound that will be use to query
	 * the network. <br/>
	 * For exemple, if you have a lowerbound equals to "Key=Value" you could return
	 * "ValueKey" as the effective search string. The lower bound will be inlcuded in
	 * the query, but the lower bound used for the searching will be the return value
	 * of this method.
	 * <br/>
	 * <br/>
	 * If you want to search with the lower bound, either return null or the lower bound
	 *
	 * @param query
	 * @return a string use to perform the search
	 */
	public String submitSearchLowerBoundValue(Query query) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	/**
	 * Construct the string out of the higherbound that will be use to query
	 * the network. <br/>
	 * For exemple, if you have a higherbound equals to "Key=Value" you could return
	 * "ValueKey" as the effective search string. The higher bound will be inlcuded in
	 * the query, but the higher bound used for the searching will be the return value
	 * of this method.
	 * <br/>
	 * <br/>
	 * If you want to search with the higher bound, either return null or the lower bound
	 *
	 * @param query
	 * @return a string use to perform the search
	 */
	public String submitSearchHigherBoundValue(Query query) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

}

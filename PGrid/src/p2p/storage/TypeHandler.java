/**
 * $Id: TypeHandler.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

package p2p.storage;

import p2p.storage.events.SearchListener;
import p2p.storage.events.StorageListener;
import p2p.basic.Peer;
import p2p.basic.Key;
import p2p.basic.GUID;

import java.util.Collection;

/**
 * Used to define types of data items to store.
 * No particular operations are defined; however, implementers are
 * encouraged to provide consistent implementations of
 * java.land.Object methods, including equals and hashCode,
 * so that different types can be distinguished.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public interface TypeHandler {

  /**
   * Create a DataItem instance compatible with the Storage implementation.
   *
   * @return a DataItem instance
   */
  public DataItem createDataItem();

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param data the encapsulated data
	 * @return a DataItem instance
	 */
	public DataItem createDataItem(Object data);

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param guid the guid of the data.
	 * @param key the key generated of the data.
	 * @param host the host.
	 * @param data the encapsulated data.
	 * @return a DataItem instance
	 */
	public DataItem createDataItem(GUID guid, Key key, Peer host, Object data);

	/**
	 * Searches localy for all dataitems matching the query query
	 *
	 * @param query the query.
	 * @param listener the search listener.
	 */
	public void handleLocalSearch(Query query, SearchListener listener);

	/**
	 * Construct the string out of the lowerbound that will be use to query
	 * the network. <br/>
	 * For exemple, if you have a lowerbound equals to "Key=Value" you could return
	 * "ValueKey" as the effective search string. The lower bound will be inlcuded in
	 * the query, but the lower bound used for the searching will be the return value
	 * of this method.
	 *  <br/>
	 *  <br/>
	 * If you want to search with the lower bound, either return null or the lower bound
	 *
	 * @param query the query being processed.
	 * @return a string use to perform the search
	 */
	public String submitSearchLowerBoundValue(Query query);

	/**
	 * Construct the string out of the higherbound that will be use to query
	 * the network. <br/>
	 * For exemple, if you have a higherbound equals to "Key=Value" you could return
	 * "ValueKey" as the effective search string. The higher bound will be inlcuded in
	 * the query, but the higher bound used for the searching will be the return value
	 * of this method.
	 *  <br/>
	 *  <br/>
	 * If you want to search with the higher bound, either return null or the lower bound 
	 *
	 * @param query the query being processed.
	 * @return a string use to perform the search
	 */
	public String submitSearchHigherBoundValue(Query query);

}

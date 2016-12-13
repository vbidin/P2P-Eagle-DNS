/**
 * $Id: PGridStorage.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

package pgrid.interfaces.storage;

import p2p.basic.P2P;
import p2p.basic.events.NoRouteToKeyException;
import p2p.storage.Query;
import p2p.storage.Storage;
import p2p.storage.Type;
import p2p.storage.events.NoSuchTypeException;
import p2p.storage.events.SearchListener;
import p2p.storage.events.StorageListener;
import pgrid.core.storage.StorageManager;
import pgrid.core.search.SearchManager;
import pgrid.interfaces.basic.PGridP2P;

import java.util.*;

/**
 * Defines the operations that the storage layer supports.
 * It includes standard data (search, insert, delete, update) operations
 * and registration of callbacks associated with them.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class PGridStorage implements Storage {

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	private static final PGridStorage SHARED_INSTANCE = new PGridStorage();

	/**
	 * The search manager.
	 */
	private SearchManager mSearchManager = null;

  /**
   * The data item manager.
   */
  private StorageManager mStorageManager = null;

	/**
	 * The P2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The Storage Factory.
	 */
	private PGridStorageFactory mStorageFactory = null;

	/**
	 * The constructor must be protected to ensure that only subclasses can
	 * call it and that only one instance can ever get created. A client that
	 * tries to instantiate PGridIndexManager directly will get an error at compile-time.
	 */
	protected PGridStorage() {
		mStorageFactory = PGridStorageFactory.sharedInstance();
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static PGridStorage sharedInstance() {
		return SHARED_INSTANCE;
	}

	/**
	 * Initializes the P-Grid facility.
	 *
	 * @param p2p the P2P facility.
	 */
	synchronized public void init(P2P p2p) {
		mPGridP2P = (PGridP2P)p2p;
		mStorageManager = mPGridP2P.getStorageManager();
		mSearchManager = mPGridP2P.getSearchManager();
	}

	/**
	 * Register a listener of events related to data items.
	 * Such listeners are notified when operations on items
	 * on the the current node are requested.
	 *
	 * @param listener the listener to register
	 * @param type     the type of data items that the listener is interested in
	 */
	public void addStorageListener(StorageListener listener, Type type) {
		mStorageManager.addStorageListener(listener, type);	
	}

	/**
	 * Remove the data items with given Ds.
	 *
	 * @param items the data items to be removed
	 */
	public void delete(Collection items) {
		if (items == null)
			throw new NullPointerException();

		mStorageManager.deleteDataItems(items);
	}

	/**
	 * Store the data items into the network
	 *
	 * @param items the DataItems to insert
	 */
	public void insert(Collection items) {
		if (items == null)
			throw new NullPointerException();

		mStorageManager.insertDataItems(items);
	}

	/**
	 * Get collection of the local data items.
	 *
	 * @return the local data items.
	 */
	public Collection getLocalDataItems() {
		return mStorageManager.getDataTable().getDataItems();
	}

	/**
	 * Get collection of the local data items prefixed by the
	 * given prefix.
	 *
	 * @param prefix prefixing the data item data field
	 * @return the local data items.
	 */
	public Collection getLocalDataItems(String prefix) {
		return mStorageManager.getDataTable().getDataItemsDataPrefixed(prefix);
	}

	/**
	 * Get collection of the local data items prefixed by the
	 * given prefix.
	 *
	 * @param lowerPrefix prefixing the data item data field
	 * @param higherPrefix prefixing the data item data field
	 * @return the local data items.
	 */
	public Collection getLocalDataItems(String lowerPrefix, String higherPrefix) {
		return mStorageManager.getDataTable().getDataItemsDataPrefixed(lowerPrefix, higherPrefix);
	}

	/**
	 * Get collection of the local data items.
	 *
	 * @return the local data items.
	 */
	public Collection getOwnedDataItems() {
		return mStorageManager.getDataTable().getOwnedDataItems();
	}

	/**
	 * Search the network for matching items. Implemented as
	 * an asynchronous operation, because search might take
	 * some time. Callback is notified for each new result.
	 *
	 * @param query    the query used to specify the search
	 * @param listener an object to notify when results arrive
	 * @throws NoSuchTypeException if the provided Type is unknown.
   * @throws NoRouteToKeyException if the query cannot be routed to a responsible peer.
	 */
	public void search(Query query, SearchListener listener) throws NoSuchTypeException, NoRouteToKeyException {
		if ((query == null) || (listener == null))
			throw new NullPointerException();

		// forward the request to the search manager
		mSearchManager.search(query, listener);
	}

	/**
	 * Updates the dataitems into the network, if the items with such ID's already
	 * exist, they will be rewritten
	 *
	 * @param items the collection of data items
	 */
	public void update(Collection items) {
		if (items == null)
			throw new NullPointerException();

		mStorageManager.updateDataItems(items);
	}

	/**
	 * Shutdowns the storage service.
	 */
	public void shutdown() {
		mStorageManager.shutdown();
	}

}

/**
 * $Id: Storage.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

import p2p.basic.GUID;
import p2p.basic.events.NoRouteToKeyException;
import p2p.storage.events.SearchListener;
import p2p.storage.events.StorageListener;
import p2p.storage.events.NoSuchTypeException;

import java.util.Collection;

/**
 * Defines the operations that the storage layer supports.
 * It includes standard data (search, insert, delete, update) operations
 * and registration of callbacks associated with them.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public interface Storage {

	/**
	 * Register a listener of events related to data items.
	 * Such listeners are notified when operations on items
	 * on the the current node are requested.
	 *
	 * @param listener the listener to register
	 * @param type     the type of data items that the listener is interested in
	 */
	public void addStorageListener(StorageListener listener, Type type);

	/**
	 * Remove the data items with given Ds.
	 *
	 * @param items data items to be removed
	 */
	public void delete(Collection items);

	/**
	 * Store the data items into the network
	 *
	 * @param items the DataItems to insert
	 */
	public void insert(Collection items);

	/**
	 * Get collection of the local data items.
	 *
	 * @return the local data items.
	 */
	public Collection getLocalDataItems();

	/**
	 * Search the network for matching items. Implemented as
	 * an asynchronous operation, because search might take
	 * some time. Callback is notified for each new result.
	 *
	 * @param query    the query used to specify the search
	 * @param listener an object to notify when results arrive
	 * @throws p2p.storage.events.NoSuchTypeException if the provided Type is unknown.
   * @throws NoRouteToKeyException if the query cannot be routed to a responsible peer.
	 */
	public void search(Query query, SearchListener listener) throws NoSuchTypeException, NoRouteToKeyException;

	/**
	 * Inserts the dataitems int the network, if the items with such ID's already
	 * exist, they will be rewritten
	 *
	 * @param items the collection of data items
	 */
	public void update(Collection items);

	/**
	 * Shutdowns the storage service.
	 */
	public void shutdown();
	
}

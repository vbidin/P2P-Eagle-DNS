/**
 * $Id: StorageManager.java,v 1.3 2006/01/16 17:32:23 rschmidt Exp $
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

package pgrid.core.storage;

import p2p.basic.Key;
import p2p.basic.KeyRange;
import p2p.basic.Peer;
import p2p.storage.Query;
import p2p.storage.TypeHandler;
import p2p.storage.events.NoSuchTypeException;
import p2p.storage.events.NoSuchTypeHandlerException;
import p2p.storage.events.SearchListener;
import p2p.storage.events.StorageListener;
import pgrid.*;
import pgrid.core.DBManager;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.interfaces.basic.PGridP2PFactory;
import pgrid.network.protocol.DataModifierMessage;
import pgrid.network.protocol.XMLDataTable;
import pgrid.util.logging.LogFormatter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class StorageManager{

	/**
	 * The only instance of this class.
	 */
	private static StorageManager mInstance = new StorageManager();

	/**
	 * The hashtable of all registered data type handlers.
	 */
	protected Hashtable mDataTypeHandlers = new Hashtable();

	/**
	 * The hashtable of all created data types.
	 */
	protected Hashtable mDataTypes = new Hashtable();

	/**
	 * The Data Table.
	 */
	private DBDataTable mDBDataTable = null;

	/**
	 * The Database manager.
	 */
	private DBManager mDBManager = null;

	/**
	 * The Data Table.
	 */
	private LocalDataTable mDataTable = null;

	/**
	 * The data distributor.
	 */
	private Distributor mDistributor = null;

	/**
	 * The PGrid P2P instance.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The list of listener for a type of data item.
	 */
	private Hashtable mStorageListener = new Hashtable();

	/**
	 * The list of listener for all types of data item.
	 */
	private Vector mStorageListenerNoType = new Vector();

	/**
	 * The PGrid.Distributor logger.
	 */
	static final Logger LOGGER = Logger.getLogger("PGrid.StorageManager");

	static {
		LogFormatter formatter = new LogFormatter();
		formatter.setDateFormat("HH:mm:ss");
		formatter.setFormatPattern(LogFormatter.DATE + ": " + LogFormatter.MESSAGE + LogFormatter.NEW_LINE + LogFormatter.THROWABLE);
		Constants.initChildLogger(LOGGER, formatter, null);
	}

	/**
	 * Constructs the Storage Manager.
	 */
	protected StorageManager() {
	}

	/**
	 * Returns the only instance of this class.
	 * @return the only instance of this class.
	 */
	public static StorageManager getInstance() {
		if (mInstance == null) {
			synchronized(StorageManager.class) {
				 mInstance = new StorageManager();
			}
		}
		return mInstance;
	}

	/**
	 * Checks whether a given types exists already in PGridP2P.
	 * @param type the type to check.
	 */
	private void checkType(p2p.storage.Type type) {
		p2p.storage.Type t = (Type)mDataTypes.get(type.toString());
		if (t == null)
			throw new NoSuchTypeException();
	}

	/**
	 * Checks whether a type handler is already registered for a given type.
	 * @param type the type to check.
	 */
	private TypeHandler checkTypeHandler(p2p.storage.Type type) {
		TypeHandler h = (TypeHandler)mDataTypeHandlers.get(type.toString());
		if (h == null)
			throw new NoSuchTypeHandlerException();
		return h;
	}

	/**
	 * Compacts the DB and commits.
	 */
	public void compactDB() {
		mDBManager.compactDB();
	}

  /**
   * Create a DataItem instance compatible with the Storage implementation.
   *
   * @param type the data item's type
   * @return a DataItem instance
   * @throws NoSuchTypeException if the provided Type is unknown.
   */
  public p2p.storage.DataItem createDataItem(String type) throws NoSuchTypeException {
	Type t = createType(type);
	TypeHandler handler = checkTypeHandler(t);

	return handler.createDataItem();
  }

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param type the data item's type
	 * @param data the encapsulated data
	 * @return a DataItem instance
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public p2p.storage.DataItem createDataItem(p2p.storage.Type type, Object data) throws NoSuchTypeException {
		checkType(type);
		TypeHandler handler = checkTypeHandler(type);

		return handler.createDataItem(data);
	}

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param type the data item's type
	 * @param data the encapsulated data
	 * @return a DataItem instance
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public p2p.storage.DataItem createDataItem(GUID guid, Type type, PGridKey key, PGridHost host, Object data) throws NoSuchTypeException {
		checkType(type);
		TypeHandler handler = checkTypeHandler(type);

		return handler.createDataItem(guid, key, host, data);
	}

	/**
	 * Create a Query instance compatible with the Storage implementation.
	 *
	 * @param type        the Type of items the query is for.
	 * @param host        the host requesting the query.
	 * @param query the object that defines the query.
	 * @return a Query instance.
	 */
	public p2p.storage.Query createQuery(p2p.storage.Type type, Peer host, String query) {
		checkType(type);
		TypeHandler handler = checkTypeHandler(type);

		Query theQuery = new pgrid.Query((PGridHost)host, type, query, null);

		String searchQuery = handler.submitSearchLowerBoundValue(theQuery);
		if (searchQuery == null)
			searchQuery = query;

		Key key = PGridP2PFactory.sharedInstance().generateKey(searchQuery);
		((pgrid.Query)theQuery).setKey(key);

		return theQuery;
	}

	/**
	 * Create a Query instance compatible with the Storage implementation.
	 *
	 * @param type        	the Type of items the query is for
	 * @param lowerBound	the string object that defines the lower bound of the query
	 * @param higherBound	the string object that defines the higher bound of the query
	 * @return a Query instance
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public Query createQuery(p2p.storage.Type type, Peer host, String lowerBound, String higherBound) {
		checkType(type);

		TypeHandler handler = checkTypeHandler(type);

		RangeQuery rQuery = new pgrid.RangeQuery((PGridHost)host, type, null, lowerBound, higherBound);

		String searchQueryLB = handler.submitSearchLowerBoundValue(rQuery);
		if (searchQueryLB == null)
			searchQueryLB = lowerBound;
		String searchQueryHB = handler.submitSearchHigherBoundValue(rQuery);
		if (searchQueryHB == null)
			searchQueryHB = higherBound;

		KeyRange key = PGridP2PFactory.sharedInstance().generateKeyRange(searchQueryLB, searchQueryHB);

		rQuery.setKeyRange(key);
		
		return rQuery;
	}

	/**
	 * Creates a Type instance compatible with the Storage implementation.
	 *
	 * @param type an application-specific type to encapsulate
	 * @return a Type instance
	 */
	public Type createType(String type) {
		Type t = (Type)mDataTypes.get(type.toString());
		if (t == null) {
			t = new pgrid.Type(type);
			mDataTypes.put(type.toString(), t);
		}
		return t;
	}


	/**
	 * Returns the type of an entry represented by a string.
	 *
	 * @param type the string represeting a type (case-sensitiv).
	 * @return the type or null if no type has been registered for this string
	 */
	public p2p.storage.Type getTypeByString(String type) {
		return (p2p.storage.Type)mDataTypes.get(type);
	}

	/**
	 * Constructs the Storage Manager.
	 *
	 * @param file the local data table file name.
	 * @param host the local host.
	 */
	synchronized public void init(String file, PGridHost host) {
		mPGridP2P = PGridP2P.sharedInstance();

		mDBManager = DBManager.sharedInstance();
		mDBManager.init();

		mDBDataTable = new DBDataTable(host);

		//if (Constants.TESTS)
		//	mDataTable = new LocalDataTable(file, host.getPath());

		// start the distributor thread
		mDistributor = new Distributor();
		Thread distributorThread = new Thread(mDistributor, "Distributor");
		distributorThread.setDaemon(true);
		distributorThread.start();
	}

	/**
	 * Inserts the given data items.
	 * @param items the items to insert.
	 */
	public void insertDataItems(Collection items) {
		mDBDataTable.addAll(items);
		/*
		if (Constants.TESTS) {
			mPGridP2P.getStatistics().DataItemsManaged = mPGridP2P.getStorageManager().getDataTable().count();
			mPGridP2P.getStatistics().DataItemsPath = mPGridP2P.getStorageManager().getDataTable().getDataItems(mPGridP2P.getLocalHost().getPath()).size();
		}

		LOGGER.finest("Try to insert "+items.size()+" data items.");

		// inform type handlers about new data items
		Vector listeners= null;
		Hashtable inform = new Hashtable();
		p2p.storage.Type type = null;
		for (Iterator it = items.iterator(); it.hasNext();) {
			p2p.storage.DataItem item = (p2p.storage.DataItem)it.next();

			// get the storage listener list
			if (type == null || !type.equals(item.getType())) {
				type = item.getType();
				listeners = getStorageListener(type);
			}

			Vector v = (Vector)inform.get(listeners);
			if (v == null)
				v = new Vector();
			v.add(item);
			inform.put(listeners, v);
		}
		Enumeration en = inform.keys();
		while (en.hasMoreElements()) {
			listeners = (Vector)en.nextElement();
			Collection it = (Collection)inform.get(listeners);
			for(Iterator listener = listeners.iterator(); listener.hasNext();)
				((StorageListener)listener.next()).dataItemsAdded(it);
		}

		// try to distribute data items which not belong to the local peer
		mDistributor.insert(items);
		*/
	}

	/**
	 * Update data items.
	 *
	 * @param items the data item to update.
	 */
	public void updateDataItems(Collection items) {
		if (Constants.TESTS) {
			mPGridP2P.getStatistics().DataItemsManaged = mPGridP2P.getStorageManager().getDataTable().count();
			mPGridP2P.getStatistics().DataItemsPath = mPGridP2P.getStorageManager().getDataTable().getDataItems(mPGridP2P.getLocalHost().getPath()).size();
		}

		LOGGER.finer("Try to update "+items.size()+" data items.");

		// inform type handlers about new data items
		Hashtable inform = new Hashtable();
		Vector remote = new Vector();
		boolean notResponsable;
		pgrid.DataItem itemTemp;

		Vector listeners= null;
		p2p.storage.Type type = null;
		for (Iterator it = items.iterator(); it.hasNext();) {
			p2p.storage.DataItem item = (p2p.storage.DataItem)it.next();

			// get the storage listener list
			if (type == null || !type.equals(item.getType())) {
				type = item.getType();
				listeners = getStorageListener(type);
			}

			Vector v = (Vector)inform.get(listeners);
			if (v == null)
				v = new Vector();
			v.add(item);
			inform.put(listeners, v);
			LOGGER.finest("Updating data item with key:"+item.getKey()+".");
			if (mPGridP2P.isLocalPeerResponsible(item.getKey())) {
				// Check if the local host is still responsible for the new key
				itemTemp = (pgrid.DataItem)((DataItem)item).clone();
				itemTemp.setKey(PGridP2PFactory.sharedInstance().generateKey(itemTemp.getData()));
				notResponsable = !mPGridP2P.isLocalPeerResponsible(itemTemp.getKey());

				if (notResponsable) {
					// Those data items will be remove from the local host and send
					// to their responsable host
					remote.add(itemTemp);
					mDBDataTable.removeDataItem((DataItem)item);
					LOGGER.finest("Local peer not responsible anymore of the updated data item. Old key:"+
							item.getKey()+" new key:"+itemTemp.getKey());
				}
				else {
					mDBDataTable.updateDataItem((DataItem)item);
					LOGGER.finest("Local peer still responsible of the updated data item. key: "+itemTemp.getKey());
				}
			}
		}
		// If some data item have change there responsible host, insert them
		if (!remote.isEmpty()) {
			insertDataItems(remote);
		}

		Enumeration en = inform.keys();
		while (en.hasMoreElements()) {
			listeners = (Vector)en.nextElement();
			Collection it = (Collection)inform.get(listeners);
			for(Iterator listener = listeners.iterator(); listener.hasNext();)
				((StorageListener)listener.next()).dataItemsUpdated(it);
		}

		// try to distribute data items which not belong to the local peer
		mDistributor.update(items);

		// TODO: MERGE IDENTITY
		/*if (items == null)
			throw new NullPointerException();

		for (Iterator iter = items.iterator(); iter.hasNext();) {
			DataItem element = (DataItem)iter.next();
			updateDataItem(element);
		}*/
	}

	/**
	 * Remove the data items with given Ds.
	 *
	 * @param dataItems the ids of the data items to be removed
	 */
	public void deleteDataItems(Collection dataItems) {
		// this is a work around to use the same facility for deleting a data item then for update or insert
		if (Constants.TESTS) {
			mPGridP2P.getStatistics().DataItemsManaged = mPGridP2P.getStorageManager().getDataTable().count();
			mPGridP2P.getStatistics().DataItemsPath = mPGridP2P.getStorageManager().getDataTable().getDataItems(mPGridP2P.getLocalHost().getPath()).size();
		}

		LOGGER.finest("Try to delete "+dataItems.size()+" data items.");

		// inform type handlers about new data items
		Vector local = new Vector();
		Hashtable inform = new Hashtable();
		Vector listeners = null;
		p2p.storage.Type type = null;
		for (Iterator it = dataItems.iterator(); it.hasNext();) {
			p2p.storage.DataItem item = (p2p.storage.DataItem)it.next();

			// get the storage listener list
			if (type == null || !type.equals(item.getType())) {
				type = item.getType();
				listeners = getStorageListener(type);
			}

			Vector v = (Vector)inform.get(listeners);
			if (v == null)
				v = new Vector();
			v.add(item);
			inform.put(listeners, v);
			if (mPGridP2P.isLocalPeerResponsible(item.getKey())) {
				local.add(item);
			}
		}
		// update local data
		if (!local.isEmpty())
			mDBDataTable.removeAll(local, mPGridP2P.getLocalPeer());

		Enumeration en = inform.keys();
		while (en.hasMoreElements()) {
			listeners = (Vector)en.nextElement();
			Collection it = (Collection)inform.get(listeners);
			for(Iterator listener = listeners.iterator(); listener.hasNext();)
				((StorageListener)listener.next()).dataItemsRemoved(it);
		}

		// try to distribute data items which not belong to the local peer
		mDistributor.delete(dataItems);
	}


	/**
	 * Register a listener of events related to data items.
	 * Such listeners are notified when operations on items
	 * on the the current node are requested.
	 *
	 * @param listener the listener to register
	 * @param type     the type of data items that the listener is interested in
	 */
	public void addStorageListener(StorageListener listener, p2p.storage.Type type) {
		mStorageListener.put(type, listener);
	}

	/**
	 * Register a listener of events related to data items.
	 * Such listeners are notified when operations on items
	 * of any type on the the current node are requested.
	 *
	 * @param listener the listener to register
	 */
	public void addStorageListener(StorageListener listener) {
		mStorageListenerNoType.add(listener);
	}

	/**
	 * Returns a Type Handler instance for a given Type.
	 *
	 * @param type an application-specific type to encapsulate
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public Vector getStorageListener(p2p.storage.Type type) throws NoSuchTypeException {
		Vector listener = new Vector();
		checkType(type);

		if (!mStorageListenerNoType.isEmpty())
			listener.addAll(mStorageListenerNoType);

		listener.addAll(mStorageListener.values());

		return listener;
	}

	/**
	 * Returns the signature for the data items.
	 *
	 * @return the signature.
	 */
	public Signature getDataSignature() {
		return mDBDataTable.getSignature();
	}

	/**
	 * Returns the local PGridP2P Data Table.
	 *
	 * @return the Data Table.
	 */
	public DBDataTable getDataTable() {
		return mDBDataTable;
	}

	/**
	 * Sets the data table according to a view.
	 *
	 * @param table the new data table.
	 */
	public void setDataTable(DBView table) {
		mDBDataTable.setDataTable(table);
	}

	/**
	 * Returns a Type Handler instance for a given Type.
	 *
	 * @param type an application-specific type to encapsulate
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public TypeHandler getTypeHandler(p2p.storage.Type type) throws NoSuchTypeException {
		checkType(type);
		return checkTypeHandler(type);
	}

	/**
	 * Registers a Type Handler instance for a given Type.
	 *
	 * @param type an application-specific type to encapsulate
	 * @param handler an application-specific type handler to encapsulate
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public void registerTypeHandler(p2p.storage.Type type, TypeHandler handler) throws NoSuchTypeException {
		checkType(type);
		mDataTypeHandlers.put(type.toString(), handler);
	}

	/**
	 * Invoked when a new insert request was received by another host.
	 * @param dataModifier
	 */
	public void remoteDataModification(DataModifierMessage dataModifier) {
		mDistributor.remoteDistribution(dataModifier);
	}

	/**
	 * Search the local data table for matching items.
	 * Callback is notified for new result.
	 *
	 * @param query    the query used to specify the search
	 * @param listener an object to notify whith the result set
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public void matchLocalItems(p2p.storage.Query query, SearchListener listener) throws NoSuchTypeException {
		TypeHandler handler = getTypeHandler(query.getType());

		handler.handleLocalSearch(query, listener);
	}

	/**
	 * Shutdowns the P-Grid facility.
	 */
	synchronized public void shutdown() {
		if (Constants.TESTS) {
			writeDataTable();
		//		mDataTable.shutdown();
		}
		mDBManager.shutdown();
		mDistributor.shutdown();
	}

	/**
	 * Saves the shared files to the specified file.
	 * Used for testing only.
	 */
	public void writeDataTable() {
		// TESTS used for testing only
		XMLDataTable xmlDataTable = new XMLDataTable(mDBDataTable);
		try {
			FileWriter file = new FileWriter(Constants.DATA_DIR+"DataTable.xml");
			BufferedWriter out = new BufferedWriter(file);
			String content = xmlDataTable.toXMLString("", Constants.LINE_SEPERATOR);
			out.write(content);
			out.close();
		} catch (FileNotFoundException e) {
			Constants.LOGGER.log(Level.WARNING, null, e);
		} catch (IOException e) {
			Constants.LOGGER.log(Level.WARNING, null, e);
		}
	}

	/**
	 * Saves the shared files to the specified file.
	 *  This method is used only for testing purpose.
	 */
	synchronized public void saveDataTable() {
		//if (Constants.TESTS)
		//	mDataTable.save();
	}

	/**
	 * Sets the Data Table.
	 *  This method is used only for testing purpose.
	 *
	 * @param dataTable a Data Table.
	 */
	public void setDataTable(DataTable dataTable) {
		/*if (Constants.TESTS) {
			mDataTable.clear();
			mDataTable.addAll(dataTable.getDataItems());
		}*/
	}

	/**
	 * Sets the local path. This method is used only for testing purpose.
	 *
	 * @param path the local path.
	 */
	public synchronized void setLocalPath(String path) {
		/*if (Constants.TESTS) {
			if (mDataTable != null)
				mDataTable.setCommonPrefix(path);
		}*/
	}

}

/**
 * $Id: DataTable.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

import org.xml.sax.helpers.DefaultHandler;
import pgrid.DataItem;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.util.Utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

/**
 * This class represents the PGridP2P facility.
 * It is responsible for all activities in the Gridella network (search,
 * exchange).
 * This class implements the <code>Singleton</code> pattern as defined by
 * Gamma et.al. As there could only exist one instance of this class, other
 * clients must use the <code>sharedInstance</code> function to use this class.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class DataTable extends DefaultHandler {

	/**
	 * The new line character(s).
	 */
	protected static final String NEW_LINE = "\r\n";

	/**
	 * The shared file names.
	 */
	protected String mPath = null;

	/**
	 * The shared file names.
	 */
	protected StringBuffer mDataItemNames = new StringBuffer();

	/**
	 * The data items.
	 */
	protected Collection mDataItems = Collections.synchronizedCollection(new TreeSet());

	/**
	 * The data item manager
	 */
	protected StorageManager mStorageManager = null;

	/**
	 * The shared files by their key.
	 */
	protected Hashtable mDataItemsByKey = new Hashtable();

	/**
	 * The data items by their type.
	 */
	protected Hashtable mDataItemsByType = new Hashtable();

	/**
	 * The data items signature.
	 */
	protected Signature mSignature = null;

	/**
	 * Some utils.
	 */
	protected pgrid.util.Utils mUtils = new pgrid.util.Utils();

	/**
	 * Construct a new empty Data Table.
	 */
	public DataTable() {
		this("");
	}

	/**
	 * Construct a new empty Data Table.
	 *
	 * @param commonPrefix the common prefix for all data items.
	 */
	public DataTable(String commonPrefix) {
		mPath = commonPrefix;
		mStorageManager = PGridP2P.sharedInstance().getStorageManager();
	}

	/**
	 * Set the stat of the data table to unsaved so that next time the save method
	 * is called the data table will be written down.
	 * <p/>
	 * This method must be used when a modification to the data table is conducted
	 * outside of DataTable class.
	 */
	public void touch() {
		mSignature = null;
	}

	/**
	 * Construct a new Data Table with the given Data Items.
	 *
	 * @param dataItems the Data Items.
	 */
	public DataTable(Collection dataItems) {
		this("");
		if (dataItems != null)
			addAll(dataItems);
	}

	/**
	 * Construct a new Data Table with the given Data Items.
	 *
	 * @param commonPrefix the common prefix for all data items.
	 * @param dataItems    the Data Items.
	 */
	public DataTable(String commonPrefix, Collection dataItems) {
		this(commonPrefix);
		if (dataItems != null)
			addAll(dataItems);
	}

	/**
	 * Adds all delivered Data Items.
	 *
	 * @param collection the Collection.
	 */
	public synchronized void addAll(Collection collection) {
		if (collection == null)
			throw new NullPointerException();
		for (Iterator it = collection.iterator(); it.hasNext();) {
			// I don't know why, but I got some NullPointerExceptions from addDataItem(DataItem)
			DataItem item = (DataItem)it.next();
			if (item != null)
				addDataItem(item);
		}
	}

	/**
	 * Adds a SharedFile to the hashtables.
	 *
	 * @param dataItem the SharedFile.
	 */
	protected synchronized void addDataItem(DataItem dataItem) {
		if (dataItem == null)
			throw new NullPointerException();
		// Data Items
		if (!mDataItems.add(dataItem))
			return;

		// Data Items by Key
		Collection filesByKey = (Collection)mDataItemsByKey.get(dataItem.getKey());
		if (filesByKey == null)
			filesByKey = new Vector();
		else
			mDataItemsByKey.remove(dataItem.getKey());
		filesByKey.add(dataItem);
		mDataItemsByKey.put(dataItem.getKey(), filesByKey);

		// Data Items by Name
		Collection filesByType = (Collection)mDataItemsByType.get(dataItem.getType());
		if (filesByType == null)
			filesByType = new Vector();
		else
			mDataItemsByType.remove(dataItem.getType());
		filesByType.add(dataItem);
		mDataItemsByType.put(dataItem.getType(), filesByType);

		mSignature = null;
	}

	/**
	 * Removes all Data Items.
	 */
	public synchronized void clear() {
		mDataItemNames.delete(0, mDataItemNames.length());
		mDataItems.clear();
		mDataItemsByKey.clear();
		mDataItemsByType.clear();
		mSignature = null;
	}

	/**
	 * Sets the common prefix.
	 *
	 * @param prefix the common prefix.
	 */
	public synchronized void setCommonPrefix(String prefix) {
		mPath = prefix;
	}

	/**
	 * Returns the number of locally managed DataItems.
	 *
	 * @return the number of DataItems.
	 */
	public int count() {
		return mDataItems.size();
	}

	/**
	 * Returns the list of all Data Items.
	 *
	 * @return the list of all data items.
	 */
	public Collection getDataItems() {
		if (mDataItems.isEmpty())
			return new Vector();

		return new Vector(mDataItems);
	}

	/**
	 * Returns the list of data items of given type.
	 *
	 * @param type the requested data item type.
	 * @return the list of data items of given type.
	 */
	public Collection getDataItems(p2p.storage.Type type) {
		if (type == null)
			throw new NullPointerException();
		return (Vector)mDataItemsByType.get(type);
	}

	/**
	 * Returns the list of data items with the given path.
	 *
	 * @param path the path of the selected data items.
	 * @return the list of data items.
	 */
	public Collection getDataItems(String path) {
		if (path == null)
			throw new NullPointerException();
		String prefix = Utils.commonPrefix(mPath, path);
		int len = prefix.length();
		int lLen = mPath.length() - len;
		int rLen = path.length() - len;
		if ((lLen > 0) && (rLen > 0)) {
			return selectData(path);
		} else
			return new Vector(mDataItems);
	}

	/**
	 * Returns a list of data types handled by the data table.
	 *
	 * @return a list of data types.
	 */
	public Collection getDataTypes() {
		return mDataItemsByType.keySet();
	}

	/**
	 * Returns the signature for the data items.
	 *
	 * @return the signature.
	 */
	public Signature getSignature() {
		if (mSignature != null)
			return mSignature;
		StringBuffer signStr = new StringBuffer(mDataItems.size() * 100);
		synchronized (mDataItems) {
			for (Iterator it = mDataItems.iterator(); it.hasNext();) {
				DataItem dataItem = (DataItem)it.next();
				signStr.append(dataItem.getSignature() + NEW_LINE);
			}
		}
		mSignature = mUtils.signature(signStr.toString(), Signature.DEFAULT_PAGE_SIZE, Signature.DEFAULT_SIGN_LENGTH);
		return mSignature;
	}

	/**
	 * Sets the signature of the data table.
	 * @param signature the signature.
	 */
	public void setSignature(Signature signature) {
		mSignature = signature;
	}
	
	/**
	 * Removes the given Data Item.
	 *
	 * @param dataItem the item to remove.
	 */
	public synchronized void removeDataItem(DataItem dataItem) {
		if (dataItem == null)
			throw new NullPointerException();

		if (mDataItems.remove(dataItem)) {
			// Data Item was found an removed => remove it from the other lists
			Vector items = (Vector)mDataItemsByKey.get(dataItem.getKey());
			if (items != null) {
				items.remove(dataItem);
				mDataItemsByKey.put(dataItem.getKey(), items);
			}
			items = (Vector)mDataItemsByType.get(dataItem.getType());
			if (items != null) {
				items.remove(dataItem);
				mDataItemsByType.put(dataItem.getType(), items);
			}
			mSignature = null;
		}
	}

	/**
	 * Removes all given data items.
	 *
	 * @param items the items to remove.
	 */
	public void removeAll(Collection items) {
		if (items == null)
			throw new NullPointerException();

		for (Iterator it = items.iterator(); it.hasNext();) {
			DataItem item = (DataItem)it.next();
			removeDataItem(item);
		}
	}


	/**
	 * Selects the elements with the given prefix.
	 *
	 * @param prefix the prefix.
	 * @return the filtered Hashtable.
	 */
	public Collection selectData(String prefix) {
		if (prefix == null)
			throw new NullPointerException();

		if (prefix.length() == 0)
			return new Vector(mDataItems);
		Collection result;
		synchronized (mDataItemsByKey) {
			TreeSet treeSet = new TreeSet(mDataItemsByKey.keySet());

			String endPrefix = "2";
			if (prefix.length() > 0)
				endPrefix = prefix.substring(0, prefix.length() - 1).concat((prefix.endsWith("0") ? "1" : "2"));

			result = new Vector();
			Collection keys = treeSet.subSet(prefix, endPrefix);
			for (Iterator it = keys.iterator(); it.hasNext();) {
				String key = (String)it.next();
				Collection files = (Collection)mDataItemsByKey.get(key);
				result.addAll(files);
			}
		}
		return result;
	}

	/**
	 * Returns the set difference of the delivered Data Table and Data Items.
	 *
	 * @param dataTable a Data Table.
	 * @param dataItems the Data Items.
	 * @return the set difference between the Data Table and Data Items.
	 */
	public static DataTable setDifference(DataTable dataTable, Collection dataItems) {
		if ((dataTable == null) || (dataItems == null))
			throw new NullPointerException();

		DataTable result = new DataTable("", dataTable.getDataItems());
		for (Iterator it = dataItems.iterator(); it.hasNext();) {
			result.removeDataItem((DataItem)it.next());
		}
		return result;
	}

	/**
	 * Returns the union of the delivered Data Table and Data Items.
	 *
	 * @param dataTable1 a Data Table.
	 * @param dataTable2 the Data Items.
	 * @return the union of the Data Table and Data Items.
	 */
	public static DataTable union(DataTable dataTable1, DataTable dataTable2) {
		if ((dataTable1 == null) || (dataTable2 == null))
			throw new NullPointerException();

		DataTable result = new DataTable("", dataTable1.getDataItems());
		if (!dataTable1.getSignature().equals(dataTable2.getSignature()))
			result.addAll(dataTable2.getDataItems());
		return result;
	}

}
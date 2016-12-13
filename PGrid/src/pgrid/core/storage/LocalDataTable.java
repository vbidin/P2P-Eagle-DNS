/**
 * $Id*
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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import pgrid.Constants;
import pgrid.DataItem;
import pgrid.DataTypeHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class represents the PGridP2P facility.
 * It is responsible for all activities in the P-Grid network (search,
 * exchange).
 * This class implements the <code>Singleton</code> pattern as defined by
 * Gamma et.al. As there could only exist one instance of this class, other
 * clients must use the <code>sharedInstance</code> function to use this class.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 * @deprecated
 */
public class LocalDataTable extends DataTable {

	/**
	 * The file name of the data table.
	 */
	private String mFilename = null;

	/**
	 * The signature of the last saved data table.
	 */
	private Signature mLastSignature = null;

	/**
	 * If set, no more saves are allowed.
	 */
	private boolean mShutdownFlag = false;

	/**
	 * The handlers of data items by data item type.
	 */
	private Hashtable mTypeHandlers = new Hashtable();

	/**
	 * Construct a new empty Data Table.
	 *
	 * @param file the local data table file name.
	 * @param commonPrefix the common prefix for all data items.
	 * @deprecated
	 */
	public LocalDataTable(String file, String commonPrefix) {
		super(commonPrefix);
		mFilename = file;
		// load managed data items from file
		File gridFile = new File(mFilename);
		if (gridFile.exists()) {
			// load managed files
			try {
				BufferedReader in = new BufferedReader(new FileReader(gridFile));
				char[] content = new char[(int)gridFile.length()];
				in.read(content, 0, content.length);
				in.close();
				SAXParserFactory spf = SAXParserFactory.newInstance();
				XMLReader parser = spf.newSAXParser().getXMLReader();
				parser.setContentHandler(this);
				parser.parse(new InputSource(new StringReader(new String(content))));
			} catch (SAXParseException e) {
				Constants.LOGGER.warning("Could not read local data table: Parse error in line '" + e.getLineNumber() + "', column '" + e.getColumnNumber() + "'! (" + e.getMessage() + ")");
			} catch (SAXException e) {
				System.err.println("Could not read local data table:");
				e.printStackTrace();
			} catch (IOException e) {
				Constants.LOGGER.severe("Could not load shared P-Grid files from '" + mFilename + "'!");
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}

	/**
	 * Adds all delivered Data Items.
	 *
	 * @param collection the Collection.
	 */
	public synchronized void addAll(Collection collection) {
		addAll(collection, false);
	}

	/**
	 * Adds all delivered Data Items.
	 *
	 * @param collection the Collection.
	 * @param silent     to add a data item by a data item type handler.
	 */
	public synchronized void addAll(Collection collection, boolean silent) {
		if (collection == null)
			throw new NullPointerException();
		for (Iterator it = collection.iterator(); it.hasNext();) {
			addDataItem((DataItem)it.next(), silent);
		}
	}                                  

	/**
	 * Adds a SharedFile to the hashtables.
	 *
	 * @param dataItem the SharedFile.
	 */
	protected synchronized void addDataItem(DataItem dataItem) {
		addDataItem(dataItem, false);
	}

	/**
	 * Adds a SharedFile to the hashtables.
	 *
	 * @param dataItem the SharedFile.
	 * @param silent   to add a data item by a data item type handler.
	 */
	synchronized void addDataItem(DataItem dataItem, boolean silent) {
		if (dataItem == null)
			throw new NullPointerException();

		// Data Items
		if (!mDataItems.contains(dataItem)) {
			super.addDataItem(dataItem);
			if (!silent) {
				DataTypeHandler handler = (DataTypeHandler)mTypeHandlers.get(dataItem.getType());
				if (handler != null) {
					handler.dataItemAdded(dataItem);
				}
			}
		}
	}

	/**
	 * Removes all Data Items.
	 */
	public synchronized void clear() {
		super.clear();
		Collection handlers = mTypeHandlers.values();
		for (Iterator it = handlers.iterator(); it.hasNext();) {
			DataTypeHandler handler = (DataTypeHandler)it.next();
			handler.dataTableCleared();
		}
	}

	/**
	 * Registers a new handler for a type of data items.
	 *
	 * @param type    the data item type.
	 * @param handler the type handler.
	 */
	public void registerTypeHandler(p2p.storage.Type type, DataTypeHandler handler) {
		mTypeHandlers.put(type, handler);
	}

	/**
	 * Saves the shared files to the specified file.
	 */
	synchronized public void save() {
	}

	/**
	 * If P-Grid is shutdown a the data table is saved to the file.
	 */
	public synchronized void shutdown() {
		save();
		mShutdownFlag = true;
	}

}
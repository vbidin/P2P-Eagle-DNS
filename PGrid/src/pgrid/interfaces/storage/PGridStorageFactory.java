/**
 * $Id: PGridStorageFactory.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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
import p2p.basic.P2PFactory;
import p2p.storage.DataItem;
import p2p.storage.Query;
import p2p.storage.Storage;
import p2p.storage.StorageFactory;
import p2p.storage.Type;
import p2p.storage.TypeHandler;
import p2p.storage.events.NoSuchTypeException;
import pgrid.core.storage.StorageManager;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2PFactory;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.PGridHost;

import java.util.Properties;

/**
 * Factory that defines the operations that create various
 * objects of the Storage subsystem. It is recommended to instantiate
 * such types only through a concrete implementation of this factory
 * to avoid hard-coding direct references to them.
 * This class provides static methods to find concrete factories
 * using the reflection API to further decouple the subsystem from its
 * client.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class PGridStorageFactory extends StorageFactory {

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	protected static final PGridStorageFactory SHARED_INSTANCE = new PGridStorageFactory();

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * The local  host.
	 */
	private PGridHost mLocalHost = null;

	/**
	 * The constructor must be protected to ensure that only subclasses can
	 * call it and that only one instance can ever get created. A client that
	 * tries to instantiate PGridIndexManager directly will get an error at compile-time.
	 */
	protected PGridStorageFactory() {
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static PGridStorageFactory sharedInstance() {
		return SHARED_INSTANCE;
	}

	/**
	 * Create the concrete Storage implementation.
	 *
	 * @return the Storage implementation
	 */
	public Storage createStorage(P2P p2p) {
		// create P2P layer
		PGridStorage storage = PGridStorage.sharedInstance();
		storage.init(p2p);

		mStorageManager = PGridP2P.sharedInstance().getStorageManager();

		// set the local host
		mLocalHost = (PGridHost)p2p.getLocalPeer();
		return storage;
	}

	/**
	 * Create a Query instance compatible with the Storage implementation.
	 *
	 * @param type        the Type of items the query is for
	 * @param query the object that defines the query
	 * @return a Query instance
	 */
	public Query createQuery(Type type, String query) throws NoSuchTypeException {
		if ((type == null) || (query == null))
			throw new NullPointerException();

		return mStorageManager.createQuery(type, mLocalHost, query);
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
	public Query createQuery(Type type, String lowerBound, String higherBound) {
		if ((type == null) || (lowerBound == null) || (higherBound == null))
			throw new NullPointerException();

		return mStorageManager.createQuery(type, mLocalHost, lowerBound, higherBound);
	}

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param type the data item's type
	 * @param data the encapsulated data
	 * @return a DataItem instance
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public DataItem createDataItem(Type type, Object data) throws NoSuchTypeException {
		if ((type == null) || (data == null))
			throw new NullPointerException();

		return mStorageManager.createDataItem(type, data);
	}

	/**
	 * Creates a Type instance compatible with the Storage implementation.
	 *
	 * @param type an application-specific type to encapsulate
	 * @return a Type instance
	 */
	public Type createType(String type) {
		if (type == null)
			throw new NullPointerException();

		return mStorageManager.createType(type);
	}

	/**
	 * Registers a Type Handler instance for a given Type.
	 *
	 * @param type an application-specific type to encapsulate
	 * @param handler an application-specific type handler to encapsulate
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public void registerTypeHandler(Type type, TypeHandler handler) throws NoSuchTypeException {
		if ((type == null) || (handler == null))
			throw new NullPointerException();

		mStorageManager.registerTypeHandler(type, handler);
	}

}

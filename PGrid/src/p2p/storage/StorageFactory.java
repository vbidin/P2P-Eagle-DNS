/**
 * $Id: StorageFactory.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

import p2p.storage.events.NoSuchTypeException;
import p2p.basic.P2P;

import java.util.Properties;

/**
 * Abstract Factory (GoF) that defines the operations that create various
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
public abstract class StorageFactory {

	/**
	 * Create a DataItem instance compatible with the Storage implementation.
	 *
	 * @param type the data item's type
	 * @param data the encapsulated data
	 * @return a DataItem instance
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public abstract DataItem createDataItem(Type type, Object data) throws NoSuchTypeException;

	/**
	 * Create a Query instance compatible with the Storage implementation.
	 *
	 * @param type        the Type of items the query is for
	 * @param queryString the string object that defines the query
	 * @return a Query instance
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public abstract Query createQuery(Type type, String queryString) throws NoSuchTypeException;

	/**
	 * Create a Query instance compatible with the Storage implementation.
	 *
	 * @param type        	the Type of items the query is for
	 * @param lowerBound	the string object that defines the lower bound of the query
	 * @param higherBound	the string object that defines the higher bound of the query
	 * @return a Query instance
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public abstract Query createQuery(Type type, String lowerBound, String higherBound) throws NoSuchTypeException;

	/**
	 * Create the concrete Storage implementation.
	 *
	 * @param p2p the P2P implementation.
	 * @return the Storage implementation.
	 */
	public abstract Storage createStorage(P2P p2p);

	/**
	 * Creates a Type instance compatible with the Storage implementation.
	 *
	 * @param type an application-specific type to encapsulate
	 * @return a Type instance
	 */
	public abstract Type createType(String type);

	/**
	 * Registers a Type Handler instance for a given Type.
	 *
	 * @param type an application-specific type to encapsulate
	 * @param handler an application-specific type handler to encapsulate
	 * @throws NoSuchTypeException if the provided Type is unknown.
	 */
	public abstract void registerTypeHandler(Type type, TypeHandler handler) throws NoSuchTypeException;

}

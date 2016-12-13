/**
 * $Id: DataItem.java,v 1.2 2005/11/07 16:56:33 rschmidt Exp $
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
import p2p.basic.Key;
import p2p.basic.Peer;

/**
 * Defines the operations that all data items stored on the network support.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public interface DataItem extends Comparable {

	/**
	 * Get the key index of the item.
	 *
	 * @return the Key index
	 */
	public Key getKey();

	/**
	 * Get the ID of the item.
	 *
	 * @return the Key index
	 */
	public GUID getGUID();

	/**
	 * Get the item's type of content.
	 *
	 * @return the Type of the item's content
	 */
	public Type getType();

	/**
	 * Get the item's content.
	 *
	 * @return an Object reference to the data
	 */
	public Object getData();

	/**
	 * Set the item's content.
	 *
	 * @param newData the new data
	 */
	public void setData(Object newData);

	/**
	 * Get the host responsible for this item.
	 *
	 * @return a Peer reference to the host
	 */
	public Peer getPeer();
	
}

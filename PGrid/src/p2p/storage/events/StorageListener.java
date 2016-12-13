/**
 * $Id: StorageListener.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

package p2p.storage.events;

import p2p.storage.DataItem;
import p2p.storage.Query;

import java.util.Collection;

/**
 * Defines the callback interface to notify of events related
 * to stored data items.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public interface StorageListener {

	/**
	 * Invoked when data items were added to the data table.
	 *
	 * @param items the added data item.
	 */
	public void dataItemsAdded(Collection items);

	/**
	 * Invoked when data items were removed from the data table.
	 *
	 * @param items the removed data item.
	 */
	public void dataItemsRemoved(Collection items);

	/**
	 * Invoked when data items were updated from the data table.
	 *
	 * @param items the removed data item.
	 */
	public void dataItemsUpdated(Collection items);

	/**
	 * Invoked when the data table is cleared.
	 */
	public void dataTableCleared();

}

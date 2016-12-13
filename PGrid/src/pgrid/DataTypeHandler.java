/**
 * $Id: DataTypeHandler.java,v 1.3 2005/11/15 09:57:50 john Exp $
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
package pgrid;

import java.util.Collection;

/**
 * This interface is implemented by handlers of data item types.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 * @deprecated
 */
public interface DataTypeHandler {

	/**
	 * Invoked when a data item was added to the data table.
	 *
	 * @param item the added data item.
	 */
	public void dataItemAdded(DataItem item);

	/**
	 * Invoked when a data item was removed from the data table.
	 *
	 * @param item the removed data item.
	 */
	public void dataItemRemoved(DataItem item);

	/**
	 * Invoked when the data table was cleared.
	 */
	public void dataTableCleared();

	/**
	 * Searches for given query.
	 *
	 * @param query the query.
	 * @return a list of found data items for the given query.
	 */
	public Collection handleSearch(ExactQueryInterface query);

	/**
	 * Searches for given query.
	 *
	 * @param query the query.
	 * @return a list of found data items for the given query.
	 */
	public Collection handleSearch(RangeQueryInterface query);

	/**
	 * Handles a new local search requests.
	 *
	 * @param query the query.
	 * @return the list of initiated queries.
	 */
	public AbstractQuery[] search(pgrid.QueryInterface query);

	/**
	 * Perform an update operation.
	 *
	 * @param item updated version
	 * @return true if the update has been perform
	 */
	public boolean handleUpdate(DataItem item);
}

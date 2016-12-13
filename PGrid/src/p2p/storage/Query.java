/**
 * $Id: Query.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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
import p2p.basic.KeyRange;

/**
 * Defines the operations that queries support. A query includes
 * a query string pair and a type of relevant items. It is used to
 * specify a search request for the storage layer.
 *
 * A simple query, where we are looking for a precise data item, is considered
 * as a special kind of range query where both bounds are equals.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public interface Query {

	/**
	 * Get the query's guid. Used to identify the query across
	 * the network.
	 *
	 * @return the global unique identifier
	 */
	public GUID getGUID();

  /**
   * Get the key of the query used to route it to the responsible peer.
   *
   * @return the query's key.
   */
	  public KeyRange getKeyRange();

	/**
	 * Get the query's status code, as defined by the storage layer.
	 *
	 * @return the status code
	 */
	public int getStatus();

	/**
	 * Get the type of items the query considers, e.g. text/file, etc.
	 *
	 * @return the query's target type
	 */
	public Type getType();

	/**
	 * Get the lower bound of the range query string that defines the query, e.g. a keyword
	 * such as 'Madonna' or a semantic query such as
	 * '&lt;predicate&gt;Actor&lt;/predicate&gt;&lt;object&gt;Madonna&lt;/object&gt;'.
	 *
	 * @return the query string
	 */
	public String getLowerBound();

	/**
	 * Get the higher query string that defines the query, e.g. a keyword
	 * such as 'Madonna' or a semantic query such as
	 * '&lt;predicate&gt;Actor&lt;/predicate&gt;&lt;object&gt;Madonna&lt;/object&gt;'.
	 *
	 * @return the query string
	 */
	public String getHigherBound();

}

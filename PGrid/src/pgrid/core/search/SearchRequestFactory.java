/**
 * $Id: SearchRequestFactory.java,v 1.3 2005/11/15 09:57:52 john Exp $
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
package pgrid.core.search;

import pgrid.RangeQuery;
import p2p.storage.Query;
import p2p.storage.events.SearchListener;

/**
 * Implementation of the abstract factory from Gamma et al.
 *
 * This factory provides a way to create routing request easely.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

public class SearchRequestFactory {

	protected static ExactSearchStrategy mExactSearch = new ExactSearchStrategy();
	protected static RangeQuerySearchStrategy mRangeQuerySearch = new RangeQuerySearchStrategy();

	/**
	 * Create a new query routing request
	 * @param query the query
	 * @param listener the listner
	 * @return a QueryRequest to be used by the router
	 */
	public static SearchRequest createSearchRequest(Query query, SearchListener listener) {
		if (query instanceof pgrid.Query)
			return new SearchRequest(query, listener, mExactSearch);
		else return new SearchRequest(query, listener, mRangeQuerySearch);
	}

	/**
	 * Create a new range query routing request
	 * @param query the query
	 * @param listener the listner
	 * @return a QueryRequest to be used by the router
	 */
	public static SearchRequest createSearchRequest(RangeQuery query, SearchListener listener) {
		return new SearchRequest(query, listener, mRangeQuerySearch);
	}
}

/**
 * $Id: QueryRoutingRequest.java,v 1.4 2005/12/02 17:58:20 john Exp $
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

package pgrid.network.router;

import p2p.storage.Query;
import p2p.storage.events.SearchListener;

/**
 * A routing request for a query.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class QueryRoutingRequest implements Request {

	private Query mQuery = null;

	private SearchListener mSearchListener = null;

	private long mStartTime = 0;

	public QueryRoutingRequest(Query query, SearchListener listener) {
		mQuery = query;
		mSearchListener = listener;
	}

	public Query getQuery() {
		return mQuery;
	}

	public SearchListener getSearchListener() {
		return mSearchListener;
	}

	public void startProcessing() {
		mStartTime = System.currentTimeMillis();
	}

	public long getStartTime() {
		return mStartTime;
	}

	public String getRoutingStrategyName() {
		return pgrid.network.router.ExactQueryRoutingStrategy.STRATEGY_NAME;
	}
}

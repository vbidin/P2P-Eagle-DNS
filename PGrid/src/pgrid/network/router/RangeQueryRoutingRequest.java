/**
 * $Id: RangeQueryRoutingRequest.java,v 1.2 2005/11/07 16:56:39 rschmidt Exp $
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
import pgrid.RangeQuery;
import pgrid.PGridHost;

/**
 * A routing request for a range query.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
class RangeQueryRoutingRequest implements Request {

	private RangeQuery mQuery = null;

	private SearchListener mSearchListener = null;

	private String mStrategy;

	private long mStartTime = 0;

	private PGridHost mNextHost;

	private String mPath;

	RangeQueryRoutingRequest(RangeQuery query, SearchListener listener, String strategy) {
		mQuery = query;
		mSearchListener = listener;
		mStrategy = strategy;

	}

	public RangeQuery getQuery() {
		return mQuery;
	}

	public SearchListener getSearchListener() {
		return mSearchListener;
	}

	public void startProcessing() {
		mStartTime = System.currentTimeMillis();
	}

	public String getRoutingStrategyName() {
		return mStrategy;
	}

	public void setNextHost(PGridHost host) {
		mNextHost = host;
	}

	public PGridHost popNextHost() {
		PGridHost tmp = mNextHost;
		mNextHost = null;
		return tmp;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		this.mPath = path;
	}

	public long getStartTime() {
		return mStartTime;
	}
	
}

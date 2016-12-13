/**
 * $Id: RangeQuerySearchStrategy.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

import pgrid.network.router.RoutingRequestFactory;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.core.storage.StorageManager;
import pgrid.RangeQuery;

/**
 * This class represents a range query request. It is used internally in P-Grid.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

class RangeQuerySearchStrategy implements SearchStrategy {

	/**
	 * The P-Grid P2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager = mPGridP2P.getStorageManager();


	public void handleSearch(SearchRequest request) {
		RangeQuery query = (pgrid.RangeQuery)request.getQuery();

		// check if the local peer is responsible for the query
		if (query.isHostResponsible(mPGridP2P.getLocalHost())) {
			// start the local search
			mStorageManager.matchLocalItems(request.getQuery(), request.getSearchListener());

		}

		// route the query to a responsible peer
		mPGridP2P.getRouter().route(RoutingRequestFactory.createRangeQueryRoutingRequest(query,
				request.getSearchListener()));
	}
}

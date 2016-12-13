/**
 * $Id: DistributionRequest.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

package pgrid.core.storage;

import java.util.Collection;

/**
 * A distribution request.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class DistributionRequest  implements DistributionRequestInt  {
	private Collection mItems = null;

	private short mRequest = -1;

	private long mStartTime;

	public DistributionRequest(short request, Collection items) {
		mRequest = request;
		mItems = items;
		mStartTime = System.currentTimeMillis();
	}

	public Collection getItems() {
		return mItems;
	}

	public short getRequest() {
		return mRequest;
	}

	public long getStartTime() {
		return mStartTime;
	}

}

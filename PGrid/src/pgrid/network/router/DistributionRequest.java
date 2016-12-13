/**
 * $Id: DistributionRequest.java,v 1.2 2005/11/07 16:56:38 rschmidt Exp $
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

import p2p.basic.Key;
import pgrid.core.storage.DistributionListener;
import pgrid.network.protocol.PGridMessage;

/**
 * A routing request for an insert/update/delete message.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class DistributionRequest implements Request {

	private DistributionListener mDistributionListener = null;


	private Key mKey = null;

	private PGridMessage mMsg = null;

	private long mStartTime=0;

	public DistributionRequest(Key key, PGridMessage msg, DistributionListener listener) {
		mKey = key;
		mMsg = msg;
		mDistributionListener = listener;
	}

	public DistributionListener getDistributionListener() {
		return mDistributionListener;
	}

	public Key getKey() {
		return mKey;
	}

	public PGridMessage getMessage() {
		return mMsg;
	}

	public String getRoutingStrategyName() {
		return pgrid.network.router.DistributionRoutingStrategy.STRATEGY_NAME;
	}

	public void startProcessing() {
		mStartTime = System.currentTimeMillis();
	}

	public long getStartTime() {
		return mStartTime;
	}
}

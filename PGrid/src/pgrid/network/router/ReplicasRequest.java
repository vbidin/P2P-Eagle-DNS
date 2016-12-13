/**
 * $Id: ReplicasRequest.java,v 1.2 2005/11/07 16:56:39 rschmidt Exp $
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

import pgrid.network.protocol.PGridMessage;

import java.util.Vector;

/**
 * A routing request to all replicas.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class ReplicasRequest implements Request {

	private Vector mExclReplicas = null;

	private PGridMessage mMessage = null;

	private String mStrategy;

	public ReplicasRequest(PGridMessage message, Vector exclReplicas) {
		mMessage = message;
		mExclReplicas = exclReplicas;
	}

	public Vector getExclReplicas() {
		return mExclReplicas;
	}

	public PGridMessage getMessage() {
		return mMessage;
	}

	public String getRoutingStrategyName() {
		return pgrid.network.router.ReplicasRoutingStrategy.STRATEGY_NAME;
	}
}

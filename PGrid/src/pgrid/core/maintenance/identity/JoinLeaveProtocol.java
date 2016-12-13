/**
 * $Id: JoinLeaveProtocol.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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
package pgrid.core.maintenance.identity;

/**
 * This interface represent the minimum to implement in order to be responsible for
 * the join - leave protocol of P-Grid
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 */
public interface JoinLeaveProtocol {

	/**
	 * Unique name of the protocol
	 *
	 * @return the name
	 */
	public String getProtocolName();

	/**
	 * This method is called when a peer join the network for the first time
	 */
	public void newlyJoined();

	/**
	 * This method is called when a peer rejoins the network.
	 */
	public void join();

	/**
	 * This method is called when a peer leaves the network.
	 */
	public void leave();
}

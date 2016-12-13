/**
 * $Id: NoMaintenancePolicy.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

import pgrid.core.maintenance.identity.MaintenancePolicy;
import pgrid.PGridHost;
import pgrid.DataItem;

/**
 * This class is a place holder. It does nothing for the maintenance or the
 * leave - join protocol.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 */
public class NoMaintenancePolicy implements MaintenancePolicy, pgrid.core.maintenance.identity.JoinLeaveProtocol {

	public static final String PROTOCOL_NAME = "None";

	/**
	 * @see pgrid.core.maintenance.identity.JoinLeaveProtocol#getProtocolName()
	 */
	public String getProtocolName() {
		return PROTOCOL_NAME;
	}

	/**
	 * @see pgrid.core.maintenance.identity.MaintenancePolicy#stale(pgrid.PGridHost)
	 */
	public boolean stale(PGridHost host) {
		return false;
	}

	/**
	 * @see pgrid.core.maintenance.identity.MaintenancePolicy#handleUpdate(pgrid.DataItem)
	 */
	public boolean handleUpdate(DataItem item) {
		return false;
	}

	/**
	 * @see pgrid.core.maintenance.identity.JoinLeaveProtocol#newlyJoined()
	 */
	public void newlyJoined() {
		// do nothing
	}

	/**
	 * @see pgrid.core.maintenance.identity.JoinLeaveProtocol#join()
	 */
	public void join() {
		// do nothing
	}

	/**
	 * @see pgrid.core.maintenance.identity.JoinLeaveProtocol#leave()
	 */
	public void leave() {
		// do nothing
	}

}

/**
 * $Id: ExchangeInvitationRequest.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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

package pgrid.core.maintenance;

import pgrid.network.protocol.ExchangeInvitationMessage;

/**
 * This class represent a request for an exchange invitation
 */
class ExchangeInvitationRequest {

	private ExchangeInvitationMessage mExchangeInvitation = null;

	private long mStartTime = 0;

	public ExchangeInvitationRequest(ExchangeInvitationMessage exchangeInvitation) {
		mExchangeInvitation = exchangeInvitation;
		mStartTime = System.currentTimeMillis();
	}

	public ExchangeInvitationMessage getExchangeInvitation() {
		return mExchangeInvitation;
	}

	public long getStartTime() {
		return mStartTime;
	}

}

/**
 * $Id: IdentityMappingUpdate.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

import org.xml.sax.helpers.DefaultHandler;

/**
 * This class represents an identity mMapping update message.
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
public class IdentityMappingUpdate extends DefaultHandler {
	//FIXME: remove this class and the one that extends it...
	/**
	 * The message id.
	 */
	protected pgrid.GUID mGUID = null;

	/**
	 * Timestamp of the update
	 */
	protected long mTimestamp;

	/**
	 * Signature
	 */
	protected String mSignature;

	/**
	 * Creates a new empty Query.
	 */
	protected IdentityMappingUpdate() {
	}

	/**
	 * Creates a new update mMapping for a given timestamp and signature. It is assumed that this peer
	 * is the initiator of the query and its address will be bound with the query as
	 * the destination for the result set.
	 *
	 * @param ts        timestamp
	 * @param signature signature
	 */
	public IdentityMappingUpdate(long ts, String signature) {
		this(new pgrid.GUID(), ts, signature);
	}

	/**
	 * Create a new id - ip mMapping update message
	 *
	 * @param guid        of the message
	 * @param signature   signature
	 */
	protected IdentityMappingUpdate(pgrid.GUID guid, long ts, String signature) {
		mGUID = guid;
		mTimestamp = ts;
		mSignature = signature;
	}

	/**
	 * Returns the message GUID.
	 *
	 * @return the message GUID.
	 */
	public pgrid.GUID getGUID() {
		return mGUID;
	}

}
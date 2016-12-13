/**
 * $Id: SimpleDataItem.java,v 1.1 2005/11/07 16:56:40 rschmidt Exp $
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
package test;

import p2p.basic.Key;
import p2p.basic.GUID;
import p2p.basic.Peer;
import p2p.storage.Type;
import pgrid.DataItem;
import pgrid.PGridHost;

/**
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
public abstract class SimpleDataItem extends DataItem implements p2p.storage.DataItem {

	/**
	 * Empty constructor
	 */
	public SimpleDataItem() {
	}

	/**
	 * Create a new IdentityDataItem
	 *
	 * @param guid      the unique id.
	 * @param type      the data type.
	 * @param key       the key for this file name.
	 * @param peer      the storing peer.
	 * @param data      the data.
	 */
	public SimpleDataItem(GUID guid, Type type, Key key, Peer peer, Object data) {
		super(type, PGridHost.getHost(((pgrid.GUID)peer.getGUID()), peer.getIP(), peer.getPort()), key, data.toString());
		setGUID(pgrid.GUID.getGUID(guid.toString()));
	}

	/**
	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
	 * as this object is less than, equal to, or greater than the specified object.
	 *
	 * @param obj the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
	 *         specified object.
	 */
	public int compareTo(Object obj, boolean withKey) {
		if (obj == null)
			return Integer.MIN_VALUE;
		if (obj.getClass() != this.getClass())
			return Integer.MIN_VALUE;
		DataItem item = (DataItem)obj;

		String localKey = "";
		String remoteKey = "";
		if (withKey) {
			localKey = COLON + mKey;
			remoteKey = COLON + item.getKey();
		}
		return (mHost.getGUID().toString() + AT + mData + COLON + localKey)
				.compareTo(item.getPeer().getGUID().toString() + AT + mData + COLON + remoteKey);
	}


	public int hashCode() {
		return (mHost.getGUID().toString() + AT + mData + COLON + mKey).hashCode();
	}

	/**
	 * Tests if the delivered item is equal to this.
	 *
	 * @param item the item to compare.
	 * @return ip address of the storing host.
	 */
	public boolean isEqual(SimpleDataItem item) {
		if (item == null)
			return false;
		if ((item.getType().equals(mType)) &&
				(item.getPeer().equals(mHost)) &&
				(item.getData().equals(mData)))
			return true;
		else
			return false;
	}

	/**
	 * @see pgrid.DataItem#getSignature()
	 */
	public String getSignature() {
		return getPeer().getGUID() + "\t" + getKey() + "\t" + getData();
	}

}

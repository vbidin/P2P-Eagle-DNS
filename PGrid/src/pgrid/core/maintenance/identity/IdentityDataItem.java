/**
 * $Id: IdentityDataItem.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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

import p2p.basic.Key;
import p2p.storage.Type;
import pgrid.DataItem;
import pgrid.PGridHost;

/**
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */
public abstract class IdentityDataItem extends DataItem {

	/**
	 * Public key of the remote host
	 */
	protected String mPublicKey = null;

	/**
	 * Encoded signature containing the ID, address, public key, and timestamp
	 */
	protected String mSignature = null;

	/**
	 * Timestamp of insertion. Used agains DoS attack
	 */
	protected long mTimeStamp;

	/**
	 * Signature separator
	 */
	public String SEPARATOR = "\t";

	/**
	 * Empty constructor
	 */
	public IdentityDataItem() {

	}

	/**
	 * Create a new IdentityDataItem
	 *
	 * @param host      the storing host.
	 * @param key       the key for this file name.
	 * @param publicKey The public key of the host
	 * @param timestamp The timestamp of the host
	 */
	public IdentityDataItem(Type type, PGridHost host, Key key, String publicKey, long timestamp, String desc) {
		super(type, host, key, desc);
		mPublicKey = publicKey;
		mTimeStamp = timestamp;
		mSignature = host.toString() + SEPARATOR + timestamp;

	}

	/**
	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
	 * as this object is less than, equal to, or greater than the specified object.
	 *
	 * @param obj the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
	 *         specified object.
	 */
	public int compareTo(Object obj) {
		if (obj == null)
			return Integer.MIN_VALUE;
		if (obj.getClass() != this.getClass())
			return Integer.MIN_VALUE;
		DataItem item = (DataItem)obj;
		return (mHost.getGUID().toString() + AT + mPublicKey + mSignature + mTimeStamp + COLON + mKey)
				.compareTo(item.getPeer().getGUID().toString() + AT + mPublicKey + mSignature + mTimeStamp + COLON + item.getKey());
	}


	public int hashCode() {
		return (mHost.getGUID().toString() + AT + mPublicKey + mSignature + mTimeStamp + COLON + mKey).hashCode();
	}

	/**
	 * Tests if the delivered item is equal to this.
	 *
	 * @param item the item to compare.
	 * @return ip address of the storing host.
	 */
	public boolean isEqual(IdentityDataItem item) {
		if (item == null)
			return false;
		if ((item.getType().equals(mType)) &&
				(item.getPeer().equals(mHost)) &&
				(item.getSignature().equals(mSignature)) &&
				(item.getPublicKey().equals(mPublicKey)) &&
				(item.getTimeStamp() == mTimeStamp))
			return true;
		else
			return false;
	}

	/**
	 * Returns the public key
	 *
	 * @return the public key
	 */
	public String getPublicKey() {
		return mPublicKey;
	}

	/**
	 * Returns the signature
	 *
	 * @return the signature
	 */
	public String getSignature() {
		return mSignature;
	}

	/**
	 * Set the signature
	 */
	public void setSignature(String signature) {
		mSignature = signature;
	}

	/**
	 * Returns the timestamp
	 *
	 * @return the timestamp
	 */
	public long getTimeStamp() {
		return mTimeStamp;
	}

	/**
	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
	 * as this object is less than, equal to, or greater than the specified object.
	 *
	 * @param obj     the Object to be compared.
	 * @param withKey if we should take the key in account
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
	 *         specified object.
	 */
	public int compareTo(Object obj, boolean withKey) {
		if (obj == null)
			return Integer.MIN_VALUE;
		if (obj.getClass() != this.getClass())
			return Integer.MIN_VALUE;
		IdentityDataItem item = (IdentityDataItem)obj;

		String localKey = "";
		String remoteKey = "";
		if (withKey) {
			localKey = COLON + mKey;
			remoteKey = COLON + item.getKey();
		}

		return (mHost.getGUID().toString() + AT + getSignature() + localKey).compareTo(item.getPeer().getGUID().toString() + AT + item.getSignature() + remoteKey);
	}

}

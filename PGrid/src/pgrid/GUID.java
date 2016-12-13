/**
 * $Id: GUID.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

package pgrid;

import java.util.WeakHashMap;

/**
 * This class stores unique IDs and provides some basic access methods.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class GUID extends pgrid.util.guid.GUID implements p2p.basic.GUID {

	/**
	 * The list of already created GUIDs. A weak hash map is use to lower the memory
	 * usage of P-Grid. The idea is as long as we have a living reference of the GUID
	 * we will have a cache for it. This will speed up the creation of GUID used by hosts.
	 * The cache system don't keep a reference to non permanent objects.
	 */
	protected static WeakHashMap mGUIDs = new WeakHashMap();

	/**
	 * Construct a new GUID.
	 * This constructor should only be used to create GUIDs, which were never used by other objects again.
	 */
	public GUID() {
		super();
	}

	/**
	 * Constructs an unique ID object from the given string.
	 * This constructor should only be used to create GUIDs, which were never used by other objects again.
	 *
	 * @param v a string representing a GUID
	 */
	public GUID(String v) {
		super(v);
	}

	/**
	 * Returns a GUID.
	 *
	 * @return the GUID.
	 */
	static public GUID getGUID() {
		GUID g = new GUID();
		//mGUIDs.put(g.toString(), g);
		return g;
	}

	/**
	 * Returns a GUID for the given string.
	 *
	 * @param guid the string representing the guid.
	 * @return the GUID.
	 */
	static public GUID getGUID(String guid) {
		GUID g = (GUID)mGUIDs.get(guid);
		if (g == null)
			g = new GUID(guid);
		//mGUIDs.put(guid, g);
		return g;
	}

	/**
	 * Returns the value of the unique ID.
	 *
	 * @return returns a byte array the represents the unique ID.
	 */
	public byte[] getBytes() {
		return super.getBytes();
	}

	/**
	 * Sets the value of the unique ID.
	 *
	 * @param newId the new value of the unique ID.
	 */
	public void setId(byte[] newId) {
		super.setId(newId);
	}

	/**
	 * Returns a unique string representation of this unique ID. The byte array
	 * that represents the unique ID is stepped through byte per byte and each
	 * byte is converted into its hex representation (padded with leading
	 * zeros).
	 *
	 * @return the unique string representation of the unique ID.
	 */
	public String toString() {
		return super.toString();
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
		return super.compareTo(obj);
	}

	/**
	 * Compares two unique IDs for equality. The result is <code>true</code> if
	 * and only if the argument is not null and is a <code>GUID</code> object
	 * that represents the same unique id as this object.
	 *
	 * @param obj the object to compare with.
	 * @return <code>true</code> if the objects are the same; false otherwise.
	 */
	public boolean equals(GUID obj) {
		return super.equals(obj);
	}

	/**
	 * Compares two unique IDs for equality. The result is <code>true</code> if
	 * and only if the argument is not null and is a <code>GUID</code> object
	 * that represents the same unique id as this object.
	 *
	 * @param obj the object to compare with.
	 * @return <code>true</code> if the objects are the same; false otherwise.
	 */
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	/**
	 * Returns a hash code value for this unique id based on its value.
	 *
	 * @return a hash code value for this unique id.
	 */
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Returns the size of this GUID.
	 *
	 * @return the size of this GUID.
	 */
	public int getSize() {
		return super.getSize();
	}


}
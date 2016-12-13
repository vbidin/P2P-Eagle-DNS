/**
 * $Id: Type.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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

/**
 * This class represents an unknown data item type.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Type implements p2p.storage.Type {

	/**
	 * The string representation of this data item type.
	 */
	public static final String TYPE_STRING = "unknown";

	/**
	 * The type string.
	 */
	private String mTypeString = null;

	/**
	 * Creates a new unknown type with type string "unknown".
	 */
	public Type() {
		mTypeString = TYPE_STRING;
	}

	/**
	 * Creates a new unknown type with the given type string.
	 *
	 * @param typeString the type string.
	 */
	public Type(String typeString) {
		mTypeString = typeString;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param obj the reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj.getClass() != Type.class)
			return false;
		return toString().equals(obj.toString());
	}

	/**
	 * Returns a hash code value for the data type.
	 *
	 * @return a hash code value for the data type.
	 */
	public int hashCode() {
		return mTypeString.hashCode();
	}

	/**
	 * Returns a string representation of the data type.
	 *
	 * @return a string representation of the data type.
	 */
	public String toString() {
		return mTypeString;
	}

}

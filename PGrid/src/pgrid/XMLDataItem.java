/**
 * $Id: XMLDataItem.java,v 1.2 2005/11/07 16:56:35 rschmidt Exp $
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
 * This interface represents an XMLDataItem
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 */
public interface XMLDataItem extends XMLizable {

	/**
	 * A part of the XML string.
	 */
	public static final String XML_DATA_ITEM = "DataItem";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_DATA_ITEM_DATA = "Data";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_DATA_ITEM_GUID = "GUID";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_DATA_ITEM_KEY = "Key";

	/**
	 * A part of the XML string.
	 */
	public static final String XML_DATA_ITEM_TYPE = "Type";

	/**
	 * Returns a string represantation of this result set.
	 *
	 * @param prefix    a string prefix for each line.
	 * @param newLine   the string for a new line, e.g. \n.
	 * @param signature add a signature
	 * @return a string represantation of this result set.
	 */
	public String toXMLString(String prefix, String newLine, boolean signature);

}
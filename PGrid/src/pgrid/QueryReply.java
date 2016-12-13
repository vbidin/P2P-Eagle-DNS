/**
 * $Id: QueryReply.java,v 1.2 2005/11/07 16:56:34 rschmidt Exp $
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

import p2p.basic.GUID;

import java.util.Collection;
import java.util.Vector;

import pgrid.util.LexicalDefaultHandler;

/**
 * This class represents a query reply.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class QueryReply extends LexicalDefaultHandler {

	/**
	 * The Query reply type Bad Request.
	 */
	public static final int TYPE_BAD_REQUEST = 2;

	/**
	 * The Query reply type File Not Found.
	 */
	public static final int TYPE_NOT_FOUND = 1;

	/**
	 * The Query reply type OK.
	 */
	public static final int TYPE_OK = 0;

	/**
	 * The Query Id.
	 */
	protected p2p.basic.GUID mGUID = null;

	/**
	 * The result set.
	 */
	protected Collection mResultSet = null;

	/**
	 * The Query reply.
	 */
	protected int mType = -1;

	/**
	 * Creates an empty query reply.
	 */
	public QueryReply() {
	}

	/**
	 * Creates a new query reply with given values.
	 *
	 * @param guid      the GUID of the Query Reply.
	 * @param type      the type of query reply.
	 * @param resultSet the result set of found files.
	 */
	public QueryReply(GUID guid, int type, Collection resultSet) {
		mGUID = guid;
		mType = type;
		mResultSet = resultSet;
	}

	/**
	 * Returns the number of hits.
	 *
	 * @return the number of hits.
	 */
	public int getHits() {
		if (mResultSet == null)
			return 0;
		return mResultSet.size();
	}

	/**
	 * Returns the query id.
	 *
	 * @return the query id.
	 */
	public GUID getGUID() {
		return mGUID;
	}

	/**
	 * Returns a result of the result set, selected by the index.
	 *
	 * @param index the index of the result in the result set.
	 * @return the result.
	 */
	public DataItem getResult(int index) {
		return (DataItem)new Vector(mResultSet).get(index);
	}

	/**
	 * Returns the result set.
	 *
	 * @return the result set.
	 */
	public Collection getResultSet() {
		return mResultSet;
	}

	/**
	 * Returns the query reply type.
	 *
	 * @return the query reply type.
	 */
	public int getType() {
		return mType;
	}

}
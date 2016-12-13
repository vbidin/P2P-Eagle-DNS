/**
 * $Id: ConnectionClosedException.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

package pgrid.network;

/**
 * The SessionClosedException is thrown when it is tryed to read or write
 * to the in/output stream, but the connection is already closed.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class ConnectionClosedException extends Exception {

	/**
	 * Constructs a SessionClosedException without any detail message.
	 */
	public ConnectionClosedException() {
		super();
	}

	/**
	 * Constructs a SessionClosedException with the specified detail message.
	 *
	 * @param errorMessage string that should be shown in the exception's output.
	 */
	public ConnectionClosedException(String errorMessage) {
		super(errorMessage);
	}

}
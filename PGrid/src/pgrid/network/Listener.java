/**
 * $Id: Listener.java,v 1.2 2005/11/07 16:56:37 rschmidt Exp $
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

import pgrid.Constants;
import pgrid.interfaces.basic.PGridP2P;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.logging.Level;

/**
 * The communication listener accepts new incoming connections from remote
 * hosts.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
class Listener implements Runnable {

	/**
	 * The listen flag.
	 */
	private boolean listening = true;

	/**
	 * The PGridP2P facility.
	 */
	private PGridP2P mPGridP2P = PGridP2P.sharedInstance();

	/**
	 * The socket to listen.
	 */
	private ServerSocket mSocket;

	/**
	 * Creates a new listener.
	 */
	public Listener() {
	}

	/**
	 * Starts the listener. A new socket is created at a definied port (config
	 * facility).
	 */
	public void run() {
		int port = mPGridP2P.getLocalHost().getPort();
		while (true) {
			try {
				mSocket = new ServerSocket(port);
			} catch (BindException e) {
				Constants.LOGGER.warning("Port " + String.valueOf(mPGridP2P.getLocalHost().getPort()) + " is already used by another application!");
				System.exit(-1);
				continue;
			} catch (IOException e) {
				Constants.LOGGER.log(Level.SEVERE, null, e);
				System.exit(-1);
			}
			break;
		}
		
		ConnectionManager connMgr = ConnectionManager.sharedInstance();
		Constants.LOGGER.info("start listening for incoming connections at port " + String.valueOf(mPGridP2P.getLocalHost().getPort()) + " ...");
		while (listening) {
			try {
				connMgr.accept(mSocket.accept());
			} catch (IOException e) {
				Constants.LOGGER.log(Level.WARNING, null, e);
				listening = false;
			}
		}
		try {
			mSocket.close();
		} catch (IOException e) {
			Constants.LOGGER.log(Level.SEVERE, null, e);
			System.exit(-1);
		}
	}

}
/**
 * $Id: SearchManager.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
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

package pgrid.core.search;

import p2p.basic.events.NoRouteToKeyException;
import p2p.storage.events.NoSuchTypeException;
import p2p.storage.events.SearchListener;
import pgrid.*;
import pgrid.core.storage.StorageManager;
import pgrid.interfaces.basic.PGridP2P;

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

/**
 * This class maintains a list of queries and of those listeners that are
 * interested in the results of these queries.
 *
 * @author <a href="mailto:Tim van Pelt <tim@vanpelt.com>">Tim van Pelt</a> &amp;
 *         <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class SearchManager extends pgrid.util.WorkerThread {

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	private static final SearchManager SHARED_INSTANCE = new SearchManager();

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager;

	/**
	 * The P-Grid P2P facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * The handler for received searches the local peer is not responsible for.
	 */
	private RemoteRequestHandler mRemoteRequestHandler = null;

	/**
	 * The handler for received searches the local peer is responsible for.
	 */
	private RemoteSearchHandler mRemoteSearchHandler = null;

	/**
	 * The search requests.
	 */
	private final Vector mRequests = new Vector();

	private Thread mThread = null;

	/**
	 * The constructor must be protected to ensure that only subclasses can
	 * call it and that only one instance can ever get created. A client that
	 * tries to instantiate the search manager directly will get an error at
	 * compile-time.
	 */
	protected SearchManager() {
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static SearchManager sharedInstance() {
		return SHARED_INSTANCE;
	}

	
	protected void handleError(Throwable t) {
		if (t instanceof InterruptedException) {
			Constants.LOGGER.finer("Searcher manager interupted.");
		} else {
			Constants.LOGGER.log(Level.WARNING, "Error in Search thread", t);
			t.printStackTrace();
		}
	}

	/**
	 * Initializes the search manager.
	 */
	public void init() {
		mPGridP2P = PGridP2P.sharedInstance();
		mStorageManager = mPGridP2P.getStorageManager();

		mRemoteRequestHandler = new RemoteRequestHandler();
		mRemoteSearchHandler = new RemoteSearchHandler(mPGridP2P);
	}

	protected boolean isCondition() {
		return !mRequests.isEmpty();
	}

	protected void prepareWorker() throws Exception {
		mThread = Thread.currentThread();
		Constants.LOGGER.config("Search thread prepared.");
	}

	protected void releaseWorker() throws Exception {
		Constants.LOGGER.config("Search thread released.");
	}

	/**
	 * Invoked when a new query message was received.
	 *
	 * @param query the query message.
	 */
	public void remoteSearch(AbstractQuery query, PGridHost remoteHost) {
		//@todo maybe check if the query was routed correctly by comparing the amount of matching bits with the index
		// todo Modify the implementation to send an ACK immediately (processing)
		SearchRequest request = null;
        // check if local peer is responsible for query
        if (query.isHostResponsible(mPGridP2P.getLocalHost())) {
            // register the search locally
            mRemoteSearchHandler.register(query, remoteHost);
			request = SearchRequestFactory.createSearchRequest(query, mRemoteSearchHandler);
		} else {
			mRemoteRequestHandler.register(query, remoteHost);
			request = SearchRequestFactory.createSearchRequest(query, mRemoteRequestHandler);
        }
		mRequests.add(request);
		Constants.LOGGER.finest("Search request for query ("+query.getGUID().toString()+") added.");

		broadcast();
	}

	/**
	 * Search the network for matching items. Implemented as
	 * an asynchronous operation, because search might take
	 * some time. Callback is notified for each new result.
	 *
	 * @param query    the query used to specify the search
	 * @param listener an object to notify when results arrive
	 * @throws p2p.storage.events.NoSuchTypeException if the provided Type is unknown.
   * @throws p2p.basic.events.NoRouteToKeyException if the query cannot be routed to a responsible peer.
	 */
	public void search(p2p.storage.Query query, SearchListener listener) throws NoSuchTypeException, NoRouteToKeyException {
		SearchRequest request = SearchRequestFactory.createSearchRequest(query, listener);

		mRequests.add(request);
		broadcast();
	}

	protected void work() throws Exception {
		Iterator requests = null;
		synchronized(mRequests) {
			requests = ((Vector)mRequests.clone()).iterator();
			mRequests.clear();
		}

		while(requests.hasNext()) {
				((SearchRequest)requests.next()).handleSearch();
			}
	}

	/**
	 * Shutdown
	 */
	public void shutdown() {
		mThread.interrupt();
	}

}
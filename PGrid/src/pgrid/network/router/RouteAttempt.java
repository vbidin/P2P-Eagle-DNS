package pgrid.network.router;

import p2p.basic.Message;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: pgrid.helper
 * Date: 15.06.2005
 * Time: 15:26:24
 * To change this template use File | Settings | File Templates.
 */
class RouteAttempt {

	private Collection mCollection = null;

	private Iterator mIterator = null;

	private Message mMessage = null;

	private long mStartTime = 0;

	private MessageWaiter mWaiter = null;

	public RouteAttempt(Message msg, Collection col, Iterator it, MessageWaiter waiter) {
		mMessage = msg;
		mCollection = col;
		mIterator = it;
		mWaiter = waiter;
		mStartTime = System.currentTimeMillis();
	}

	public Collection getCollection() {
		return mCollection;
	}

	public Iterator getIterator() {
		return mIterator;
	}

	public Message getMessage() {
		return mMessage;
	}

	public MessageWaiter getWaiter() {
		return mWaiter;
	}

	public long getStartTime() {return mStartTime;}

}

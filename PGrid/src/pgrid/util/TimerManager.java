package pgrid.util;

import pgrid.util.TimerListener;

import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Collections;
import java.util.Comparator;

/**
 * This class is a timer manager. It use a single thread to manager all timers
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

public class TimerManager {
	/**
	 * Lock object
	 */
	private final Object mLock = new Object();

	/**
	 * A sorted list of timer listener
	 */
	private SortedSet mTimerListner;

	/**
	 * The timer thread
	 */
	private Thread mTimerThread;

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	private static final TimerManager SHARED_INSTANCE = new TimerManager();

	protected TimerManager() {
		mTimerListner = Collections.synchronizedSortedSet(new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				return (int)(((TimerElement)o1).mNextTimeout - ((TimerElement)o2).mNextTimeout);
			}
		}));

		mTimerThread = new Thread(new TimerManager.Timer());
		mTimerThread.setDaemon(true);
		mTimerThread.start();
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static TimerManager sharedInstance() {
		return SHARED_INSTANCE;
	}

	/**
	 * Register a timer. As soons as the the time out occurs, the listener
	 * will be informed. The object id will be given in parameter.
	 *
	 * @param timeout 	before the listener will be informed
	 * @param id		User dependent ID. It could be null
	 * @param listener  to inform
	 * @param periodic	if true the timer will be reconducted automatically
	 */
	public void register(long timeout, Object id, TimerListener listener, boolean periodic) {
		TimerElement te = new TimerElement(timeout, id, listener, periodic);
		synchronized(mLock) {
			//Add the new timer element
			mTimerListner.add(te);
			if (mTimerListner.first().equals(te)) {
				// The new TE should be the next triggered element
				mLock.notifyAll();
			}
		}

	}

	/**
	 * a timer object
	 */
	class TimerElement {
		public long mTimeout;
		public long mNextTimeout;
		public Object mID;
		public pgrid.util.TimerListener mListener;
		public boolean mPeriodic;

		public TimerElement(long timout, Object id, TimerListener listener, boolean periodic) {
			mTimeout = timout;
			mNextTimeout = mTimeout + System.currentTimeMillis();
			mID = id;
			mListener = listener;
			mPeriodic = periodic;
		}
	}

	/**
	 * the timer thread
	 */
	class Timer implements Runnable {

		/**
		 * When an object implementing interface <code>Runnable</code> is used
		 * to create a thread, starting the thread causes the object's
		 * <code>run</code> method to be called in that separately executing
		 * thread.
		 * <p/>
		 * The general contract of the method <code>run</code> is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		public void run() {
			try {
				long currentTime;
				long tmp;
				TimerElement te;

				while (true) {
					synchronized(mLock) {
						if (mTimerListner.isEmpty()) mLock.wait();
						else {
							tmp = ((TimerElement)mTimerListner.first()).mNextTimeout-System.currentTimeMillis();
							if (tmp > 0)
								mLock.wait(tmp);
						}

						// trigger function that should be triggered
						currentTime = System.currentTimeMillis();
						while (!mTimerListner.isEmpty() && (te = ((TimerElement)mTimerListner.first())).mNextTimeout <= currentTime) {
							mTimerListner.remove(te);
							te.mListener.timerTriggered(te.mID);

							if (te.mPeriodic) {
								te.mNextTimeout = currentTime+te.mTimeout;
								mTimerListner.add(te);
							}
						}
					}

				}
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}


}

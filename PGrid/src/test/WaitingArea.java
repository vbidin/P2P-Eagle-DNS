package test;

/**
 * Utility class to suspend threads.
 *
 * @author A. Nevski
 */
public class WaitingArea {

	/**
	 * Suspends current thread indefinitely or until jvm shutdown,
	 * for example following a Ctrl-C signals.
	 */
	public static void waitTillSignal(int time) {
		Object sync = new Object();
		synchronized (sync) {
			try {
				if (time < 0)
					sync.wait();
				else
					sync.wait(time);
			} catch (InterruptedException exc) {
			}
		}
	}
}

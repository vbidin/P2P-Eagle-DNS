package pgrid.util;

/**
 * This listener is called by the TimerManager when a timer hits his timeout
 *
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 * @version 1.0.0
 */

public interface TimerListener {
	public void timerTriggered(Object id);
}

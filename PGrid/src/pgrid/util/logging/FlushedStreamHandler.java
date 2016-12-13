package pgrid.util.logging;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * This class extends the {@link java.util.logging.StreamHandler} by flushing the stream after every published
 * {@link java.util.logging.LogRecord}.
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0 2003/03/14
 * @see java.util.logging.StreamHandler
 */
public class FlushedStreamHandler extends StreamHandler {

	/**
	 * Create a </code>FlushedStreamHandler</code>, with no current output stream.
	 */
	public FlushedStreamHandler() {
		super();
	}

	/**
	 * Create a </code>FlushedStreamHandler</code> with a given <code>Formatter</code> and output stream.
	 *
	 * @param out       the target output stream.
	 * @param formatter Formatter to be used to format output.
	 */
	public FlushedStreamHandler(OutputStream out, Formatter formatter) {
		super(out, formatter);
	}

	/**
	 * Format, publish, and flushes a <code>LogRecord</code>.
	 *
	 * @param record description of the log event.
	 * @see java.util.logging.StreamHandler
	 */
	public synchronized void publish(LogRecord record) {
		super.publish(record);
		super.flush();
	}

}

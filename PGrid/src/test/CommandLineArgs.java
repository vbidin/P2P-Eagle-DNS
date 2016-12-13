package test;

import pgrid.interfaces.basic.PGridP2P;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Utility class extracts the application launch parameters from the
 * command line. The expected format is the following:
 * <i>application local_port bootstrap_host_address boostrap_host_port</i>.
 * All accessors throw RuntimeException if an unexpected format is
 * found, after displaying the usage information on the stderr output.
 * These parameters are needed to initialize the peer-to-peer layer.
 *
 * @author A. Nevski
 */
public class CommandLineArgs {
	private String[] args;

	/**
	 * Creates a instance.
	 *
	 * @param args the command line arguments whose values accessors expose
	 */
	public CommandLineArgs(String[] args) {
		this.args = args;
	}

	/**
	 * Get the Internet address.
	 *
	 * @return the address from the command line arguments
	 */
	public InetAddress getAddress() {
		try {
			return InetAddress.getByName(args[1]);
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Invalid number of arguments");
			printUsage();
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			System.err.println("Address is not valid");
			printUsage();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the service port number.
	 *
	 * @return the port number from the command line arguments
	 */
	public int getPort() {
		try {
			return Integer.parseInt(args[2]);
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Invalid number of arguments");
			printUsage();
			throw new RuntimeException(e);
		} catch (NumberFormatException e) {
			System.err.println("Could not determine remote port number");
			printUsage();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the local service port number to use.
	 *
	 * @return a Properties instance with a LocalPort property
	 */
	public Properties getOtherProperties() {
		try {
			Properties p = new Properties();
			int localPort = Integer.parseInt(args[0]);
			p.setProperty(PGridP2P.PROP_LOCAL_PORT, String.valueOf(localPort));
			return p;
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Invalid number of arguments");
			printUsage();
			throw new RuntimeException(e);
		} catch (NumberFormatException e) {
			System.err.println("Could not determine local port number");
			printUsage();
			throw new RuntimeException(e);
		}
	}

	private void printUsage() {
		System.err.println("Usage: application local_port "
				+ " bootstrap_host_address boostrap_host_port");
	}
}

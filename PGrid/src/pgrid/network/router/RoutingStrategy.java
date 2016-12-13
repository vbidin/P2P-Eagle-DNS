package pgrid.network.router;

import pgrid.network.router.Router;
import pgrid.network.router.Request;

/**
 * Created by IntelliJ IDEA.
 * User: john
 * Date: Aug 9, 2005
 * Time: 6:28:49 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class RoutingStrategy {

	/**
	 * Link to the router object
	 */
	protected Router mRouter;

	/**
	 * default constructor
	 * @param router
	 */
	public RoutingStrategy(Router router) {
		mRouter = router;
	}

	/**
	   * Routes a message to the responsible peer.
	   *
	   * @param req
	 */
	  public abstract void route(Request req) ;

	/**
	 * Retruns the name of the strategy. This name must be unique
	 * @return name of the strategy
	 */
	public abstract String getStrategyName();

	/**
	 * Returns the router associated with this strategy
	 * @return the router object
	 */
	public Router getRouter() {
		return mRouter;
	}
}

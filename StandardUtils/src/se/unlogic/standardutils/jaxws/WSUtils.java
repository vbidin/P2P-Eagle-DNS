package se.unlogic.standardutils.jaxws;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import se.unlogic.standardutils.time.MillisecondTimeUnits;


public class WSUtils {

	public static void setTimeouts(BindingProvider bindingProvider, int connectionTimeout, int requestTimeout){
		
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		
		requestContext.put("com.sun.xml.internal.ws.connect.timeout", connectionTimeout * MillisecondTimeUnits.SECOND);		
		requestContext.put("com.sun.xml.internal.ws.request.timeout", requestTimeout * MillisecondTimeUnits.SECOND);
	}
	
	public static void setUsernamePassword(BindingProvider bindingProvider, String username, String password){
		
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		
		requestContext.put(BindingProvider.USERNAME_PROPERTY, username);		
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);
	}
	
	public static void setEndpoint(BindingProvider bindingProvider, String url){
		
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
	}
	
	public static void setMaintainSession(BindingProvider bindingProvider, boolean value){
		
		bindingProvider.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, value);
	}
}

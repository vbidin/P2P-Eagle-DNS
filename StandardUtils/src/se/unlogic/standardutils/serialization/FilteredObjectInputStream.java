package se.unlogic.standardutils.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;


public class FilteredObjectInputStream extends ObjectInputStream {

	private final List<String> allowedClasses;

	public FilteredObjectInputStream(InputStream in, Class<?>... allowedClasses) throws IOException {

		super(in);
	
		if(allowedClasses == null){
			
			throw new NullPointerException("Allowed classes cannot be null");
		}
		
		this.allowedClasses = new ArrayList<String>(this.allowedClasses.size());
		
		for(Class<?> clazz : allowedClasses){
			
			this.allowedClasses.add(clazz.getName());
		}
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

		if(!allowedClasses.contains(desc.getName())){
			
			throw new InvalidClassException(desc.getName(), "Unallowed class detected");
		}
		
		return super.resolveClass(desc);
	}
}

package org.vitrivr.cineast.core.importer;

import java.util.Map;
import java.util.Set;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;

/**
 * 
 * Copies data from an {@link Importer} to a {@link PersistencyWriter}
 *
 */
public class Copier {

	private final String entityName;
	private final Importer<?> importer;
	private final PersistencyWriter<?> writer;
	
	public Copier(String entityName, Importer<?> importer){
		this(entityName, importer, Config.getDatabaseConfig().getWriterSupplier().get());
	}
	
	public Copier(String entityName, Importer<?> importer, PersistencyWriter<?> writer){
		this.entityName = entityName;
		this.importer = importer;
		this.writer = writer;
	}
	
	public void copy(){
		Map<String, PrimitiveTypeProvider> map = this.importer.readNextAsMap();
		
		if(map == null){
			return;
		}
		
		Set<String> keyset = map.keySet();
		String[] names = new String[keyset.size()];
		
		int i = 0;
		for(String name : keyset){
			names[i++] = name;
		}
		
		this.writer.open(entityName);
		this.writer.setFieldNames(names);
		
		Object[] objects = new Object[names.length];
		
		do{
			for(i = 0; i < names.length; ++i){
				objects[i] = map.get(names[i]);
			}
			@SuppressWarnings("rawtypes")
			PersistentTuple tuple = this.writer.generateTuple(objects);
			this.writer.persist(tuple); //TODO use batched operation
		}while((map = this.importer.readNextAsMap()) != null);
		
		this.writer.close();
		
	}

	@Override
	protected void finalize() throws Throwable {
		this.writer.close();
		super.finalize();
	}
	
}
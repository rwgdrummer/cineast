package org.vitrivr.cineast.core.db.dao.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.db.DBSelector;

public class MultimediaObjectLookup {

  private static final Logger LOGGER = LogManager.getLogger();

  private final DBSelector selector;

  public MultimediaObjectLookup() {
    this.selector = Config.getDatabaseConfig().getSelectorSupplier().get();
    this.selector.open(MultimediaObjectDescriptor.ENTITY);
  }

  public MultimediaObjectDescriptor lookUpObjectById(String objectId) {
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(MultimediaObjectDescriptor.FIELDNAMES[0], objectId);

    if (result.isEmpty()) {
      return new MultimediaObjectDescriptor();
    }

    return mapToDescriptor(result.get(0));
  }

  private MultimediaObjectDescriptor mapToDescriptor(Map<String, PrimitiveTypeProvider> map) {
    PrimitiveTypeProvider idProvider = map.get(MultimediaObjectDescriptor.FIELDNAMES[0]);
    PrimitiveTypeProvider typeProvider = map.get(MultimediaObjectDescriptor.FIELDNAMES[1]);
    PrimitiveTypeProvider nameProvider = map.get(MultimediaObjectDescriptor.FIELDNAMES[2]);
    PrimitiveTypeProvider pathProvider = map.get(MultimediaObjectDescriptor.FIELDNAMES[3]);


    if (!checkProvider(MultimediaObjectDescriptor.FIELDNAMES[0], idProvider, ProviderDataType.STRING)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider(MultimediaObjectDescriptor.FIELDNAMES[1], typeProvider, ProviderDataType.INT)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider(MultimediaObjectDescriptor.FIELDNAMES[2], nameProvider, ProviderDataType.STRING)) {
      return new MultimediaObjectDescriptor();
    }

    if (!checkProvider(MultimediaObjectDescriptor.FIELDNAMES[3], pathProvider, ProviderDataType.STRING)) {
      return new MultimediaObjectDescriptor();
    }



    return new MultimediaObjectDescriptor(idProvider.getString(), nameProvider.getString(), pathProvider.getString(), MediaType.fromId(typeProvider.getInt()), true);

  }

  private boolean checkProvider(String name, PrimitiveTypeProvider provider,
      ProviderDataType expectedType) {
    if (provider == null) {
      LOGGER.error("no {} in multimedia object", name);
      return false;
    }

    if (provider.getType() != expectedType) {
      LOGGER.error("invalid data type for field {} in multimedia object, expected {}, got {}", name,
          expectedType, provider.getType());
      return false;
    }
    return true;
  }

  public MultimediaObjectDescriptor lookUpObjectByName(String name) {
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(MultimediaObjectDescriptor.FIELDNAMES[2], name);

    if (result.isEmpty()) {
      return new MultimediaObjectDescriptor();
    }

    return mapToDescriptor(result.get(0));
  }

  public Map<String, MultimediaObjectDescriptor> lookUpVideos(String... videoIds) {
    if (videoIds == null || videoIds.length == 0) {
      return new HashMap<>();
    }

    HashMap<String, MultimediaObjectDescriptor> _return = new HashMap<>();

    List<Map<String, PrimitiveTypeProvider>> results = selector.getRows(MultimediaObjectDescriptor.FIELDNAMES[0], videoIds);

    if (results.isEmpty()) {
      return new HashMap<>();
    }

    for (Map<String, PrimitiveTypeProvider> map : results) {
      MultimediaObjectDescriptor d = mapToDescriptor(map);
      _return.put(d.getObjectId(), d);
    }

    return _return;

  }

  public Map<String, MultimediaObjectDescriptor> lookUpVideos(Iterable<String> videoIds) {
    if (videoIds == null) {
      return new HashMap<>();
    }

    HashMap<String, MultimediaObjectDescriptor> _return = new HashMap<>();

    List<Map<String, PrimitiveTypeProvider>> results = selector.getRows(MultimediaObjectDescriptor.FIELDNAMES[0], videoIds);

    if (results.isEmpty()) {
      return new HashMap<>();
    }

    for (Map<String, PrimitiveTypeProvider> map : results) {
      MultimediaObjectDescriptor d = mapToDescriptor(map);
      _return.put(d.getObjectId(), d);
    }

    return _return;

  }

  public void close() {
    this.selector.close();
  }

  @Override
  protected void finalize() throws Throwable {
    this.close();
    super.finalize();
  }

  public List<MultimediaObjectDescriptor> getAllVideos() {
    DBSelector selector = Config.getDatabaseConfig().getSelectorSupplier().get();
    selector.open(MultimediaObjectDescriptor.ENTITY);
    List<Map<String, PrimitiveTypeProvider>> all = selector.getAll();
    List<MultimediaObjectDescriptor> _return = new ArrayList<>(all.size());
    for (Map<String, PrimitiveTypeProvider> map : all) {
      _return.add(mapToDescriptor(map));
    }
    return _return;
  }

  public List<String> lookUpVideoIds() {
    DBSelector selector = Config.getDatabaseConfig().getSelectorSupplier().get();
    selector.open(MultimediaObjectDescriptor.ENTITY);
    List<PrimitiveTypeProvider> ids = selector.getAll(MultimediaObjectDescriptor.FIELDNAMES[0]);
    Set<String> uniqueIds = new HashSet<>();
    for (PrimitiveTypeProvider l : ids) {
      uniqueIds.add(l.getString());
    }
    selector.close();

    List<String> multimediaobjectIds = new ArrayList<>();
    for (String id : uniqueIds) {
      multimediaobjectIds.add(id);
    }

    return multimediaobjectIds;
  }

}
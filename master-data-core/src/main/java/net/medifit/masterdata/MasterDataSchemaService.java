package net.medifit.masterdata;

import com.fasterxml.jackson.core.TreeNode;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import net.medifit.masterdata.schema.MasterDataSchema;

import java.io.InputStream;
import java.net.URL;

public interface MasterDataSchemaService {

  Uni<MasterDataSchema> getById(String id);

  Uni<MasterDataSchema> getByGroupType(String group, String type);

  Uni<MasterDataSchema> storeJsonSchema(
      String group, String type, InputStream schemaStream, String defaultPrefix);

  Uni<MasterDataSchema> storeJsonSchema(String group, String type, URL url, String defaultPrefix);

  Uni<Boolean> deleteJsonSchema(String id);

  Multi<MasterDataSchema> list();

  Uni<String> storeJsonData(
          String group, String type, InputStream schemaStream);

  Uni<TreeNode> getJsonData(
          String group, String type, String id);

}

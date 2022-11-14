package net.medifit.masterdata.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MasterDataSchema {
  private String uuid;
  private String type;
  private String group;
  private String jsonSchemaS3Key;
  private String jsonSchemaUrl;
  private String defaultPrefix;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
}

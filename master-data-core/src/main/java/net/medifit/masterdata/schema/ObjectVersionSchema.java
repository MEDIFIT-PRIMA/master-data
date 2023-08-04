package net.medifit.masterdata.schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;
import static com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;

@Data
public class ObjectVersionSchema {
    String eTag;
    Long size;
    String storageClass;
    String key;
    String versionId;
    Boolean isLatest;
    @JsonFormat(without = {ADJUST_DATES_TO_CONTEXT_TIME_ZONE, WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS})
    OffsetDateTime lastModified;
    String owner;
   String id;

    public static ObjectVersionSchema fromObjectVersion(final ObjectVersion v) {
        final ObjectVersionSchema s = new ObjectVersionSchema();
        s.eTag = v.eTag();
        s.size = v.size();
        s.storageClass= v.storageClassAsString();
        s.key=v.key();
        s.versionId = v.versionId();
        s.isLatest = v.isLatest();
        s.lastModified=v.lastModified().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        s.owner=v.owner().displayName();
        s.id=v.owner().id();
        return s;
    }
}

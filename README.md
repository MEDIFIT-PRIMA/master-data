# master-data

## run quarkus dev

- make sure local elasticsearch and minio containers are up and running
- run ```mvn quarkus:dev```

## create docker runtime image
```shell
      mvn clean package \
      -Dquarkus.container-image.build=true \
      -Dquarkus.container-image.group=openepcis \
      -Dquarkus.container-image.registry=docker-registry.company-group.com \
      -Dquarkus.container-image.additional-tags=latest \
      -Dquarkus.jib.base-jvm-image=openjdk:17-jdk
```

## run image locally
```shell
      docker run --rm -ti --name masterdata \
        --network=openepcis-net -p 8080:8080 \
        -e "QUARKUS_PROFILE=docker" \
        -e "QUARKUS_OPENSEARCH_HOSTS=openepcis-opensearch-01:9200" \
        -e "S3_ENDPOINT_OVERRIDE=http://openepcis-minio:9000" \
        docker-registry.company-group.com/openepcis/master-data-quarkus-rest:1.0.0-SNAPSHOT
```
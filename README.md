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
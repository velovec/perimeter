FROM @docker.baseImage@

ENTRYPOINT ["java", "-jar", "/opt/perimeter/perimeter-configurator.jar", "--spring.config.location=/opt/perimeter/perimeter-configurator.yml"]
WORKDIR "/opt/perimeter"

RUN apk update && \
    apk add python go bash && \
    rm -rf /var/cache/apk/*

COPY classes/application.yml /opt/perimeter/perimeter-configurator.yml
COPY @project.artifactId@-@project.version@.jar /opt/perimeter/perimeter-configurator.jar

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"
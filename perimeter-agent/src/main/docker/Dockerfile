FROM @docker.baseImage@

ENTRYPOINT ["java", "-jar", "/opt/perimeter/perimeter-agent.jar", "--spring.config.location=/opt/perimeter/perimeter-agent.yml"]
WORKDIR "/opt/perimeter"

RUN apk update && \
    apk add libpcap python go bash && \
    rm -rf /var/cache/apk/*

COPY classes/application.yml /opt/perimeter/perimeter-agent.yml
COPY @project.artifactId@-@project.version@.jar /opt/perimeter/perimeter-agent.jar

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"
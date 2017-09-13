FROM @docker.baseImage@

ENTRYPOINT ["java", "-jar", "/opt/perimeter/perimeter.jar", "--spring.config.location=/opt/perimeter/perimeter.yml"]
WORKDIR "/opt/perimeter"
EXPOSE 8080

COPY application.yml /opt/perimeter/perimeter.yml
COPY @project.artifactId@-@project.version@.jar /opt/perimeter/perimeter.jar

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"
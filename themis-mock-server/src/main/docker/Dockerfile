FROM @docker.baseImage@

ENTRYPOINT ["java", "-jar", "/opt/themis/themis.jar", "--spring.config.location=/opt/themis/themis.yml"]
WORKDIR "/opt/themis"
EXPOSE 5000

COPY classes/application.yml /opt/themis/themis.yml
COPY @project.artifactId@-@project.version@.jar /opt/themis/themis.jar

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"
FROM @docker.baseImage@

ENTRYPOINT ["java", "-jar", "/opt/perimeter/perimeter.jar", "--spring.config.location=classpath:application.yml,/opt/perimeter/perimeter.yml"]
WORKDIR "/opt/perimeter"
EXPOSE 8080 1488

RUN mkdir -p /opt/perimeter/sshd
COPY classes/application.yml /opt/perimeter/perimeter.yml
COPY @project.artifactId@-@project.version@.jar /opt/perimeter/perimeter.jar

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"
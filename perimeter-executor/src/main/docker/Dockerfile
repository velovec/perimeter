FROM @docker.baseImage@

ENTRYPOINT ["java", "-jar", "/opt/perimeter/perimeter-executor.jar", "--spring.config.location=classpath:application.yml,/opt/perimeter/perimeter-executor.yml"]
WORKDIR "/opt/perimeter"

RUN apt update && \
    apt upgrade -qy && \
    apt dist-upgrade -qy && \
    apt install -qy golang openjdk-8-jdk python python-pip python-requests && \
    rm -rf /var/cache/apt/*

COPY classes/application.yml /opt/perimeter/perimeter-executor.yml
COPY @project.artifactId@-@project.version@.jar /opt/perimeter/perimeter-executor.jar

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"
FROM golang AS build

COPY *.go /usr/src/judas/
WORKDIR /usr/src/judas

RUN go build -o judas -ldflags "-linkmode external -extldflags -static" *.go

FROM @docker.baseImage@

ENTRYPOINT ["java", "-jar", "/opt/perimeter/perimeter-judas.jar", "--spring.config.location=classpath:application.yml,/opt/perimeter/perimeter-judas.yml"]
WORKDIR "/opt/perimeter"

RUN apt update && \
    apt upgrade -qy && \
    apt dist-upgrade -qy && \
    apt install -qy procps && \
    rm -rf /var/cache/apt/* && \
    go get -u "github.com/pkg/errors"

COPY --from=build /usr/src/judas/judas /opt/perimeter/judas
COPY classes/application.yml /opt/perimeter/perimeter-judas.yml
COPY @project.artifactId@-@project.version@.jar /opt/perimeter/perimeter-judas.jar

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"

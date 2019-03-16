FROM golang:latest as builder
WORKDIR /go/src/perimeter_exporter
COPY . .
RUN go get -d -v ./...
RUN go install -v ./...
RUN go build .


FROM ubuntu:latest
EXPOSE 9900
COPY --from=builder /go/src/perimeter_exporter/perimeter_exporter /bin/perimeter-exporter

ENTRYPOINT ["perimeter-exporter", "-interval", "10", "-debug"]

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"

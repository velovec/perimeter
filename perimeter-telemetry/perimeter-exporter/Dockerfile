FROM golang:latest as builder

WORKDIR /go/src/perimeter_exporter
COPY . .
RUN go get -d -v ./...
RUN go install -v ./...


FROM ubuntu:latest

ENTRYPOINT ["/etc/perimeter_exporter/perimeter-exporter", "-debug", "-interval", "10"]
WORKDIR "/etc/perimeter_exporter"
EXPOSE 9900

COPY --from=builder /go/src/perimeter_exporter/perimeter_exporter /etc/perimeter_exporter/perimeter-exporter
LABEL project.groupid="ru.v0rt3x.ctf" \
      project.artifactid="perimeter-exporter" \
      project.version="latest"
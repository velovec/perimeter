package main

import (
	"flag"
	"log"
	"net/http"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"

	"perimeter_exporter/api"
)

var (
	info = prometheus.NewGaugeVec(
		prometheus.GaugeOpts{
			Name: "perimeter_flags_info",
			Help: "Information about submited flags",
		},
		[]string{"type"},
	)
	queue = prometheus.NewGaugeVec(
		prometheus.GaugeOpts{
			Name: "perimeter_flags_queue",
			Help: "Information about flags queue to submit",
		},
		[]string{"type"},
	)
)

func main() {
	addr := flag.String("web.listen-address", ":9900", "Address on which to expose metrics")
	apiEndpoint := flag.String("api.endpoint-address", "127.0.0.1:8080", "Perimeter api address")
	interval := flag.Int("interval", 180, "Interval fo metrics collection in seconds")
	debug := flag.Bool("debug", false, "Debug log level")
	flag.Parse()

	r := prometheus.NewRegistry()

	r.MustRegister(info)
	r.MustRegister(queue)

	handler := promhttp.HandlerFor(r, promhttp.HandlerOpts{})

	http.Handle("/metrics", handler)
	go run(int(*interval), *debug, *apiEndpoint)
	log.Fatal(http.ListenAndServe(*addr, nil))
}

func run(interval int, debug bool, address string) {
	for {
		if debug {
			log.Printf("Try to get metrics from %s.\n", address)
		}
		flagStats, err := api.GetInfo(address)
		if err != nil {
			log.Print(err)
		}
		if debug {
			log.Printf("Metrics receved: %v\n", flagStats)
		}
		info.Reset()
		queue.Reset()
		info.With(prometheus.Labels{"type": "accepted"}).Set(float64(flagStats.Accepted))
		info.With(prometheus.Labels{"type": "rejected"}).Set(float64(flagStats.Rejected))

		queue.With(prometheus.Labels{"type": "normal"}).Set(float64(flagStats.Queued.Normal))
		queue.With(prometheus.Labels{"type": "high"}).Set(float64(flagStats.Queued.High))
		queue.With(prometheus.Labels{"type": "low"}).Set(float64(flagStats.Queued.Low))
		if debug {
			log.Printf("Waiting for %d seconds.\n", interval)
		}
		time.Sleep(time.Duration(interval) * time.Second)
	}
}

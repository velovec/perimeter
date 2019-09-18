package main

import (
	"log"
	"net/http"
)

func (p *Perimeter) HandleTransaction(r *http.Request, response []byte) []byte {
	replacedCount := 0
	pReplacedCount := &replacedCount
	replaced := p.pattern.ReplaceAllFunc(response, func(in []byte) []byte {
		n := GenerateRandomFlag(len(string(in)))
		*pReplacedCount = *pReplacedCount + 1
		return []byte(n)
	})

	log.Printf("%s replaced %v flags", r.URL.String(), replacedCount)
	return []byte(replaced)
}

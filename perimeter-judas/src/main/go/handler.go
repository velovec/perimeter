package main

import (
	"log"
	"net/http"
)

func (p *Perimeter) HandleTransaction(r *http.Request, response []byte) []byte {
	replacedCount := 0
	pReplacedCount := &replacedCount
	replaced := p.pattern.ReplaceAllFunc(response, func(flag []byte) []byte {
		// 193 - usual length of flag with jwt token
		if len(flag) == 193 {
			go func(flag string) {
				if err := p.SubmitFlag(flag); err != nil {
					log.Printf("ERROR: SubmitFlag failed: %v", err)
				}
			}(string(flag))
		}
		fakeFlag := GenerateRandomFlag(len(string(flag)))
		*pReplacedCount = *pReplacedCount + 1
		return []byte(fakeFlag)
	})

	log.Printf("%s used and replaced %v flags", r.URL.String(), replacedCount)
	return []byte(replaced)
}

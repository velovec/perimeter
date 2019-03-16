package api

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
)

// Type FlagStats stores information about flags

type FlagStats struct {
	Rejected int `json:"rejected"`
	Accepted int `json:"accepted"`
	Queued   struct {
		Normal int `json:"normal"`
		High   int `json:"high"`
		Low    int `json:"low"`
	} `json:"queued"`
}

var (
	err      error
	response *http.Response
	body     []byte
	stats    FlagStats
)

// Function GetInfo extract information about flags from perimeter api
func GetInfo(hostname string) (FlagStats, error) {
	response, err = http.Get(fmt.Sprintf("http://%s/api/flag/stats", hostname))
	if err != nil {
		fmt.Println(err)
		return stats, err
	}
	defer response.Body.Close()

	body, err = ioutil.ReadAll(response.Body)
	if err != nil {
		fmt.Println(err)
		return stats, err
	}

	err = json.Unmarshal(body, &stats)
	if err != nil {
		fmt.Println(err)
		return stats, err
	}

	return stats, nil
}

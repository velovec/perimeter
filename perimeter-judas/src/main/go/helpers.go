package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"math/rand"
	"net/http"
	"time"

	"github.com/pkg/errors"
)

const charset = "abcdefghijklmnopqrstuvwxyz" +
	"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

var seededRand *rand.Rand = rand.New(
	rand.NewSource(time.Now().UnixNano()))

func GenerateRandomFlag(length int) string {
	if length != 193 {
		return "VolgaCTF{" + StringWithCharset(length-10, charset) + "}"
	} else {
		return fmt.Sprintf("VolgaCTF{%s.%s.%s}",
			StringWithCharset(36, charset),
			StringWithCharset(59, charset),
			StringWithCharset(86, charset))
	}
}

func StringWithCharset(length int, charset string) string {
	b := make([]byte, length)
	for i := range b {
		b[i] = charset[seededRand.Intn(len(charset))]
	}
	return string(b)
}

func getJsonAndUnmarshal(url string, v interface{}) error {
	response, err := http.Get(url)
	if err != nil {
		return errors.Wrap(err, "http.Get error")
	}
	defer response.Body.Close()

	body, err := ioutil.ReadAll(response.Body)
	if err != nil {
		return errors.Wrap(err, "Error reading body")
	}

	if err = json.Unmarshal(body, v); err != nil {
		return errors.Wrap(err, "Unmarshal error")
	}

	return nil
}

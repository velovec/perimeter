package main

import (
	"net/http"
)

// ResponseTransformer modifies a response in any way we see fit, such as inserting extra JavaScript.
type ResponseTransformer interface {
	Transform(response *http.Response) error
}

type LocationRewritingResponseTransformer struct{}

func (l LocationRewritingResponseTransformer) Transform(response *http.Response) error {
	location, err := response.Location()
	if err != nil {
		if err == http.ErrNoLocation {
			return nil
		}
		return err
	}

	// Turn it into a relative URL
	location.Scheme = ""
	location.Host = ""
	response.Header.Set("Location", location.String())
	return nil
}

type CSPRemovingTransformer struct{}

func (c CSPRemovingTransformer) Transform(response *http.Response) error {
	response.Header.Del("Content-Security-Policy")
	return nil
}

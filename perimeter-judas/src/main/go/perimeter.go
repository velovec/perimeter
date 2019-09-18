package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"regexp"

	"github.com/pkg/errors"
)

type PerimeterJudasTarget struct {
	Protocol string `json:"protocol"`
	Host     string `json:"host"`
	Port     int    `json:"port"`
}

type PerimeterJudasConfig struct {
	Pattern string `json:"pattern"`
}

type PerimeterFlag struct {
	Flag string `json:"flag"`
}

type Perimeter struct {
	target      PerimeterJudasTarget
	config      PerimeterJudasConfig
	serverUrl   url.URL
	servicePort int
	pattern     *regexp.Regexp
}

func NewPerimeter(serverUrl url.URL, servicePort int) (p *Perimeter, err error) {
	p = new(Perimeter)
	p.serverUrl = serverUrl
	p.servicePort = servicePort

	if err = p.getJudasConfig(); err != nil {
		return nil, errors.Wrap(err, "Error getting judas config")
	}

	patternString := p.config.Pattern
	patternString = regexp.MustCompile("\\(\\?<([^>]+)>").ReplaceAllString(patternString, "(")
	patternString = regexp.MustCompile("^\\^").ReplaceAllString(patternString, "")
	patternString = regexp.MustCompile("\\$$").ReplaceAllString(patternString, "")
	p.pattern = regexp.MustCompile(patternString)
	return
}

func (p *Perimeter) getJudasConfig() error {
	return getJsonAndUnmarshal(
		fmt.Sprintf("%s/api/judas/config/", p.serverUrl.String()), &p.config)
}

func (p *Perimeter) getJudasTargetUrl() (*PerimeterJudasTarget, error) {
	target := new(PerimeterJudasTarget)
	return target, getJsonAndUnmarshal(
		fmt.Sprintf("%s/api/judas/target/%d/", p.serverUrl.String(), p.servicePort), &target)
}
func (p *Perimeter) GetJudasUrl() (*url.URL, error) {
	target, err := p.getJudasTargetUrl()
	if err != nil {
		return nil, errors.Wrap(err, "Error getting judas target")
	}
	u, err := url.Parse(fmt.Sprintf("%s://%s:%d", target.Protocol, target.Host, target.Port))
	if err != nil {
		return nil, errors.Wrapf(err, "Error parsing url %v", u)
	}
	return u, nil
}

func (p *Perimeter) SubmitFlag(flag string) error {
	flagBuffer, err := json.Marshal(&PerimeterFlag{
		Flag: flag,
	})

	if err != nil {
		return errors.Wrap(err, "Marshal error")
	}

	submitUrl := fmt.Sprintf("%s/api/judas/submit/", p.serverUrl.String())

	if _, err = http.Post(submitUrl, "application/json", bytes.NewBuffer(flagBuffer)); err != nil {
		return errors.Wrap(err, "Post request error")
	}
	return nil
}

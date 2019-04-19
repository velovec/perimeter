package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"regexp"
)

var (
	err      error
	response *http.Response
	body     []byte
	target   PerimeterJudasTarget
	config   PerimeterJudasConfig
	pattern  *regexp.Regexp
)

type HTTPTransaction struct {
	Request  http.Request
	Response http.Response
}

type PerimeterClient struct {
	perimeterServer *url.URL
	servicePort     int
}

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

func (client *PerimeterClient) ProcessTransactions(transactions chan HTTPTransaction) {
	config, err = client.GetConfig()
	if err != nil {
		fmt.Println(err)
		return
	}


	var patternString = config.Pattern

	patternString = regexp.MustCompile("\\(\\?<([^>]+)>").ReplaceAllString(patternString, "(")
	patternString = regexp.MustCompile("^\\^").ReplaceAllString(patternString, "")
	patternString = regexp.MustCompile("\\$$").ReplaceAllString(patternString, "")

	pattern, _ = regexp.Compile(patternString)
	if err != nil {
		fmt.Println(err)
		return
	}

	for transaction := range transactions {
		var body []byte

		body, err = ioutil.ReadAll(transaction.Response.Body)
		if err != nil {
			fmt.Println(err)
			continue
		}

		for _, flag := range pattern.FindAllString(string(body), -1) {
			client.SubmitFlag(flag)
		}
	}
}

func (client *PerimeterClient) GetConfig() (PerimeterJudasConfig, error) {
	response, err = http.Get(fmt.Sprintf("%s/api/judas/config/", client.perimeterServer))
	if err != nil {
		fmt.Println(err)
		return config, err
	}
	defer response.Body.Close()

	body, err = ioutil.ReadAll(response.Body)
	if err != nil {
		fmt.Println(err)
		return config, err
	}

	err = json.Unmarshal(body, &config)
	if err != nil {
		fmt.Println(err)
		return config, err
	}

	return config, nil
}

func (client *PerimeterClient) GetTargetUrl() (PerimeterJudasTarget, error) {
	response, err = http.Get(fmt.Sprintf("%s/api/judas/target/%d/", client.perimeterServer, client.servicePort))
	if err != nil {
		fmt.Println(err)
		return target, err
	}
	defer response.Body.Close()

	body, err = ioutil.ReadAll(response.Body)
	if err != nil {
		fmt.Println(err)
		return target, err
	}

	err = json.Unmarshal(body, &target)
	if err != nil {
		fmt.Println(err)
		return target, err
	}

	return target, nil
}

func (client *PerimeterClient) SubmitFlag(flag string) {
	var flagBuffer, err = json.Marshal(&PerimeterFlag{
		Flag: flag,
	})

	if err != nil {
		fmt.Println(err)
		return
	}

	var submitUrl = fmt.Sprintf("%s/api/judas/submit/", client.perimeterServer)

	_, err = http.Post(submitUrl, "application/json", bytes.NewBuffer(flagBuffer))
	if err != nil {
		fmt.Println(err)
	}
}

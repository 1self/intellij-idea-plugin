package org.quantifieddev.repository

import groovy.json.JsonBuilder
import groovyx.net.http.RESTClient

//@Slf4j
class PlatformRepository {
    def URI platformReadWriteUri
    def URI platformStreamUri

    public PlatformRepository() {
        println("Welcome to QuantifiedDev Idea Plugin")
    }

    def register(String content) {
        println("Registering the plugin.")
        RESTClient client = new RESTClient(platformStreamUri, 'application/json')
        def response = client.post([body: content])
        if (response.status == 200) {
            println("Executed Successfully $response.data")
            response.data
        } else {
            println("Unexpected failure. Cannot get a new stream.")
            """{ "Failure" : "${response.statusLine}" }""".toString()
        }
    }

    def insert(Map event, String writeToken) {
        println("QuantifiedDev Idea Plugin:Platform insert - event = $event")
        RESTClient platform = new RESTClient("$platformReadWriteUri", 'application/json')
        def response = platform.post([body: event, headers: ["Authorization": writeToken]])
        if (response.status == 200) {
            println("Executed Successfully $response.data")
            new JsonBuilder(response.data).toString()
        } else {
            println("Unexpected failure. Cannot write to the stream.")
            """{ "Failure" : "${response.statusLine}" }""".toString()
        }
    }

    def locate(String locationUriString) {
        RESTClient client = new RESTClient(locationUriString)
        def response = client.get([:])
        if(response.status == 200){
            println("Executed Successfully $response.data")
            response.data
        } else {
            println("Unexpected failure. Cannot get location.")
            """{ "Failure" : "${response.statusLine}" }""".toString()
        }
    }
}

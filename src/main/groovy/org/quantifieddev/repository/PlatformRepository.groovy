package org.quantifieddev.repository

import groovyx.net.http.RESTClient
import org.quantifieddev.utils.PlatformPersister

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

//@Slf4j
class PlatformRepository {
    def URI platformReadWriteUri
    def URI platformStreamUri
    def BlockingQueue<Tuple> eventQueue = new LinkedBlockingQueue<Tuple>();

    public PlatformRepository() {
        println("Welcome to QuantifiedDev Idea Plugin")
        Executors.newFixedThreadPool(1).submit(new PlatformPersister(eventQueue))
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
        eventQueue.offer(new Tuple(event, writeToken))
    }

    def locate(String locationUriString) {
        RESTClient client = new RESTClient(locationUriString)
        def response = client.get([:])
        if (response.status == 200) {
            println("Executed Successfully $response.data")
            response.data
        } else {
            println("Unexpected failure. Cannot get location.")
            """{ "Failure" : "${response.statusLine}" }""".toString()
        }
    }


}

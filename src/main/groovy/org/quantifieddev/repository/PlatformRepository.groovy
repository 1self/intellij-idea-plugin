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
    final def appId = "app-id-e2b2df17bf1f6994c1a661384fff2854"
    final def appSecret = "app-secret-0382136771320146d964b6d35df69827e80d7d26e7b80d9fcff22c0c1403c6c4"

    public PlatformRepository() {
        println("Welcome to QuantifiedDev Idea Plugin")
        Executors.newFixedThreadPool(1).submit(new PlatformPersister(eventQueue))
    }

    def register(String content) {
        println("Registering the plugin.")
        RESTClient client = new RESTClient(platformStreamUri, 'application/json')
        def response = client.post([body: content, headers: ["Authorization": appId + ":" + appSecret]])
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

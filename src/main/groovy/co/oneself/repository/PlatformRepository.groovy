package co.oneself.repository

import groovyx.net.http.RESTClient
import co.oneself.utils.PlatformPersister

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

import static co.oneself.Configuration.*

//@Slf4j
class PlatformRepository {
    def BlockingQueue<Tuple> eventQueue = new LinkedBlockingQueue<Tuple>();

    private static PlatformRepository platformRepository = new PlatformRepository();

    private PlatformRepository() {
        println("Welcome to 1self Idea Plugin")
        Executors.newFixedThreadPool(1).submit(new PlatformPersister(eventQueue))
    }

    public static PlatformRepository getInstance() {
        platformRepository
    }

    def register(String content) {
        println("Registering the plugin.")
        RESTClient client = new RESTClient(new URI(_1selfStreamUrl), 'application/json')
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

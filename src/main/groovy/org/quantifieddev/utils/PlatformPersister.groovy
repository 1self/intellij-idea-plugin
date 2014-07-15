package org.quantifieddev.utils

import groovyx.net.http.RESTClient
import org.quantifieddev.Configuration

import java.util.concurrent.BlockingQueue

class PlatformPersister implements Runnable {

    def BlockingQueue<Tuple> eventQueue

    PlatformPersister(eventQueue) {
        this.eventQueue = eventQueue
    }

    public void run() {
        try {
            while (true) {
                if (!eventQueue.isEmpty()) {
                    Tuple tuple = eventQueue.peek();
                    def event = tuple.get(0)
                    String writeToken = tuple.get(1)

                    println("QuantifiedDev Idea Plugin:Platform insert - event = $event")

                    try {
                        sendEventToPlatformViaREST(event, writeToken)
                    } catch (Exception ignored) {
                        Thread.sleep(5 * 60 * 1000)
                    }
                } else {
                    Thread.sleep(1 * 1000)
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace()
        }
    }

    private void sendEventToPlatformViaREST(event, String writeToken) {
        RESTClient platform = new RESTClient("$Configuration.repository.platformReadWriteUri", 'application/json')
        def response = platform.post([body: event, headers: ["Authorization": writeToken]])
        if (response.status == 200) {
            println("Executed Successfully $response.data")
            eventQueue.poll()
        } else {
            println("Unexpected failure. Cannot write to the stream.")
            """{ "Failure" : "${response.statusLine}" }""".toString()
        }
    }

}
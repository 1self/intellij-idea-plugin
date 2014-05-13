package org.quantifieddev

import org.quantifieddev.repository.PlatformRepository

class Configuration {
    private Configuration() {}

    def static final repository = new PlatformRepository()

    def static setPlatformReadWriteUri(String platformReadWriteUri) {
        println("Initializing Read Write URI.")
        repository.platformReadWriteUri = new URI(platformReadWriteUri)
    }

    def static setPlatformStreamUri(String platformStreamUri) {
        println("Initializing Stream URI.")
        repository.platformStreamUri = new URI(platformStreamUri)
    }
}

package org.quantifieddev

import org.quantifieddev.repository.PlatformRepository

class Configuration {

    private Configuration() {
    }

    static final ConfigObject appConfig = new ConfigSlurper('configuration').parse(AppConfig.class).configuration

    def static final repository = new PlatformRepository()
    def static final QD_DASHBOARD_URL = "http://app.quantifieddev.org/dashboard"

    def static setPlatformReadWriteUri(String platformReadWriteUri) {
        println("Initializing Read Write URI.")
        repository.platformReadWriteUri = new URI(platformReadWriteUri)
    }

    def static setPlatformStreamUri(String platformStreamUri) {
        println("Initializing Stream URI.")
        repository.platformStreamUri = new URI(platformStreamUri)
    }
}

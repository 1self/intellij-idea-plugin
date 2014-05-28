package org.quantifieddev.idea

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import org.quantifieddev.Configuration

@State(
        name = "BuildSettings",
        storages = [
        @Storage(id = "dir", file = "\$APP_CONFIG\$/quantifiedDevSettings.xml")
        ]
)

class BuildSettingsPersister implements PersistentStateComponent<BuildSettingsPersister> {
    private String platformURI = "http://quantifieddev.herokuapp.com/stream/"
    private String locationURI = "http://freegeoip.net/json"
    private String streamId, writeToken, readToken
    private Double latitude, longitude

    private void createStream() {
        println("Creating a new stream.")
        def streamDetails = Configuration.repository.register("{}")
        this.streamId = streamDetails.streamid
        this.writeToken = streamDetails.writeToken
        this.readToken = streamDetails.readToken
        println streamDetails;
    }

    private void locateUser() {
        def locationDetails = Configuration.repository.locate(locationURI)
        this.latitude = locationDetails.latitude
        this.longitude = locationDetails.longitude
        println locationDetails;
    }


    public setPlatformURI(String uri) {
        this.platformURI = uri
    }

    public setStreamId(String streamId) {
        this.streamId = streamId
    }

    public setWriteToken(String writeToken) {
        this.writeToken = writeToken
    }

    public setReadToken(String readToken) {
        this.readToken = readToken
    }

    public setLatitude(Double latitude) {
        this.latitude = latitude
    }

    public setLongitude(Double longitude) {
        this.longitude = longitude
    }

    public String getPlatformURI() {
        return this.platformURI
    }

    public String getStreamId() {
        return this.streamId
    }

    public String getWriteToken() {
        return this.writeToken
    }

    public String getReadToken() {
        return this.readToken
    }

    public Double getLatitude() {
        return this.latitude
    }

    public Double getLongitude() {
        return this.longitude
    }

    @Override
    BuildSettingsPersister getState() {
        println("getting state")
        if (this.streamId == null) {
            try{
                createStream()
            }
            catch(Exception e) {
                println("Exception : " + e.message)
                println("Exception during creating stream! Proceeding further without registration...")
            }
        }
        if (this.latitude == null || this.longitude == null) {
            try {
                locateUser()
            }
            catch(Exception e) {
                println("Exception : " + e.message)
                println("Exception during fetching user location! Proceeding further with existing details...")
            }
        }
        return this
    }

    @Override
    void loadState(BuildSettingsPersister buildSettings) {
        println("loading state")
        XmlSerializerUtil.copyBean(buildSettings, this)
    }
}

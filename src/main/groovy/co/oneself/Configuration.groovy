package co.oneself

class Configuration {

    private Configuration() {
    }

    static final ConfigObject appConfig = new ConfigSlurper('configuration').parse(AppConfig.class).configuration
    def static final _1SELF_DASHBOARD_URL = "https://app.1self.co/dashboard"
    public static final String _1selfStreamUrl = "https://api.1self.co/v1/streams/"
    public static final String locationURI = "http://freegeoip.net/json"
    public final static String appId = "app-id-e2b2df17bf1f6994c1a661384fff2854"
    public final static String appSecret = "app-secret-0382136771320146d964b6d35df69827e80d7d26e7b80d9fcff22c0c1403c6c4"

//    def static final _1SELF_DASHBOARD_URL = "http://localhost:5000/dashboard"
//    public static final String _1selfStreamUrl = "http://localhost:5000/v1/streams/"
//    public final static String appId = "abc"
//    public final static String appSecret = "123"
    public static String _1selfEventsUrl // populated on stream creation or plugin load
}

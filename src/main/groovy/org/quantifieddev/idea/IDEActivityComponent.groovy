package org.quantifieddev.idea

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.joda.time.DateTime
import org.quantifieddev.Configuration
import org.quantifieddev.utils.DateFormat
import org.quantifieddev.utils.EventLogger

import java.awt.*
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

class IDEActivityComponent implements ProjectComponent, AWTEventListener {
    private final Project project
    private final BuildSettingsComponent settings
    private boolean sleeping = false
    private boolean disposed = false
    private boolean isUserActive = false
    private final def sleepForTime = 5 * 60 * 1000
    private final def detectActivityForTime = 2 * 60 * 1000


    // +---------+       +--------+      +--------+
    //           |       |        |      |
    //           | AWAKE |        |      |  SLEEP
    //           | 2 min |        |      |  5 min
    //           +-------+        +------+

    public IDEActivityComponent(Project project, BuildSettingsComponent settings) {
        this.project = project
        this.settings = settings
        Thread.start("IDEActivityDetectorThread") {
            while(!disposed) {
                sleeping = true
                Thread.sleep(sleepForTime)  // don't detect for this time
                sleeping = false
                isUserActive = false
                Thread.sleep(detectActivityForTime)  // detect for this time
                long timeDurationInMillis = isUserActive ? detectActivityForTime : sleepForTime
                logEventQD(isUserActive, timeDurationInMillis)
            }
        }
    }

    void logEventQD(boolean isUserActive, long timeDurationInMillis) {
        def activityEvent = createActivityEvent(isUserActive, timeDurationInMillis)
        persist(activityEvent)
    }

    private Map createActivityEvent(isUserActive, timeDurationInMillis) {
        [
                "dateTime"  : ['$date': new DateTime().toString(DateFormat.isoDateTime)],
                "streamid"  : settings.streamId,
                "location"  : [
                        "lat" : settings.latitude,
                        "long": settings.longitude
                ],
                "source"    : 'Intellij Idea Plugin',
                "version"   : Configuration.appConfig.product.version.complete,
                "objectTags": ['Computer', 'Software'],  //??
                "actionTags": ['Develop'],            //??
                "properties": ['Environment': 'IntellijIdea12', 'isUserActive' : isUserActive, 'duration': timeDurationInMillis]
        ]
    }

    private def persist(Map event) {
        def writeToken = settings.writeToken
        Configuration.repository.insert(event, writeToken)
    }

    @Override
    void projectOpened() {
    }

    @Override
    void projectClosed() {
    }

    @Override
    void initComponent() {
        disposed = false
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK)
    }

    @Override
    void disposeComponent() {
        disposed = true
    }

    @Override
    String getComponentName() {
        this.getClass().simpleName
    }

    //This method belongs to AWTEventListener
    @Override
    void eventDispatched(AWTEvent event) {
        def eventId = event.getID()
        switch (eventId) {
            case MouseEvent.MOUSE_MOVED:
            case MouseEvent.MOUSE_CLICKED:
            //TODO: Key press for Enter, Backspace, Tab
            case KeyEvent.KEY_PRESSED:
                handleEvent(event)

            default:
                return
        }

    }

    private void handleEvent(AWTEvent event) {
        //if within sleep, just return
        // else listen to events and determine activity
        if(sleeping) {
            return
        } else {
            isUserActive = true
        }
    }
}

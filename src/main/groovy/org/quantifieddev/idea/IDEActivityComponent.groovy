package org.quantifieddev.idea

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.joda.time.DateTime
import org.quantifieddev.Configuration
import org.quantifieddev.utils.DateFormat

import java.awt.*
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

class IDEActivityComponent implements ProjectComponent, AWTEventListener {
    private final Project project
    private final BuildSettingsComponent settings
    private boolean disposed = false
    private boolean isUserActive = false
    private long activeSessionStartTime = System.currentTimeMillis()
    private long activeSessionEndTime = activeSessionStartTime
    private long inactiveSessionStartTime = System.currentTimeMillis()
    private long inactiveSessionEndTime = inactiveSessionStartTime

    // +-----+          +-----+         +-----+       AWAKE
    //       |          |     |         |
    //       | SLEEP    |AWAKE|         |
    //       | 5 min    |2 min|         |
    //       +----------+     +---------+             SLEEP
    //                        ^
    //                        |
    //                        |
    //                Log Activity Here

    //  +---------+          +---------+         +---------+
    //            |          |         |         |                       During Sampling Phase, we record
    //   SAMPLING | SAMPLING |         |         |                       - for how long is the user active or inactive?
    //     ON     |    OFF   |         |         |                       if we get a single event, during sampling ON phase,
    //            +-|--|--|--+         +---------+                       we assume that user was active for entire sampling phase
    //       10        10        10       10

    //  <---------------->
    //                  t1     t2    t3
    //                   +-----+     +-----+         ACTIVE
    //                   |     |     |
    //  +----------------+     +-----+               INACTIVE
    //                   4     6 7   9
    //               LOG INACTIVE

    /* Given: I define a inactive period of 2 units,

     activeSessionStartTime, t = 1
     activeSessionEndTime, t = 4
     t = 4 , activeSessionEndTime (last Active Time)
     t = 5, thread awakens  , diff = 5 - 4 = 1   (Inactive for 1 time Unit)
     t = 6, thread awakens  , diff = 6 - 4 = 2   (Inactive for 2 Time Unit)
     Send up event every 2 unit time
     log(activeDuration) // (activeSessionEndTime - activeSessionStartTime) = 3
     log(inactiveDuration)  //(inactiveSessionStartTime

     log(inactiveDuration) //2 units

     t = 10, lastEventTime (lastActiveTime)
     t = 11, thread awakens, diff = 11 - 7 = 1 (Inactive for 1 time  unit)
     t = 12, thread awakens, diff = 12 - 7 = 2 (Inactive for 2 time units)
     Send up event every 2 unit time
     log(lastActiveTime) // 7
     log(inactiveDuration) //2

                         AD ID ID ID ID AD
     */

    //  +---------+          +---------+         +---------+
    //            |          |         |         |                       During Sampling Phase, we record
    //   SAMPLING | SAMPLING |         |         |                       - for how long is the user active or inactive?
    //     ON     |    OFF   |         |         |                       if we get a single event, during sampling ON phase,
    //            +----------+         +---------+                       we assume that user was active for entire sampling phase
    //
    //
    //  o-------------o-------------o-------------o-------------o         We log the data collected during sampling phase
    //LOG HERE
    //
    // Define 1 Log Event = x samples
    //

    public IDEActivityComponent(Project project, BuildSettingsComponent settings) {
        this.project = project
        this.settings = settings
        Thread.start("IDEActivityDetectorThread") {
            while (!disposed) {
                if (isUserActive) {       //User is active
                    long inactivityTime = System.currentTimeMillis() - activeSessionEndTime
                    if (inactivityTime >= 5 * 60 * 1000) {
                        inactiveSessionStartTime = activeSessionEndTime
                        long activeDurationInMillis = activeSessionEndTime - activeSessionStartTime
                        try {
                            logEventQD(true, activeDurationInMillis)
                        }
                        catch (Exception e) {

                        }
                        isUserActive = false
                    }
                }
            }
        }
    }

    //This method belongs to AWTEventListener
    private void handleEvent(AWTEvent event) {
        if (!isUserActive) {
            activeSessionStartTime = System.currentTimeMillis()
            inactiveSessionEndTime = activeSessionStartTime
            long inactiveDurationInMillis = inactiveSessionEndTime - inactiveSessionStartTime
            try {
                logEventQD(false, inactiveDurationInMillis)
            }
            catch (Exception e) {

            }
            isUserActive = true
        }
        activeSessionEndTime = System.currentTimeMillis()
    }

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

    void logEventQD(boolean isUserActive, long timeDurationInMillis) {
        def activityEvent = createActivityEvent(isUserActive, timeDurationInMillis)
        persist(activityEvent)
    }

    private Map createActivityEvent(isUserActive, timeDurationInMillis) {
        [
                "dateTime": ['$date': new DateTime().toString(DateFormat.isoDateTime)],
                "streamid": settings.streamId,
                "location": [
                        "lat": settings.latitude,
                        "long": settings.longitude
                ],
                "source": 'Intellij Idea Plugin',
                "version": Configuration.appConfig.product.version.complete,
                "objectTags": ['Computer', 'Software'],
                "actionTags": ['Develop'],
                "properties": ['Environment': 'IntellijIdea12', 'isUserActive': isUserActive, 'duration': timeDurationInMillis]
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
        activeSessionStartTime = System.currentTimeMillis()
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


}

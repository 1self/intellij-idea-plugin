package org.quantifieddev.idea

import com.intellij.openapi.components.ApplicationComponent
import org.joda.time.DateTime
import org.quantifieddev.Configuration
import org.quantifieddev.utils.DateFormat

import java.awt.*
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

class IDEActivityComponent implements ApplicationComponent, AWTEventListener {
    private final BuildSettingsComponent settings
    private boolean disposed = false
    private boolean isUserActive = false
    private long activeSessionStartTime = System.currentTimeMillis()
    private long activeSessionEndTime = activeSessionStartTime
    private long inactiveSessionStartTime = System.currentTimeMillis()
    private long inactiveSessionEndTime = inactiveSessionStartTime
    int THRESHOLD_INACTIVITY_DURATION = 1 * 60 * 1000

    //  <---------------->
    //                  t1     t2    t3
    //                   +-----+     +-----+         ACTIVE
    //                   |     |     |
    //  +----------------+     +-----+               INACTIVE
    //                   4     6 7   9
    //               LOG INACTIVE
    // We are detecting edges, both, leading and trailing.
    public IDEActivityComponent(BuildSettingsComponent settings) {
        this.settings = settings
        Thread.start("IDEActivityDetectorThread") {
            while (!disposed) {
                if (isUserActive) {
                    if (inactivityDuration() >= THRESHOLD_INACTIVITY_DURATION) {
                        inactiveSessionStartTime = activeSessionEndTime
                        logEventQD(activityDuration())
                        markUserAsInactive()
                    }
                }
                Thread.sleep(THRESHOLD_INACTIVITY_DURATION)
            }
        }
    }

    private long inactivityDuration() {
        System.currentTimeMillis() - activeSessionEndTime
    }

    private long activityDuration() {
        activeSessionEndTime - activeSessionStartTime
    }

    private void markUserAsInactive() {
        isUserActive = false
    }

    @Override
    void eventDispatched(AWTEvent event) {
        def eventId = event.getID()
        switch (eventId) {
            case MouseEvent.MOUSE_MOVED:
            case MouseEvent.MOUSE_CLICKED:
                //TODO: Key press for Enter, Backspace, Tab
            case KeyEvent.KEY_PRESSED:
                handleEvent()
            default:
                return
        }
    }

    private void handleEvent() {
        if (!isUserActive) {
            startCountingActivity()
            markUserAsActive()
        } else if (inactivityDuration() >= THRESHOLD_INACTIVITY_DURATION) {
            handleIdeaWakeupEvent()
        }
        updateActivityEndCounter()
    }

    private void startCountingActivity() {
        activeSessionStartTime = System.currentTimeMillis()
        inactiveSessionEndTime = activeSessionStartTime
    }

    private void markUserAsActive() {
        isUserActive = true
    }

    private void handleIdeaWakeupEvent() {
        logEventQD(activityDuration())
        startCountingActivity()
    }

    private void updateActivityEndCounter() {
        activeSessionEndTime = System.currentTimeMillis()
    }

    void logEventQD(long timeDurationInMillis) {
        def activityEvent = createActivityEvent(timeDurationInMillis)
        persist(activityEvent)
    }

    private Map createActivityEvent(timeDurationInMillis) {
        [
                "dateTime"  : ['$date': new DateTime().toString(DateFormat.isoDateTime)],
                "streamid"  : settings.streamId,
                "location"  : [
                        "lat" : settings.latitude,
                        "long": settings.longitude
                ],
                "source"    : 'Intellij Idea Plugin',
                "version"   : Configuration.appConfig.product.version.complete,
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
    void initComponent() {
        disposed = false
        activeSessionStartTime = System.currentTimeMillis()
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK)
    }

    @Override
    void disposeComponent() {
        disposed = true
        if (isUserActive) {
            logEventQD(activityDuration())
        }
    }

    @Override
    String getComponentName() {
        this.getClass().simpleName
    }

}

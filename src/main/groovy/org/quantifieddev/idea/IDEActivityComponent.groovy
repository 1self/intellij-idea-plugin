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


    //  <---------------->
    //                  t1     t2    t3
    //                   +-----+     +-----+         ACTIVE
    //                   |     |     |
    //  +----------------+     +-----+               INACTIVE
    //                   4     6 7   9
    //               LOG INACTIVE

    // We are detecting edges, both, leading and trailing.

    public IDEActivityComponent(Project project, BuildSettingsComponent settings) {
        this.project = project
        this.settings = settings
        Thread.start("IDEActivityDetectorThread") {
            int PRESET_SLEEP_TIME = 5 * 60 * 1000
            while (!disposed) {
                if (isUserActive) {       //User is active
                    long inactivityTime = System.currentTimeMillis() - activeSessionEndTime
                    if (inactivityTime >= PRESET_SLEEP_TIME) {
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
                Thread.sleep(PRESET_SLEEP_TIME)
            }
        }
    }

    //This method belongs to AWTEventListener
    private void handleEvent(AWTEvent event) {
        if (!isUserActive) {
            activeSessionStartTime = System.currentTimeMillis()
            inactiveSessionEndTime = activeSessionStartTime
//        We don't want to log inactive events for now
/*
            long inactiveDurationInMillis = inactiveSessionEndTime - inactiveSessionStartTime
            try {
                logEventQD(false, inactiveDurationInMillis)
            }
            catch (Exception e) {

            }
*/
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
        try {
            if(isUserActive) {
                long activeDurationInMillis = activeSessionEndTime - activeSessionStartTime
                logEventQD(isUserActive, activeDurationInMillis)
            }
        } catch(Exception e) {
        }
    }

    @Override
    String getComponentName() {
        this.getClass().simpleName
    }


}

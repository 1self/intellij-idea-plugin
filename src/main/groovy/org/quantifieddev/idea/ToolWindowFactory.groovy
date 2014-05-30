package org.quantifieddev.idea

import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import org.joda.time.DateTime
import org.quantifieddev.Configuration
import org.quantifieddev.utils.DateFormat
import org.quantifieddev.utils.EventLogger

import javax.swing.JButton
import javax.swing.JPanel
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    private JButton wtfButton
    private com.intellij.openapi.wm.ToolWindow toolWindow
    private JPanel toolWindowContent
    private BuildSettingsComponent settings

    ToolWindowFactory(BuildSettingsComponent settings) {
        this.settings = settings
        wtfButton = new JButton("WTF!!")
        toolWindowContent = new JPanel()
        toolWindowContent.setLayout(new GridBagLayout())
        toolWindowContent.setPreferredSize(new Dimension(5, 5))
        GridBagConstraints c = new GridBagConstraints();
       // c.fill = GridBagConstraints.CENTER;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        wtfButton.setSize(5, 5)
        wtfButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                Map wtfEvent = createWTFEventQD()
                persist(wtfEvent)
            }
        })
                              /* button = new JButton("Long-Named Button 4");
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.gridwidth = 3;
    c.gridx = 0;
    c.gridy = 1;
    pane.add(button, c);*/

        toolWindowContent.add(wtfButton,c)
    }

    private Map createWTFEventQD() {
        [
                "dateTime": ['$date' : new DateTime().toString(DateFormat.isoDateTime)],
                "streamid": settings.streamId,
                "location": [
                        "lat":settings.latitude,
                        "long": settings.longitude
                ],
                "objectTags": [
                        "Computer",
                        "Software",
                        "code"
                ],
                "actionTags": [
                        "wtf"
                ],
                "properties": [
                        "Environment": "IntellijIdea12"
                ]
        ]
    }

    private def persist(Map event) {
        def writeToken = settings.writeToken
        Configuration.repository.insert(event, writeToken)
        EventLogger.logSuccess("Successfully Persisted", "$event")
    }

    @Override
    void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolWindow) {
        this.toolWindow = toolWindow
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance()
        Content content = contentFactory.createContent(toolWindowContent, "", false)
        toolWindow.getContentManager().addContent(content)
    }
}

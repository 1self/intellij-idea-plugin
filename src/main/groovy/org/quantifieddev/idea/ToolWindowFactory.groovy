package org.quantifieddev.idea

import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import org.joda.time.DateTime
import org.quantifieddev.Configuration
import org.quantifieddev.utils.DateFormat
import org.quantifieddev.utils.EventLogger

import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    private JButton wtfButton
    private com.intellij.openapi.wm.ToolWindow toolWindow
    private JPanel toolWindowContent
    private BuildSettingsComponent settings

    ToolWindowFactory(BuildSettingsComponent settings) {
        this.settings = settings

        toolWindowContent = new JPanel()
        toolWindowContent.setLayout(new GridBagLayout())
        toolWindowContent.setPreferredSize(new Dimension(5, 5))
        GridBagConstraints constraints = new GridBagConstraints()
        constraints.fill = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 1
        constraints.gridy = 1
        wtfButton = new JButton("WTF!!")
        wtfButton.setSize(5, 5)
        wtfButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                Map wtfEvent = createWTFEventQD()
                persist(wtfEvent)
            }
        })
        toolWindowContent.add(wtfButton, constraints)

        JLabel codeSucksLabel = new JLabel("This Code Sucks, Click -->")
        constraints = new GridBagConstraints()
        constraints.fill = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 0
        constraints.gridy = 1
        toolWindowContent.add(codeSucksLabel, constraints)

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

package org.quantifieddev.idea

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import org.joda.time.DateTime
import org.quantifieddev.Configuration
import org.quantifieddev.lang.LanguageDetector
import org.quantifieddev.utils.DateFormat
import org.quantifieddev.utils.EventLogger

import javax.swing.JButton
import javax.swing.JPanel
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    private JButton wtfButton, settingsButton, qdButton, helpButton
    private com.intellij.openapi.wm.ToolWindow toolWindow
    private JPanel toolWindowContent
    private BuildSettingsComponent settings

    ToolWindowFactory(BuildSettingsComponent settings) {
        this.settings = settings
        toolWindowContent = new JPanel()
        toolWindowContent.setLayout(new GridBagLayout())
        toolWindowContent.setPreferredSize(new Dimension(5, 5))
        GridBagConstraints constraints = new GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 1
        constraints.gridy = 1
        wtfButton = new JButton('WTF?!')
        wtfButton.setSize(5, 5)
        toolWindowContent.add(wtfButton, constraints)

        settingsButton = new JButton('S')
        constraints = new GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 0
        constraints.gridy = 1
        toolWindowContent.add(settingsButton, constraints)

        qdButton = new JButton('QD')
        constraints = new GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 0
        constraints.gridy = 2
        toolWindowContent.add(qdButton, constraints)

        helpButton = new JButton('?')
        constraints = new GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 0
        constraints.gridy = 3
        toolWindowContent.add(helpButton, constraints)
    }

    private Map createWTFEventQD(languages) {
        def objectTags = ['Computer', 'Software']
        def properties = ['Environment': 'IntellijIdea12']

        if(languages) {
          objectTags << 'code'
          properties << ['Language' : languages]
        }

        [
            "dateTime": ['$date' : new DateTime().toString(DateFormat.isoDateTime)],
            "streamid": settings.streamId,
            "location": [
                    "lat":settings.latitude,
                    "long": settings.longitude
            ],
            "objectTags": objectTags,
            "actionTags": ['wtf'],
            "properties": properties
        ]
    }

    @Override
    void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolWindow) {
        this.toolWindow = toolWindow
        Component component = toolWindow.getComponent()
        setupSettingsButtonListener(project)
        setupWtfButtonListener(project)

        qdButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                def message = 'To Be Implemented'
                Messages.showMessageDialog(project, message, "Information", Messages.getInformationIcon())
            }
        })

        helpButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                def message = '''
                               | Wtf is a way of measuring code quality <link to cartoon>
                               | Hit the wtf button every time you see something you don't like,
                               | then review you wtfs over time on your QD dashboard
                               | for more info see <link to qd>
                              '''.stripMargin('|')
                Messages.showMessageDialog(project, message, "Information", Messages.getInformationIcon())
            }
        })
        component.getParent().add(toolWindowContent)
    }

    private void setupWtfButtonListener(Project project) {
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project)
        final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance()
        wtfButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                Editor editor = fileEditorManager.getSelectedTextEditor()
                if (editor) {
                    Document document = editor.getDocument()
                    final VirtualFile file = fileDocumentManager.getFile(document)
                    def languages = LanguageDetector.detectLanguages([file.canonicalPath])
                    if (languages) {
                        Map wtfEvent = createWTFEventQD(languages)
                        persist(wtfEvent)
                    } else {
                        Map wtfEvent = createWTFEventQD()
                        persist(wtfEvent)
                    }
                } else {
                    Map wtfEvent = createWTFEventQD()
                    persist(wtfEvent)
                }
            }
        })
    }

    private void setupSettingsButtonListener(project) {
        settingsButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                ShowSettingsUtil settingsUtil = ShowSettingsUtil.getInstance()
                settingsUtil.showSettingsDialog(project, BuildSettingsComponent)
            }
        })
    }

    private def persist(Map event) {
        def writeToken = settings.writeToken
        Configuration.repository.insert(event, writeToken)
        EventLogger.logSuccess("Successfully Persisted", "$event")
    }
}

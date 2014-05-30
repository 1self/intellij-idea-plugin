package org.quantifieddev.idea

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentAdapter
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.joda.time.DateTime
import org.quantifieddev.Configuration
import org.quantifieddev.lang.LanguageDetector
import org.quantifieddev.utils.DateFormat
import org.quantifieddev.utils.EventLogger

import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import java.awt.Component
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
        constraints.anchor = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 1
        constraints.gridy = 1
        wtfButton = new JButton("WTF!!")
        wtfButton.setSize(5, 5)
        toolWindowContent.add(wtfButton, constraints)

        JLabel codeSucksLabel = new JLabel("This Code Sucks, Click -->")
        constraints = new GridBagConstraints()
        constraints.anchor = GridBagConstraints.EAST
        constraints.gridwidth = 1
        constraints.gridx = 0
        constraints.gridy = 1
        toolWindowContent.add(codeSucksLabel, constraints)
    }

    private Map createWTFEventQD(languages) {
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
                    "Environment": "IntellijIdea12",
                    "Language" : languages
            ]
        ]
    }


    @Override
    void createToolWindowContent(Project project, com.intellij.openapi.wm.ToolWindow toolWindow) {
        this.toolWindow = toolWindow
        Component component = toolWindow.getComponent()
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project)
        final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance()

        wtfButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                Editor editor = fileEditorManager.getSelectedTextEditor()
                Document document = editor.getDocument()
                final VirtualFile file = fileDocumentManager.getFile(document)
                def languages = LanguageDetector.detectLanguages([file.canonicalPath])
                if (languages) {
                    Map wtfEvent = createWTFEventQD(languages)
                    persist(wtfEvent)
                }
            }
        })
        component.getParent().add(toolWindowContent)
    }

    private def persist(Map event) {
        def writeToken = settings.writeToken
        Configuration.repository.insert(event, writeToken)
        EventLogger.logSuccess("Successfully Persisted", "$event")
    }
}

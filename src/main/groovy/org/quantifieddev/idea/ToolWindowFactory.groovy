package org.quantifieddev.idea

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.joda.time.DateTime
import org.quantifieddev.Configuration
import org.quantifieddev.lang.LanguageDetector
import org.quantifieddev.utils.DateFormat
import org.quantifieddev.utils.EventLogger

import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit
import java.awt.*
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToggleButton
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    private JButton wtfButton, settingsButton, qdButton
    private JToggleButton helpToggleButton
    private JEditorPane helpEditorPane
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

        helpToggleButton = new JToggleButton('?')
        constraints = new GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 0
        constraints.gridy = 3
        toolWindowContent.add(helpToggleButton, constraints)


        helpEditorPane = setupHelpPane()
        constraints = new GridBagConstraints()
        constraints.anchor = GridBagConstraints.WEST
        constraints.gridwidth = 1
        constraints.gridx = 1
        constraints.gridy = 2
        toolWindowContent.add(helpEditorPane, constraints)
    }

    private JEditorPane setupHelpPane() {
        def message = '''
                       | Wtf is a way of measuring <a href="http://www.quantifieddev.org/images/wtf.png">code quality</a>.</br>
                       | Hit the wtf button every time you see something you don't like,
                       | then review you wtfs over time on your QD dashboard.</br>
                       | For more info see <a href="http://www.quantifieddev.org">Quantified Dev</a>
                      '''.stripMargin('|')

        helpEditorPane = new JEditorPane()
        helpEditorPane.setCursor(new Cursor(Cursor.HAND_CURSOR))
        final Font currFont = helpEditorPane.getFont()
        helpEditorPane.setFont(new Font('Courier New', currFont.getStyle(), currFont.getSize()))
        helpEditorPane.setContentType('text/html')
        helpEditorPane.setText(message)
        helpEditorPane.setEditable(false)
        helpEditorPane.setPreferredSize(new Dimension(500, 100))
        helpEditorPane.setOpaque(false)
        helpEditorPane.setVisible(false)

        helpEditorPane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    Desktop desktop = Desktop.getDesktop()
                    try {
                        desktop.browse(e.getURL().toURI())
                    } catch (Exception ex) {
                        EventLogger.logError('Problem', ex.message)
                    }
                }
            }
        })
        helpEditorPane
    }

    private Map createWTFEventQD(languages) {
        def objectTags = ['Computer', 'Software']
        def properties = ['Environment': 'IntellijIdea12']

        if (languages) {
            objectTags << 'code'
            properties << ['Language': languages]
        }

        [
                "dateTime"  : ['$date': new DateTime().toString(DateFormat.isoDateTime)],
                "streamid"  : settings.streamId,
                "location"  : [
                        "lat" : settings.latitude,
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
        setupQDButtonListener(project)
        setupHelpButtonListener(project)
        component.getParent().add(toolWindowContent)
    }

    private void setupHelpButtonListener(project) {

        //On Press
        // - ChangeEvent!
        // - ChangeEvent!

        //On Release
        // - ChangeEvent!
        // - ItemEvent!
        // - ChangeEvent!
        // - ActionEvent!

        boolean showHelpEditorPane = false
        helpToggleButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ev) {
                int state = ev.getStateChange()
                if (state == ItemEvent.SELECTED) {
                    showHelpEditorPane = true
                } else {
                    showHelpEditorPane = false
                }
            }
        })

        helpToggleButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent ev) {
                helpEditorPane.setVisible(showHelpEditorPane)
                wtfButton.setVisible(!showHelpEditorPane)
            }
        })
    }

    private void setupQDButtonListener(project) {
        qdButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                def queryParams = "?streamId=${settings.streamId}&readToken=${settings.readToken}"
                Desktop.getDesktop().browse(new URL(Configuration.QD_DASHBOARD_URL + queryParams).toURI())
            }
        })
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

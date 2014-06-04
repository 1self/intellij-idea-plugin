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

import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.JToolBar
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
    private JPanel toolPanel
    private JPanel contentPanel
    private JToolBar toolBar
    private BuildSettingsComponent settings

    ToolWindowFactory(BuildSettingsComponent settings) {
        this.settings = settings
        toolWindowContent = new JPanel(new BorderLayout())

        toolBar = setupToolbar()
        toolWindowContent.add(toolBar, BorderLayout.WEST)

        contentPanel = new JPanel(new GridBagLayout())

        wtfButton = new JButton()
        Image wtfImage= ImageIO.read(getClass().getResource('/wtf_icon75x45.jpg'))
        wtfButton.setIcon(new ImageIcon(wtfImage))
        wtfButton.setToolTipText("Log WTF!")
        contentPanel.add(wtfButton)

        helpEditorPane = setupHelpPane()
        contentPanel.add(helpEditorPane)

        toolWindowContent.add(contentPanel, BorderLayout.CENTER)
    }

    private JToolBar setupToolbar() {
        toolBar = new JToolBar(JToolBar.VERTICAL)
        toolBar.setFloatable(false)

        settingsButton = new JButton()
        Image settingsImage= ImageIO.read(getClass().getResource('/settings_24x24.png'))
        settingsButton.setMargin(new Insets(0, 0, 0, 0))
        settingsButton.setIcon(new ImageIcon(settingsImage))
        settingsButton.setToolTipText("Edit/View QD Settings")
        toolBar.add(settingsButton)

        qdButton = new JButton()
        Image qdImage= ImageIO.read(getClass().getResource('/QDLogo_24x24.png'))
        qdButton.setMargin(new Insets(0, 0, 0, 0))
        qdButton.setIcon(new ImageIcon(qdImage))
        qdButton.setToolTipText("View my QD dashboard in browser")
        toolBar.add(qdButton)

        helpToggleButton = new JToggleButton()
        Image helpImage= ImageIO.read(getClass().getResource('/help_24x24.png'))
        helpToggleButton.setMargin(new Insets(0, 0, 0, 0))
        helpToggleButton.setIcon(new ImageIcon(helpImage))
        helpToggleButton.setToolTipText("About WTF")
        toolBar.add(helpToggleButton)
        toolBar
    }

    private JEditorPane setupHelpPane() {
        def message = '''
                       | Wtf is a way of measuring <a href="http://www.quantifieddev.org/images/WTFs_per_minute.gif">code quality</a>.</br>
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

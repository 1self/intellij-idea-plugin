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
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener

class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    private JButton wtfButton, settingsButton, qdButton, hadCoffeeButton, drankWaterButton
    private JToggleButton helpToggleButton
    private JScrollPane helpEditorScrollPane
    private com.intellij.openapi.wm.ToolWindow toolWindow
    private JPanel toolWindowContent
    private JPanel contentPanel
    private JToolBar toolBar
    private BuildSettingsComponent settings

    ToolWindowFactory(BuildSettingsComponent settings) {
        this.settings = settings
        toolWindowContent = new JPanel(new BorderLayout())
        toolWindowContent.setMinimumSize(new Dimension(550, 160))
        toolBar = setupToolbar()
        toolWindowContent.add(toolBar, BorderLayout.WEST)

        contentPanel = new JPanel(new FlowLayout())

        wtfButton = new JButton()
        Image wtfImage = ImageIO.read(getClass().getResource('/wtf_icon75x45.jpg'))
        wtfButton.setIcon(new ImageIcon(wtfImage))
        wtfButton.setToolTipText("Log WTF!")
        contentPanel.add(wtfButton)

        hadCoffeeButton = new JButton('Had Coffee')
//        Image wtfImage = ImageIO.read(getClass().getResource('/wtf_icon75x45.jpg'))
//        hadCoffeeButton.setIcon(new ImageIcon(wtfImage))
        hadCoffeeButton.setToolTipText('Had Coffee')
        contentPanel.add(hadCoffeeButton)

        drankWaterButton = new JButton('Drank Water')
//        Image wtfImage = ImageIO.read(getClass().getResource('/wtf_icon75x45.jpg'))
//        hadCoffeeButton.setIcon(new ImageIcon(wtfImage))
        drankWaterButton.setToolTipText('Drank Water')
        contentPanel.add(drankWaterButton)

        helpEditorScrollPane = setupHelpPane()
        contentPanel.add(helpEditorScrollPane)

        toolWindowContent.add(contentPanel, BorderLayout.CENTER)
    }

    private JToolBar setupToolbar() {
        toolBar = new JToolBar(JToolBar.VERTICAL)
        toolBar.setFloatable(false)

        settingsButton = new JButton()
        Image settingsImage = ImageIO.read(getClass().getResource('/settings_24x24.png'))
        settingsButton.setMargin(new Insets(0, 0, 0, 0))
        settingsButton.setIcon(new ImageIcon(settingsImage))
        settingsButton.setToolTipText("Edit/View QD Settings")
        toolBar.add(settingsButton)

        qdButton = new JButton()
        Image qdImage = ImageIO.read(getClass().getResource('/QDLogo_24x24.png'))
        qdButton.setMargin(new Insets(0, 0, 0, 0))
        qdButton.setIcon(new ImageIcon(qdImage))
        qdButton.setToolTipText("View my QD dashboard in browser")
        toolBar.add(qdButton)

        helpToggleButton = new JToggleButton()
        Image helpImage = ImageIO.read(getClass().getResource('/help_24x24.png'))
        helpToggleButton.setMargin(new Insets(0, 0, 0, 0))
        helpToggleButton.setIcon(new ImageIcon(helpImage))
        helpToggleButton.setToolTipText("About WTF")
        toolBar.add(helpToggleButton)
        toolBar
    }

    private JScrollPane setupHelpPane() {
        def encodedStreamId = URLEncoder.encode(settings.streamId, "UTF-8")
        def encodedReadToken = URLEncoder.encode(settings.readToken, "UTF-8")
        def message = """
                       | This QuantifiedDev plug-in lets you log and compare your software development activity for personal insights. We are currently in beta and are releasing new features all the time.
                       | <p>
                       | Current features:
                       | <ol>
                       | <li>WTFs - Wtf is a way of <a href='http://www.quantifieddev.org/images/wtfs_per_minute.gif?plugin=intellij'>measuring code quality</a>. Hit the wtf button every time you see something you don't like or find confusing, then review you wtfs over time on <a href='http://www.quantifieddev.org/app/dashboard.html?plugin=intellij&type=wtf&streamId=$encodedStreamId&readToken=$encodedReadToken'>your QD dashboard</a>.
                       | </li>
                       | <li>Builds - The plug-in logs when you start and complete builds and whether the build succeeded or failed. You can see your build behaviour over time on <a href='http://www.quantifieddev.org/app/dashboard.html?plugin=intellij&type=build&streamId=$encodedStreamId&readToken=$encodedReadToken'>your QD dashboard</a>.
                       | </li>
                       | <li>Community - The plug-in senses your location from your IP address and attaches this information to your logged events. This means that you can <a href='http://www.quantifieddev.org/app/community.html?plugin=intellij'>see yourself and your community of developers logging builds and WTFs around the world in real time</a>.
                       | </li>
                       | </ol>
                       | </p>
                       | For more info see <a href='http://www.quantifieddev.org/?plugin=intellij'>QuantifiedDev.org</a>
                      """.stripMargin('|')

        JEditorPane helpEditorPane = new JEditorPane()
        helpEditorPane.setCursor(new Cursor(Cursor.HAND_CURSOR))
        final Font currFont = helpEditorPane.getFont()
        helpEditorPane.setFont(new Font('Courier New', currFont.getStyle(), currFont.getSize()))
        helpEditorPane.setContentType('text/html')
        helpEditorPane.setText(message)
        helpEditorPane.setEditable(false)
        helpEditorPane.setOpaque(false)

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
        JScrollPane helpEditorScrollPane = new JScrollPane(helpEditorPane)
        helpEditorScrollPane.setVisible(false)
        helpEditorScrollPane.setPreferredSize(new Dimension(500, 150))
        helpEditorScrollPane
    }

    private Map createWTFEventQD(languages = []) {
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

    private Map createDrankWaterEventQD(languages = []) {
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
                "actionTags": ['water'],
                "properties": properties
        ]
    }

    private Map createHadCoffeeEventQD(languages = []) {
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
                "actionTags": ['coffee'],
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
        setupDrankWaterButtonListener(project)
        setupHadCoffeeButtonListener(project)
        component.getParent().add(toolWindowContent)
    }

    private void setupDrankWaterButtonListener(project) {
        drankWaterButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                Map drankWaterEvent = createDrankWaterEventQD()
                persist(drankWaterEvent)
            }
        })
    }

    private void setupHadCoffeeButtonListener(project) {
        hadCoffeeButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                Map hadCoffeeEvent = createHadCoffeeEventQD()
                persist(hadCoffeeEvent)
            }
        })
    }

    private void setupHelpButtonListener(project) {
        boolean showHelp = false
        helpToggleButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ev) {
                int state = ev.getStateChange()
                if (state == ItemEvent.SELECTED) {
                    showHelp = true
                } else {
                    showHelp = false
                }
            }
        })

        helpToggleButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent ev) {
                helpEditorScrollPane.setVisible(showHelp)
                wtfButton.setVisible(!showHelp)
                drankWaterButton.setVisible(!showHelp)
                hadCoffeeButton.setVisible(!showHelp)
            }
        })
    }

    private void setupQDButtonListener(project) {
        qdButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                def encodedStreamId = URLEncoder.encode(settings.streamId, "UTF-8")
                def encodedReadToken = URLEncoder.encode(settings.readToken, "UTF-8")
                def queryParams = "?streamId=$encodedStreamId&readToken=$encodedReadToken"
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

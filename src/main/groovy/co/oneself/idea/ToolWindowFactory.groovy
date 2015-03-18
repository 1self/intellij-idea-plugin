package co.oneself.idea

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.joda.time.DateTime
import co.oneself.Configuration
import co.oneself.lang.LanguageDetector
import co.oneself.repository.PlatformRepository
import co.oneself.utils.DateFormat
import co.oneself.utils.DesktopApi
import co.oneself.utils.EventLogger
import org.joda.time.format.ISODateTimeFormat

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
    private JButton wtfButton, settingsButton, _1selfDashboardButton
    private JToggleButton helpToggleButton, notificationToggleButton
    private Image disabledNotificationImage = ImageIO.read(getClass().getResource('/announcements_grey_icon24x24.png'))
    private Image enabledNotificationImage = ImageIO.read(getClass().getResource('/announcements_blue_icon24x24.png'))
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
        settingsButton.setToolTipText("Edit/View 1self Settings")
        toolBar.add(settingsButton)

        _1selfDashboardButton = new JButton()
        Image _1selfLogoImage = ImageIO.read(getClass().getResource('/1selfLogo_24x24.png'))
        _1selfDashboardButton.setMargin(new Insets(0, 0, 0, 0))
        _1selfDashboardButton.setIcon(new ImageIcon(_1selfLogoImage))
        _1selfDashboardButton.setToolTipText("View my 1self dashboard in browser")
        toolBar.add(_1selfDashboardButton)

        helpToggleButton = new JToggleButton()
        Image helpImage = ImageIO.read(getClass().getResource('/help_24x24.png'))
        helpToggleButton.setMargin(new Insets(0, 0, 0, 0))
        helpToggleButton.setIcon(new ImageIcon(helpImage))
        helpToggleButton.setToolTipText("About WTF")
        toolBar.add(helpToggleButton)

        notificationToggleButton = new JToggleButton()
        notificationToggleButton.setMargin(new Insets(0, 0, 0, 0))
        notificationToggleButton.setIcon(new ImageIcon(enabledNotificationImage))
        notificationToggleButton.setToolTipText("Turn Notifications OFF")
        toolBar.add(notificationToggleButton)

        toolBar
    }

    private JScrollPane setupHelpPane() {
        def encodedStreamId = settings.streamId ? URLEncoder.encode(settings.streamId, "UTF-8") : settings.streamId
        def encodedReadToken = settings.readToken ? URLEncoder.encode(settings.readToken, "UTF-8") : settings.readToken
        def message = """
                       | This 1self plug-in lets you log and compare your software development activity for personal insights. We are currently in beta and are releasing new features all the time.
                       | <p>
                       | Current features (ver $Configuration.appConfig.product.version.complete):
                       | <ol>
                       | <li>WTFs - Wtf is a way of <a href='http://www.oneself.org/images/wtfs_per_minute.gif?plugin=intellij'>measuring code quality</a>. Hit the wtf button every time you see something you don't like or find confusing, then review your wtfs over time on <a href='https://app.1self.co/dashboard?plugin=intellij&type=wtf&streamId=$encodedStreamId&readToken=$encodedReadToken'>your 1self dashboard</a>.
                       | </li>
                       | <li>Builds - The plug-in logs when you start and complete builds and whether the build succeeded or failed. You can see your build behaviour over time on <a href='https://app.1self.co/dashboard?plugin=intellij&type=build&streamId=$encodedStreamId&readToken=$encodedReadToken'>your 1self dashboard</a>.
                       | </li>
                       | <li>Community - The plug-in senses your location from your IP address and attaches this information to your logged events. This means that you can <a href='https://app.1self.co/community?plugin=intellij'>see yourself and your community of developers logging builds and WTFs around the world in real time</a>.
                       | </li>
                       | </ol>
                       | </p>
                       | For more info see <a href='http://www.1self.co/?plugin=intellij'>1self.co</a>
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
                    try {
                        DesktopApi.browse(e.getURL().toURI())
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

    private Map createWTFEvent(languages = []) {
        def objectTags = ['Computer', 'Software']
        def properties = ['Environment': 'IntellijIdea12']

        if (languages) {
            objectTags << 'code'
            properties << ['Language': languages]
        }

        [
                "dateTime"  : new DateTime().toString(),
                "location"  : [
                        "lat" : settings.latitude,
                        "long": settings.longitude
                ],
                "version"   : Configuration.appConfig.product.version.complete,
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
        setupDashboardButtonListener(project)
        setupHelpButtonListener(project)
        setupNotificationButtonListener(project)
        component.getParent().add(toolWindowContent)
    }

    private void setupNotificationButtonListener(project) {
        String toolTipText = "Turn Notifications "
        notificationToggleButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ev) {
                int state = ev.getStateChange()
                if (state == ItemEvent.DESELECTED) {
                    EventLogger.canLog = true
                    EventLogger.logSuccess('1self - Notifications', 'Turned On')
                    notificationToggleButton.setIcon(new ImageIcon(enabledNotificationImage))
                    notificationToggleButton.setToolTipText(toolTipText + 'OFF')
                } else {
                    EventLogger.canLog = false
                    notificationToggleButton.setIcon(new ImageIcon(disabledNotificationImage))
                    notificationToggleButton.setToolTipText(toolTipText + 'ON')
                }
            }
        })
    }


    private void setupHelpButtonListener(project) {
        boolean showHelp = false
        helpToggleButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ev) {
                int state = ev.getStateChange()
                showHelp = state == ItemEvent.SELECTED
            }
        })

        helpToggleButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent ev) {
                helpEditorScrollPane.setVisible(showHelp)
                wtfButton.setVisible(!showHelp)
            }
        })
    }

    private void setupDashboardButtonListener(project) {
        _1selfDashboardButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                def encodedStreamId = settings.streamId ? URLEncoder.encode(settings.streamId, "UTF-8") : settings.streamId
                def encodedReadToken = settings.readToken ? URLEncoder.encode(settings.readToken, "UTF-8") : settings.readToken
                def queryParams = "?streamId=$encodedStreamId&readToken=$encodedReadToken"
                DesktopApi.browse(new URL(Configuration._1SELF_DASHBOARD_URL + queryParams).toURI())
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
                        Map wtfEvent = createWTFEvent(languages)
                        persist(wtfEvent)
                    } else {
                        Map wtfEvent = createWTFEvent()
                        persist(wtfEvent)
                    }
                } else {
                    Map wtfEvent = createWTFEvent()
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
        PlatformRepository.getInstance().insert(event, writeToken)
        EventLogger.logSuccess("Successfully sent to 1self.co", "$event")
    }
}

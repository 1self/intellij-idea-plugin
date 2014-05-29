package org.quantifieddev.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class WTFToolWindow implements ToolWindowFactory {

    private JButton logWTF;
    private JLabel logStatus;
    private ToolWindow wtfToolWindow;
    private JPanel wtfToolWindowContent;

    @Override
    void createToolWindowContent(Project project, ToolWindow toolWindow) {
        wtfToolWindow = toolWindow;

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(wtfToolWindowContent, "", false);
        wtfToolWindow.getContentManager().addContent(content);
    }
}

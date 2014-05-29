package org.quantifieddev.idea

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.util.IconLoader
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.quantifieddev.Configuration

import javax.swing.*

public class BuildSettingsComponent implements ApplicationComponent, Configurable {
    private BuildSettingsForm form
    private BuildSettingsPersister buildSettingsPersister

    public BuildSettingsComponent() {
        buildSettingsPersister = ServiceManager.getService(BuildSettingsPersister.class)
    }

    public String getPlatformUri() {
        return buildSettingsPersister.getPlatformURI()
    }

    public String getStreamId() {
        return buildSettingsPersister.getStreamId()
    }

    Double getLatitude() {
        return buildSettingsPersister.getLatitude()
    }

    Double getLongitude() {
        return buildSettingsPersister.getLongitude()
    }

    public String getWriteToken() {
        return buildSettingsPersister.getWriteToken()
    }

    public String getReadToken() {
        return buildSettingsPersister.getReadToken()
    }

    public javax.swing.Icon getIcon() {
        return IconLoader.getIcon("/QD_icon.png")
    }

    public void setBuildSettingsData(final String uri, final String streamId, final String readToken, final String writeToken,
                                     final String latitude, final String longitude) {
        buildSettingsPersister.setPlatformURI(uri)
        buildSettingsPersister.setStreamId(streamId)
        buildSettingsPersister.setReadToken(readToken)
        buildSettingsPersister.setWriteToken(writeToken)
        buildSettingsPersister.setLatitude(Double.parseDouble(latitude))
        buildSettingsPersister.setLongitude(Double.parseDouble(longitude))
        buildSettingsPersister.loadState(buildSettingsPersister)
    }

    @Override
    void initComponent() {
        println("Initializing App Component.")
        Configuration.setPlatformStreamUri(this.buildSettingsPersister.platformURI)
        buildSettingsPersister.getState()
    }

    @Override
    void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "QuantifiedDevComponent";
    }

    @Override
    public String getDisplayName() {
        return "Quantified Dev Settings";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (form == null) {
            form = new BuildSettingsForm();
            form.setData(this);
        }
        return form.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return form != null && form.isModified(this);
    }

    @Override
    public void apply() throws ConfigurationException {
        if (form != null) {
            form.setBuildSettingsData(this);
        }

    }

    @Override
    public void reset() {
        if (form != null) {
            form.setData(this);
        }
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }


}

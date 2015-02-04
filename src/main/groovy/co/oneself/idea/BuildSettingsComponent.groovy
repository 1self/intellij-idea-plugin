package co.oneself.idea

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import javax.swing.*

public class BuildSettingsComponent implements ApplicationComponent, Configurable {
    private BuildSettingsForm form
    private BuildSettingsPersister buildSettingsPersister

    public BuildSettingsComponent() {
        buildSettingsPersister = ServiceManager.getService(BuildSettingsPersister.class)
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

    public void setBuildSettingsData(
            final String streamId, final String readToken, final String latitude, final String longitude) {
        buildSettingsPersister.setStreamId(streamId)
        buildSettingsPersister.setReadToken(readToken)
        buildSettingsPersister.setLatitude(Double.parseDouble(latitude))
        buildSettingsPersister.setLongitude(Double.parseDouble(longitude))
        buildSettingsPersister.loadState(buildSettingsPersister)
    }

    @Override
    void initComponent() {
        //println("Initializing App Component.")
        buildSettingsPersister.getState()
    }

    @Override
    void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "1selfComponent";
    }

    @Override
    public String getDisplayName() {
        return "1self Settings";
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

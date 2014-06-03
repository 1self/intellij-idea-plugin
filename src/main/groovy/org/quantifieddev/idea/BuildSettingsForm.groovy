package org.quantifieddev.idea

import javax.swing.*
import java.awt.*

class BuildSettingsForm {

    private JPanel rootComponent;
    private JLabel latitude, longitude, streamId, readToken
    private JTextField latitudeText, longitudeText, streamIdText, readTokenText

    public BuildSettingsForm() {
        if (rootComponent == null) {
            createUIComponents()
        }
    }

    public JComponent getRootComponent() {
        return rootComponent
    }

    public void setData(BuildSettingsComponent data) {
        streamIdText.setText(data.getStreamId())
        readTokenText.setText(data.readToken)
        latitudeText.setText(data.getLatitude().toString())
        longitudeText.setText(data.getLongitude().toString())
    }

    public void setBuildSettingsData(BuildSettingsComponent data) {
        data.setBuildSettingsData(streamIdText.getText(), readToken.text, latitudeText.getText(), longitudeText.getText())
    }

    public boolean isModified(BuildSettingsComponent data) {
        return isLatitudeModified(data) || isLongitudeModified(data)
    }

    private boolean isLatitudeModified(BuildSettingsComponent data) {
        def latitude = latitudeText.getText()
        return (latitude != null && latitude != "null") ? !Double.parseDouble(latitude).equals(data.latitude) : data.latitude != null
    }

    private boolean isLongitudeModified(BuildSettingsComponent data) {
        def longitude = longitudeText.getText()
        return (longitude != null && longitude != "null") ? !Double.parseDouble(longitude).equals(data.longitude) : data.longitude != null
    }

    private void createUIComponents() {
        rootComponent = new JPanel()
        rootComponent.setSize(100, 100)
        rootComponent.setLayout(new GridLayout(20, 2))
//        platformURI = new JLabel("Platform URI")
//        platformURIText = new JTextField(30)
//        platformURI.setLabelFor(platformURIText)
//        platformURIText.setEditable(false)

        streamId = new JLabel("StreamId")
        streamIdText = new JTextField(30)
        streamId.setLabelFor(streamIdText)
        streamIdText.setEditable(false)

        readToken = new JLabel("ReadToken")
        readTokenText = new JTextField(30)
        readToken.setLabelFor(readTokenText)
        readTokenText.setEditable(false)

//        writeToken = new JLabel("WriteToken")
//        writeTokenText = new JTextField(30)
//        writeToken.setLabelFor(writeTokenText)
//        writeTokenText.setEditable(false)

        latitude = new JLabel("Latitude")
        latitudeText = new JTextField(30)
        latitude.setLabelFor(latitudeText)

        longitude = new JLabel("Longitude")
        longitudeText = new JTextField(30)
        longitude.setLabelFor(longitudeText)

//        rootComponent.add(platformURI)
//        rootComponent.add(platformURIText)
        rootComponent.add(streamId)
        rootComponent.add(streamIdText)
        rootComponent.add(readToken)
        rootComponent.add(readTokenText)
//        rootComponent.add(writeToken)
//        rootComponent.add(writeTokenText)
        rootComponent.add(latitude)
        rootComponent.add(latitudeText)
        rootComponent.add(longitude)
        rootComponent.add(longitudeText)
    }
}
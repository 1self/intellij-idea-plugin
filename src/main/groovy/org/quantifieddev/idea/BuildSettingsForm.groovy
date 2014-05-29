package org.quantifieddev.idea

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.quantifieddev.Configuration

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class BuildSettingsForm {

    private JPanel rootComponent;
    private JLabel platformURI, latitude, longitude, streamId, readToken, writeToken;
    private JTextField platformURIText, latitudeText, longitudeText, streamIdText, readTokenText, writeTokenText;
    private JButton wtfButton;
    private final DateTimeFormatter isoDateTimeFormat = ISODateTimeFormat.dateTimeNoMillis()

    public BuildSettingsForm() {
        if (rootComponent == null) {
            createUIComponents()
        }
    }

    public JComponent getRootComponent() {
        return rootComponent
    }

    public void setData(BuildSettingsComponent data) {
        platformURIText.setText(data.getPlatformUri())
        streamIdText.setText(data.getStreamId())
        readTokenText.setText(data.readToken)
        writeTokenText.setText(data.writeToken)
        latitudeText.setText(data.getLatitude().toString())
        longitudeText.setText(data.getLongitude().toString())
    }

    public void setBuildSettingsData(BuildSettingsComponent data) {
        data.setBuildSettingsData(platformURIText.getText(), streamIdText.getText(), readToken.text, writeToken.text, latitudeText.getText(), longitudeText.getText())
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
        platformURI = new JLabel("Platform URI")
        platformURIText = new JTextField(30)
        platformURI.setLabelFor(platformURIText)
        platformURIText.setEditable(false)

        streamId = new JLabel("StreamId")
        streamIdText = new JTextField(30)
        streamId.setLabelFor(streamIdText)
        streamIdText.setEditable(false)

        readToken = new JLabel("ReadToken")
        readTokenText = new JTextField(30)
        readToken.setLabelFor(readTokenText)
        readTokenText.setEditable(false)

        writeToken = new JLabel("WriteToken")
        writeTokenText = new JTextField(30)
        writeToken.setLabelFor(writeTokenText)
        writeTokenText.setEditable(false)

        latitude = new JLabel("Latitude")
        latitudeText = new JTextField(30)
        latitude.setLabelFor(latitudeText)

        longitude = new JLabel("Longitude")
        longitudeText = new JTextField(30)
        longitude.setLabelFor(longitudeText)

        wtfButton = new JButton("WTF")
        wtfButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                Map wtfEvent = createWTFEventQD()
                persist(wtfEvent)
            }
        })

        rootComponent.add(platformURI)
        rootComponent.add(platformURIText)
        rootComponent.add(streamId)
        rootComponent.add(streamIdText)
        rootComponent.add(readToken)
        rootComponent.add(readTokenText)
        rootComponent.add(writeToken)
        rootComponent.add(writeTokenText)
        rootComponent.add(latitude)
        rootComponent.add(latitudeText)
        rootComponent.add(longitude)
        rootComponent.add(longitudeText)
        rootComponent.add(wtfButton)
    }

    private Map createWTFEventQD() {
        println("Creating WTF event ")
        [
                "dateTime": ['$date' : new DateTime().toString(isoDateTimeFormat)],
                "streamid": streamIdText.getText(),
                "location": [
                        "lat":latitudeText.getText(),
                        "long": longitudeText.getText()
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
                        "Environment": "IntellijIdea12"
                ]
        ]

    }

    private def persist(Map event) {
        println("Persisting... $event")
        Configuration.repository.insert(event, writeToken.text);
    }

}
#IntelliJ-Idea Plugin for QuantifiedDev

### Overview

QuantifiedDev is Quantified Self for developers - self knowledge through numbers.
i.e. You can see interesting stats and correlations drawn from your daily builds
<a href="https://app.quantifieddev.org">here</a>.

Just install the plugin in Idea and start seeing the stats. It's that simple.

### Plugin Installation:
1. Download the plugin from <a href="http://www.quantifieddev.org/#/download">here</a>.
2. Open Intellij Idea
3. Go to File -> Settings(Ctrl+Alt+S) -> Plugins
4. Click on 'Install plugin from disk...'
5. Browse to the location where the QuantifiedDev Plugin is downloaded
6. Select the downloaded plugin zip and click OK
7. Now 'Quantified Dev Build Plugin' is added to the list of plugins
8. Click Apply and restart Intellij Idea

### How do I see the stats?
1. Open Intellij Idea
2. Go to File -> Settings(Ctrl + Alt + S) -> Quantified Dev Settings
3. Copy streamid
4. Go to http://www.quantifieddev.org
5. Paste the streamid in 'streamid' textbox
6. Go back to IntelliJIdea and copy read token
7. Go back to website and paste the read token in 'read token' textbox
8. Click 'Save'.

Now as you build your projects from Idea, you can see corresponding stats reflected on the website (https://app.quantifieddev.org)

### Information collected by Plugin:
1. Your Latitude and Longitude based on your IP address
2. Programming Languages used in the project
3. Status of the Build event (Start, Success, Failure)


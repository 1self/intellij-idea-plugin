#intellij-idea-plugin
====================

##IntelliJ-Idea Plugin for QuantifiedDev

### Overview

QuantifiedDev is Quantified Self for developers - self knowledge through numbers.
Quantified Dev Build Plugin sends the build events fired from Intellij Idea to QuantifiedDev server.
This allows the user to view his/her development statistics <a href="http://www.quantifieddev.org/app">here</a>.

### Steps to install:
1. Download the plugin from <a href="http://www.quantifieddev.org/#download">here</a>.
2. Open Intellij Idea
3. Go to File -> Settings(Ctrl+Alt+S) -> Plugins
4. Click on 'Install plugin from disk...'
5. Browse to the location where the QuantifiedDev Plugin is downloaded
6. Select 'buildIdeaPlugin.zip' and click OK
7. Now 'Quantified Dev Build Plugin' is added to the list of plugins
8. Click Apply and restart Intellij Idea

### Information collected by Plugin:
1. Latitude and Longitude of the machine based on its IP address
2. The StreamId, Read and Write Tokens required by the QuantifiedDev app
3. Programming Languages used in the project
4. Status of the Build event (Start, Success, Failure)


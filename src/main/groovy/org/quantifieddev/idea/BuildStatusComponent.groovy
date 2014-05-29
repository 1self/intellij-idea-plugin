package org.quantifieddev.idea

import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import groovy.util.logging.Slf4j
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.quantifieddev.Configuration
import org.quantifieddev.lang.LanguageDetector
import org.quantifieddev.utils.DirWalker
import org.quantifieddev.utils.EventLogger

import java.util.regex.Pattern

class BuildStatusComponent implements ProjectComponent, CompilationStatusListener {
    def static final String QUANTIFIED_DEV_BUILD_LOGGER = 'Quantified Dev Build Logger'
    private final Project project
    private final BuildSettingsComponent settings
    private final CompileTask beforeCompileTask
    private final DateTimeFormatter isoDateTimeFormat = ISODateTimeFormat.dateTimeNoMillis()
    private final languages
    private final long timeToDetectProjectLanguages
    private final long totalFilesScanned
    private final Pattern regex = Pattern.compile(LanguageDetector.languageFileExtensions.values().flatten().collect { "(.*\\$it)" }.join('|'))

    public BuildStatusComponent(Project project, BuildSettingsComponent settings) {
        this.project = project
        this.settings = settings
        URI projectRoot = new URI("file:///${project.baseDir.canonicalPath}")
        long startTime = System.currentTimeMillis()
        def walker = new DirWalker(projectRoot, regex)
        def files = walker.walk()
        this.totalFilesScanned = files.size()
        this.languages = LanguageDetector.detectLanguages(files)
        this.timeToDetectProjectLanguages = System.currentTimeMillis() - startTime

        this.beforeCompileTask = new CompileTask() {
            @Override
            boolean execute(CompileContext compileContext) {
                def executionSuceeded = true    //Exception in plugin should not abort the compilation
                try {
                    def startEvent = createBuildStartEventQD(new DateTime(compileContext.properties.startCompilationStamp).toString(isoDateTimeFormat))
                    persist(startEvent)
                }
                catch (Exception e) {
                    EventLogger.logError("Could Not Send Event to quantifieddev.org", e.message)
                }
                executionSuceeded
            }
        }
    }

    //CompilationStatusListener
    @Override
    void compilationFinished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
        try {
            def finishEvent = createBuildFinishEventQD(getCompilationStatus(aborted, errors), new DateTime().toString(isoDateTimeFormat))
            persist(finishEvent)
        }
        catch (Exception e) {
            EventLogger.logError("Could Not Send Event to quantifieddev.org", e.message)
        }
    }

    //CompilationStatusListener
    @Override
    void fileGenerated(String s, String s1) {
    }


    private String getCompilationStatus(aborted, errors) {
        if (errors > 0) {
            return 'Failure'
        } else if (aborted == true) {
            return 'Aborted'
        } else {
            return 'Success'
        }
    }

    //ProjectComponent
    @Override
    void projectOpened() {
    }

    //ProjectComponent
    @Override
    void projectClosed() {
    }

    //ProjectComponent
    @Override
    void initComponent() {
        println("Initializing Project Component.")
        CompilerManager.getInstance(project).addCompilationStatusListener(this)
        CompilerManager.getInstance(project).addBeforeTask(beforeCompileTask)
        Configuration.setPlatformReadWriteUri("${this.settings.platformUri}${this.settings.streamId}/event")
    }

    //ProjectComponent
    @Override
    void disposeComponent() {
        CompilerManager.getInstance(project).removeCompilationStatusListener(this)
    }

    //ProjectComponent
    @Override
    String getComponentName() {
        QUANTIFIED_DEV_BUILD_LOGGER
    }

    //todo: reflect on whether language property needs to be matured to object tags?
    //todo: really its a general question that we need to have a guideline on:
    //todo: when should a property be upgraded to a object tag or vice versa?
    private def createBuildStartEventQD(startedOn) {
        [
                'dateTime': startedOn,
                'streamid': settings.streamId,
                'location': ['lat': settings.latitude, 'long': settings.longitude],
                'objectTags': ['Computer', 'Software'],
                'actionTags': ['Build', 'Start'],
                'properties': ['Language': languages, 'Environment': 'IntellijIdea12', 'TimeToDetectProjectLanguages': timeToDetectProjectLanguages, 'TotalFilesScanned': totalFilesScanned]
        ]
    }

    private def createBuildFinishEventQD(compilationStatus, finishedOn) {
        [
                'dateTime': finishedOn,
                'streamid': settings.streamId,
                'location': ['lat': settings.latitude, 'long': settings.longitude],
                'objectTags': ['Computer', 'Software'],
                'actionTags': ['Build', 'Finish'],
                'properties': ['Result': compilationStatus, 'Language': languages, 'Environment': 'IntellijIdea12', 'TimeToDetectProjectLanguages': timeToDetectProjectLanguages, 'TotalFilesScanned': totalFilesScanned]
        ]
    }

    private def persist(Map event) {
        Configuration.repository.insert(event, settings.writeToken);
    }
}

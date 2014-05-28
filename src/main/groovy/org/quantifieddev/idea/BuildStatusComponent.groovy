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

class BuildStatusComponent implements ProjectComponent, CompilationStatusListener {
    def static final String QUANTIFIED_DEV_BUILD_LOGGER = 'Quantified Dev Build Logger'
    private final Project project
    private final BuildSettingsComponent settings
    private final CompileTask beforeCompileTask
    private final DateTimeFormatter isoDateTimeFormat = ISODateTimeFormat.dateTimeNoMillis()
    private final languages
    private final long timeToDetectProjectLanguages

    public BuildStatusComponent(Project project, BuildSettingsComponent settings) {
        this.project = project
        this.settings = settings
        URI projectRoot = new URI("file:///${project.baseDir.canonicalPath}")
        long startTime = System.currentTimeMillis()
        this.languages = LanguageDetector.detectLanguages(projectRoot)
        this.timeToDetectProjectLanguages = System.currentTimeMillis() - startTime

        this.beforeCompileTask = new CompileTask() {
            @Override
            boolean execute(CompileContext compileContext) {
                println("Starting Compilation!")
                def executionSuceeded = true    //Exception in plugin should not abort the compilation
                try {
                    def startEvent = createBuildStartEventQD(new DateTime(compileContext.properties.startCompilationStamp).toString(isoDateTimeFormat))
                    persist(startEvent)
                }
                catch (Exception e) {
                    println(e.getMessage())
                    println("Exception occurred! Continuing Compilation!")
                }
                executionSuceeded
            }
        }
        println("BuildStatusComponent Created for Project $project.name")

    }

    //CompilationStatusListener
    @Override
    void compilationFinished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
        println("Compilation finished!")
        println("COMPILATION CONTEXT = ${compileContext.properties}")
        println("COMPILATION Aborted = $aborted, Errors = $errors")
        try {
            def finishEvent = createBuildFinishEventQD(getCompilationStatus(aborted, errors), new DateTime().toString(isoDateTimeFormat))
            persist(finishEvent)
        }
        catch (Exception e) {
            println(e.getMessage())
            println("Exception occurred after compilation! Ignoring and proceeding...")
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
        println("getting stream id: " + settings.streamId)
        [
                'dateTime': startedOn,
                'streamid': settings.streamId,
                'location': ['lat': settings.latitude, 'long': settings.longitude],
                'objectTags': ['Computer', 'Software'],
                'actionTags': ['Build', 'Start'],
                'properties': ['Language': languages, 'Environment': 'IntellijIdea12', 'TimeToDetectProjectLanguages': timeToDetectProjectLanguages]
        ]
    }

    private def createBuildFinishEventQD(compilationStatus, finishedOn) {
        println("getting stream id: " + settings.streamId)
        [
                'dateTime': finishedOn,
                'streamid': settings.streamId,
                'location': ['lat': settings.latitude, 'long': settings.longitude],
                'objectTags': ['Computer', 'Software'],
                'actionTags': ['Build', 'Finish'],
                'properties': ['Result': compilationStatus, 'Language': languages, 'Environment': 'IntellijIdea12', 'TimeToDetectProjectLanguages': timeToDetectProjectLanguages]
        ]
    }

    private def persist(Map event) {
        println("Persisting... $event")
        Configuration.repository.insert(event, settings.writeToken);
    }
}

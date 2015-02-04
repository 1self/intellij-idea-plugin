package co.oneself.idea

import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import co.oneself.lang.LanguageDetector
import co.oneself.repository.PlatformRepository
import co.oneself.utils.DirWalker
import co.oneself.utils.EventLogger

import java.util.regex.Pattern

class BuildStatusComponent implements ProjectComponent, CompilationStatusListener {
    def static final String _1SELF_BUILD_LOGGER = '1self Build Logger'
    private final Project project
    private final BuildSettingsComponent settings
    private final CompileTask beforeCompileTask
    private final DateTimeFormatter isoDateTimeFormat = ISODateTimeFormat.dateTimeNoMillis()
    private final languages
    private final long timeToDetectProjectLanguages
    private final long totalFilesScanned
    private final Pattern regex = Pattern.compile(LanguageDetector.languageFileExtensions.values().flatten().collect {
        "(.*\\$it)"
    }.join('|'))

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
                    def startEvent = createBuildStartEvent(new DateTime(compileContext.properties.startCompilationStamp).toString(isoDateTimeFormat))
                    persist(startEvent)
                }
                catch (Exception e) {
                    EventLogger.logError("Could Not Send Event to 1self.co", e.message)
                }
                executionSuceeded
            }
        }
    }

    //CompilationStatusListener
    @Override
    void compilationFinished(boolean aborted, int errors, int warnings, final CompileContext compileContext) {
        try {
            def buildDuration = DateTime.now().millis - compileContext.properties.startCompilationStamp
            def buildFinishTime = new DateTime().toString(isoDateTimeFormat)
            def finishEvent = createBuildFinishEvent(getCompilationStatus(aborted, errors), buildFinishTime, buildDuration)
            persist(finishEvent)
        }
        catch (Exception e) {
            EventLogger.logError("Could Not Send Event to 1self.co", e.message)
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
        //println("Initializing Project Component.")
        CompilerManager.getInstance(project).addCompilationStatusListener(this)
        CompilerManager.getInstance(project).addBeforeTask(beforeCompileTask)
    }

    //ProjectComponent
    @Override
    void disposeComponent() {
        CompilerManager.getInstance(project).removeCompilationStatusListener(this)
    }

    //ProjectComponent
    @Override
    String getComponentName() {
        _1SELF_BUILD_LOGGER
    }

    //todo: reflect on whether language property needs to be matured to object tags?
    //todo: really its a general question that we need to have a guideline on:
    //todo: when should a property be upgraded to a object tag or vice versa?
    private def createBuildStartEvent(startedOn) {
        [
                'dateTime'  : startedOn,
                'location'  : ['lat': settings.latitude, 'long': settings.longitude],
                'objectTags': ['Computer', 'Software'],
                'actionTags': ['Build', 'Start'],
                'properties': ['Language'                    : languages, 'Environment': 'IntellijIdea12',
                               'TimeToDetectProjectLanguages': timeToDetectProjectLanguages,
                               'TotalFilesScanned'           : totalFilesScanned]
        ]
    }

    private def createBuildFinishEvent(compilationStatus, finishedOn, buildDuration) {
        [
                'dateTime'  : finishedOn,
                'location'  : ['lat': settings.latitude, 'long': settings.longitude],
                'objectTags': ['Computer', 'Software'],
                'actionTags': ['Build', 'Finish'],
                'properties': ['Result'                      : compilationStatus, 'Language': languages, 'Environment': 'IntellijIdea12',
                               'TimeToDetectProjectLanguages': timeToDetectProjectLanguages,
                               'TotalFilesScanned'           : totalFilesScanned,
                               'BuildDuration'               : buildDuration]
        ]
    }

    private def persist(Map event) {
        PlatformRepository.getInstance().insert(event, settings.writeToken);
    }
}

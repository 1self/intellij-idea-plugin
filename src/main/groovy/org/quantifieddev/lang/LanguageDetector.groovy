package org.quantifieddev.lang

import org.quantifieddev.utils.FileWalker

import java.nio.file.Path
import java.util.regex.Pattern

class LanguageDetector {
    private static def languageFileExtensions = [
        'csharp' : ['.cs', '.cshtml', '.csx'],
        'groovy':  ['.groovy'],
        'javascript' :  ['.js'],
        'java': ['.java']
    ]

    public static List<String> detectLanguages(Path projectRoot) {
        def walker = new FileWalker(projectRoot, Pattern.compile('.*'))
        def files = walker.walk()
        def fileExtensions = files.groupBy { file -> fileExtension(file) }.keySet()
        languageFileExtensions.findResults { language, extensions ->
            extensions.intersect(fileExtensions)? language : null
        }
    }

    private static fileExtension(String pathtofile) {
        pathtofile.substring(pathtofile.lastIndexOf('.'))
    }
}



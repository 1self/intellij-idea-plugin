package org.quantifieddev.lang

import org.quantifieddev.utils.DirWalker

import java.util.regex.Pattern

class LanguageDetector {
    private static def languageFileExtensions = [
        'csharp' : ['.cs', '.cshtml', '.csx'],
        'groovy':  ['.groovy'],
        'javascript' :  ['.js'],
        'java': ['.java'],
        'scala': ['.scala']
    ]

    public static List<String> detectLanguages(URI projectRoot) {
        def walker = new DirWalker(projectRoot, Pattern.compile('.*'))
        def files = walker.walk()
        def fileExtensions = files.groupBy { file -> fileExtension(file) }.keySet()
        languageFileExtensions.findResults { language, extensions ->
            extensions.intersect(fileExtensions)? language : null
        }
    }

    private static fileExtension(String pathtofile) {
        pathtofile.lastIndexOf('.')!=-1?pathtofile.substring(pathtofile.lastIndexOf('.')):pathtofile
    }
}



package co.oneself.lang

class LanguageDetector {
    public static def languageFileExtensions = [
        'csharp' : ['.cs', '.cshtml', '.csx'],
        'groovy':  ['.groovy', '.gsp'],
        'javascript' :  ['.js'],
        'java': ['.java'],
        'python': ['.py'],
        'ruby': ['.rb'],
        'scala': ['.scala']
    ]

    public static List<String> detectLanguages(List<String> files) {
        def fileExtensions = files.groupBy { file -> fileExtension(file) }.keySet()
        languageFileExtensions.findResults { language, extensions ->
            extensions.intersect(fileExtensions)? language : null
        }
    }

    private static fileExtension(String pathtofile) {
        pathtofile.lastIndexOf('.')!=-1?pathtofile.substring(pathtofile.lastIndexOf('.')):pathtofile
    }
}



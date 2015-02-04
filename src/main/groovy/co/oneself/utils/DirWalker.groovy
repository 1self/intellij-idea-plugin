package co.oneself.utils

import java.util.regex.Pattern

class DirWalker {

    private def allFiles = []
    private URI startDir
    private Pattern collectFilesRegex

    DirWalker(URI startDir, Pattern collectFilesRegex) {
        this.startDir = startDir
        this.collectFilesRegex = collectFilesRegex
    }

    List<String> walk() {
        allFiles.clear()
        walk0(new File(startDir))
        allFiles
    }

    private void walk0(File directory) {
        File[] files = directory.listFiles()
        files.each { file ->
            if(file.isDirectory()) {
                walk0(file)
            } else {
                def matcher = collectFilesRegex.matcher(file.name)
                if(matcher.matches()) {
                    allFiles << file.getName()
                }
            }
        }
    }
}

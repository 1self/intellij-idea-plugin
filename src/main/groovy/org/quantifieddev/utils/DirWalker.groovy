package org.quantifieddev.utils

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
        walk0(startDir)
        allFiles
    }

    private void walk0(URI directory) {
        File file = new File(directory)
        File[] files = file.listFiles()
        files.each { fileEntry ->
            if(fileEntry.isDirectory()) {
                walk0(fileEntry.toURI())
            } else {
                def matcher = collectFilesRegex.matcher(fileEntry.name)
                if(matcher.matches()) {
                    allFiles << fileEntry.getName()
                }
            }
        }
    }
}

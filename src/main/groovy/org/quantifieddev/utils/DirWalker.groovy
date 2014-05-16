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
        File file = new File(startDir)
        File[] files = file.listFiles()
        files.each { fileEntry ->
            if(fileEntry.isDirectory()) {
               def subDirFiles = new DirWalker(fileEntry.toURI(), collectFilesRegex).walk()
                allFiles += subDirFiles
            } else {
                def matcher = collectFilesRegex.matcher(fileEntry.name)
                if(matcher.matches()) {
                    allFiles += fileEntry.name
                }
            }
        }
        allFiles
    }
}

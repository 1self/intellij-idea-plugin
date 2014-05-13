package org.quantifieddev.utils

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Pattern

class FileWalker {

    private def files = []
    private Path startDir
    private Pattern collectFilesRegex

    FileWalker(Path startDir, Pattern collectFilesRegex) {
        this.startDir = startDir
        this.collectFilesRegex = collectFilesRegex
    }

    private def visitor = new SimpleFileVisitor<Path>() {
        FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts) {
            def file = path.toAbsolutePath().toString()
            def matcher = collectFilesRegex.matcher(file)
            if(matcher.matches()) {
                files += file
            }
            FileVisitResult.CONTINUE
        }

        FileVisitResult visitFileFailed(Path path, IOException exc) {
            FileVisitResult.CONTINUE
        }
    }

    List<String> walk() {
        files.clear()
        Files.walkFileTree(startDir, visitor)
        files
    }
}

package org.quantifieddev.utils

import org.quantifieddev.lang.LanguageDetector

import java.util.regex.Pattern


File start = new File('/Users/dhavald/Documents/workspace/quantifiedDev-IdeaPlugin')
URI startDir = start.toURI()
def walker = new DirWalker(startDir, Pattern.compile('(.*\\.js)|(.*\\.groovy)'))
println walker.walk()

println LanguageDetector.detectLanguages(startDir)

package org.quantifieddev.utils

import spock.lang.Specification

import java.util.regex.Pattern

class DirWalkerSpec extends Specification {

    def "it returns files matching specified regex in a given directory"() {
        given:
        def projectRoot = new File("src/test/resources").getAbsolutePath() + "/SampleProject"
        DirWalker dirWalker = new DirWalker(new URI("file://$projectRoot"), Pattern.compile("(.*\\.java)|(.*\\.groovy)"));

        when:
        def files = dirWalker.walk() as Set

        then:
        files == ["Sample.groovy", "Hello.java"] as Set
    }
}

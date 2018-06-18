package test.sonarqube

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class TestControllerSpec extends Specification implements ControllerUnitTest<TestController> {

    def setup() {
    }

    def cleanup() {
    }

    void "make codenarc whine"() {
        when:
        // CodeNarc will complain about the position of ':'
        Map map = [
            'hi': 'there'
        ]

        then:
        map.hi == 'there'
    }
}

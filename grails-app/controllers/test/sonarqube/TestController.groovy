package test.sonarqube

class TestController {

    def index() { }

    def test() { 
        // CodeNarc will complain about the position of ':'
        Map map = [
            'hi': 'there'
        ]
        return map
    }
}


This project tries to demonstrate that Gradle plugin "org.sonarqube" either needs
additional configuration for CodeNarc or should better interoperate with the
Gradle plugin "codenarc".

This repo reflects the final state of this README.md - all discussed changes applied.

For simplicity, I'm going to use a Grails project.

```
$ sdk use grails 3.3.6  
$ grails create-app test-sonarqube  
$ cd test-sonarqube  
$ grails create-controller test  
```

Edit ```grails-app/controllers/test/sonarqube/TestController.groovy```  
Add the following method to the class

```
def test() { 
  // CodeNarc will complain about the position of ':'
  Map map = [
    'hi': 'there'
  ]
  return map
}
```

Edit ```src/test/groovy/test/sonarqube/TestControllerSpec.groovy```  
* Remove or fix the "test something" method.
* Add the following method to the class

```
void "make codenarc whine"() {
  when:
  // CodeNarc will complain about the position of ':'
  Map map = [
    'hi': 'there'
  ]

  then:
  map.hi == 'there'
}
```

Tests should successfully run.

```
$ ./gradlew check
```

Edit ```build.gradle``` to add SonarQube support

```
apply plugin: "org.sonarqube"

buildscript {
    dependencies {
        ...
        classpath "org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.6.2"
    }
}

sonarqube {
   properties {
        property "sonar.host.url", "http://localhost:9000"
   }
}
```

Run sonarqube

```
$ ./gradlew sonarqube
```

This should run correctly.

Looking in SonarQube, I see 
* one unit test was run
* CodeNarc complained about one issue (although not the SpaceAroundMapEntryColon issue)

I wanted to obtain more control over what CodeNarc is checking, so I added the following items
to ```build.gradle```.

```
apply plugin: 'codenarc'

sonarqube {
   properties {
       ...
        property "sonar.groovy.codenarc.reportPath", "build/reports/codenarc/main.xml"
   }
}

codenarc {
    toolVersion = '0.25.2'
    configFile = file("${rootProject.projectDir}/config/codenarc/rules.groovy") 
    reportFormat = 'xml' 
    ignoreFailures = true 
}
```

Note above, I am specifying codenarc version 0.25.2 as that is what I believe the bundled codenarc within sonarqube (sonar-groovy) is using.

I added the file ```config/codenarc/rules.groovy``` with the below contents

```
ruleset {
    description 'Grails-CodeNarc Project RuleSet'

    ruleset('rulesets/basic.xml')
    ruleset('rulesets/braces.xml')
    ruleset('rulesets/convention.xml')
    ruleset('rulesets/design.xml')
    ruleset('rulesets/dry.xml')
    ruleset('rulesets/exceptions.xml')
    ruleset('rulesets/formatting.xml')
    ruleset('rulesets/generic.xml')
    ruleset('rulesets/imports.xml')
    ruleset('rulesets/naming.xml')
    ruleset('rulesets/unnecessary.xml')
    ruleset('rulesets/unused.xml')
    ruleset('rulesets/grails.xml')
}
```

Run sonarqube

```
$ ./gradlew sonarqube
```

and it won't find the codenarc reports. Predictable. Run again with manual execution of codenarc:

```
$ ./gradlew codenarcMain codenarcTest sonarqube
```

The codenarc reports now contain more problems. The sonarqube plugin is recognizing my codenarc xml report files, but it doesn't recognize any of the codenarc errors. Looking in sonarqube, codenarc issues are no longer present.

Is there a way to either
1. Make sonar-groovy more configurable so I can control what codenarc tests are run?
2. Make it so reading the codenarc reports doesn't produce
   ```"No such rule in SonarQube, so violation from CodeNarc will be ignored: ..."```
   for every codenarc issue?

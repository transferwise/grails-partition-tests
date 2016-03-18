import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport

scriptEnv="test"
includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsTest")
includeTargets << grailsScript("TestApp")

USAGE = """
partition-test [grails test arguments] "--split=<splitNumber>" "--totalSplits=<totalSplits>"

where
grails test arguments: Any command line options available when running 'grails test-app'
splitNumber: The current partition/split to run from a total of 'totalSplits'.
totalSplits: The total number of partitions/splits
I.e. split 1 of totalSplits 3 would run approx 1/3 of the applications tests

Sample usage: grails partitionTest unit:spock "--split=1" "--totalSplits=2" --verbose
"""

def log = {msg ->
    grailsConsole.log(msg)
}
def error = {msg ->
    grailsConsole.error(msg)
}

//usage: grails partitionTest unit "--testNames=SomeTest,AnotherTest"
target(partitionTests: "Splits all Grails test files based on the testNames argument.") {
	if (!argsMap.testNames) {
		error('testNames must be provided')
		exit(1)
	}

	def testNames = argsMap.testNames as String
	def testNamesList = testNames.split(",")
	log "testNamesList: " + testNamesList
	grailsConsole.addStatus("Ready to compile '${testNamesList.size()}' tests for split run")

	GrailsTestTypeSupport.metaClass.eachSourceFile = { Closure body ->
		testNamesList.each { String testName ->
			body(testName)
		}
	}

    log "** Running Tests in partition mode. Tests: " + testNamesList + " **"
    if (!argsMap.skip) {
        log("Handing off to grails test-app")

        /* Calls default target in TestApp.groovy
        * A but nasty because if any other targets with name 'default' are included which one gets called?
        * Don't want to repeat all the logic in TestApp.groovy to resolve test types, phases, arguments, etc.
        */
        depends('default')
    } else{
        log('skipping test phase for some reason')
    }
}
setDefaultTarget(partitionTests)

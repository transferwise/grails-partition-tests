import org.codehaus.groovy.grails.test.GrailsTestTargetPattern

includeTargets << grailsScript("_GrailsEvents")

def log = {msg ->
	grailsConsole.log(msg)
}
def error = {msg ->
	grailsConsole.error(msg)
}



def getSplitter(Integer currentSplit, String testReportsUrl, ClassLoader classLoader, List<String> testNames,
                String testTypeName, Binding buildBinding) {
	def isSmartSplit = testReportsUrl ? true : false

	//[raw: **.*, filePattern: **/*, classPattern: **.*, methodName: null]
	List<GrailsTestTargetPattern> testTargetPatterns = []
	for (def testName in testNames)
		testTargetPatterns << new GrailsTestTargetPattern(testName)


	def splitter = null
	if (isSmartSplit) {
		def splitClass = classLoader.loadClass('grails.plugin.partitiontests.VoodooTestSplitter')
		splitter = splitClass.newInstance(currentSplit, testReportsUrl, testTargetPatterns, testTypeName, buildBinding)
	}
	else {
		def splitClass = classLoader.loadClass('grails.plugin.partitiontests.GrailsTestSplitter')
		splitter = splitClass.newInstance(currentSplit, testTargetPatterns, testTypeName, buildBinding)
	}

	return splitter
}

eventTestCompileStart = {testType ->
	log "eventTestCompileStart called"
	//log "testNAmes: " + testNames
	//log "testTargetPatterns: " + testTargetPatterns
	//log "targetpatterns: " + testTargetPatterns
	println "eventTestcompileStart"
	println "binding: " + binding
    if (!binding.hasVariable('testReportsUrl')) {
        return
    }

	println "calculating buckets"
	def totalBuckets = 0

	def testTypeName = 'spock' // FIXME: get it from GrailsTestType.getName() [maybe the method has been renamed?]

	// 30 is the maximum we're willing to do
	for (def i in 1..30) {
		def splitter = getSplitter(i, testReportsUrl, classLoader, ["**.*Spec"], testTypeName, binding)
		grailsConsole.addStatus("Calculating split #${i} with $splitter")

		def testNamesList = [] // splitter.eachSourceFileHotReplace
		splitter.eachSourceFileHotReplace { _, File testFile  ->
			def testName = testFile.name
			println "testName: " + testName
			testName = testName.replaceAll(".groovy", "")
			println "testName now: " + testName
			def pathParts = testFile.path.split("/")
			println "pathParts: " + pathParts
			def comStartsAt = pathParts.findIndexOf { it == 'com' }
			println "comStartsAt: " + comStartsAt
			def packageParts = pathParts[comStartsAt..pathParts.size()-2]
			println "packageParts: " + packageParts
			def packageName = packageParts.join(".")
			println "packageName: " + packageName
			def fullTestName = "${packageName}.${testName}"
			println "fullTestName: " + fullTestName
			testNamesList << fullTestName
		}

		if (!testNamesList)
			break

		def testNamesString = testNamesList.join(" ")
		log "BUCKET${i}=${testNamesString}"
		totalBuckets++
	}

	log "TOTAL_BUCKETS=${totalBuckets}"

	System.exit(0)
/*
    //grailsConsole.addStatus("Adding split support to grails test type: ${testType.class}")
   // Integer totalSplits = Integer.valueOf(binding.getVariable('totalSplits'))
    //Integer split = Integer.valueOf(binding.getVariable('split'))

    /*try {
	    //classLoader.loadClass('grails.plugin.partitiontests.ScriptsHelper')
        //def splitter = ScriptsHelper.getSplitter()

        testType.metaClass.eachSourceFile = splitter.eachSourceFileHotReplace
        testType.metaClass.testSplitter  = splitter

    } catch (Throwable t) {
        grailsConsole.error("Could not add split support", t)
    }*/
    //grailsConsole.addStatus("Ready to complie '${testType.name}' tests for split run")
}

eventTestCompileEnd = {testType ->
    event("SplitTestTestCompileEnd", [testType])
}

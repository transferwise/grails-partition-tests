import org.codehaus.groovy.grails.test.GrailsTestTargetPattern
import org.codehaus.groovy.grails.test.GrailsTestType

includeTargets << grailsScript("_GrailsEvents")

def log = { msg ->
	grailsConsole.log(msg)
}
def error = { msg ->
	grailsConsole.error(msg)
}



def getSplitter(Integer currentSplit, Integer totalSplits, String testReportsUrl, ClassLoader classLoader,
                Binding buildBinding, GrailsTestType testType) {
	List<String> testNames = ["**.*Spec"]
	def isSmartSplit = testReportsUrl ? true : false

	//[raw: **.*, filePattern: **/*, classPattern: **.*, methodName: null]
	List<GrailsTestTargetPattern> testTargetPatterns = []
	for (def testName in testNames)
		testTargetPatterns << new GrailsTestTargetPattern(testName)

	def testTypeName = testType.getName()

	def splitter = null
	if (isSmartSplit) {
		def splitClass = classLoader.loadClass('grails.plugin.partitiontests.VoodooTestSplitter')
		splitter = splitClass.newInstance(currentSplit, totalSplits, testReportsUrl, testTargetPatterns, testTypeName, buildBinding)
	}
	else {
		def splitClass = classLoader.loadClass('grails.plugin.partitiontests.GrailsTestSplitter')
		splitter = splitClass.newInstance(currentSplit, totalSplits, testTargetPatterns, testTypeName, buildBinding)
	}

	grailsConsole.addStatus("Calculating split #${currentSplit} with $splitter")
	return splitter
}


void doCalculateSplits(String testReportsUrl, GrailsTestType testType) {
	println "Calculating splits"

	if (!testReportsUrl) {
		// todo: fall back to the regular GrailsTestSplitter in this case. it will use 10 buckets.
		println "testReportsUrl is not set, can't calculate splits"
		return
	}

	def totalBuckets = 0

	// 30 is the maximum we're willing to do
	for (def i in 1..30) {
		def splitter = getSplitter(i, null, testReportsUrl, classLoader, binding, testType)


		def testNamesList = [] // splitter.eachSourceFileHotReplace
		splitter.eachSourceFileHotReplace { _, File testFile  ->
			def testName = testFile.name
			//println "testName: " + testName
			testName = testName.replaceAll(".groovy", "")
			//println "testName now: " + testName
			def pathParts = testFile.path.split("/")
			//println "pathParts: " + pathParts
			def comStartsAt = pathParts.findIndexOf { it == 'com' }
			//println "comStartsAt: " + comStartsAt
			def packageParts = pathParts[comStartsAt..pathParts.size()-2]
			//println "packageParts: " + packageParts
			def packageName = packageParts.join(".")
			//println "packageName: " + packageName
			def fullTestName = "${packageName}.${testName}"
			//println "fullTestName: " + fullTestName
			testNamesList << fullTestName
		}

		if (!testNamesList)
			break

		def testNamesString = testNamesList.join(" ")
		println "BUCKET${i}=${testNamesString}"
		totalBuckets++
	}

	println "TOTAL_BUCKETS=${totalBuckets}"

	// We don't want to actually run the tests here yet.
	System.exit(0)
}

void doPartitionTests(String testReportsUrl, testType) {
	if (!binding.hasVariable('totalSplits') || !binding.hasVariable('split')) {
		return
	}

	grailsConsole.addStatus("Adding split support to grails test type: ${testType.class}")
	Integer totalSplits = Integer.valueOf(binding.getVariable('totalSplits'))
	Integer split = Integer.valueOf(binding.getVariable('split'))

	def splitter = getSplitter(split, totalSplits, testReportsUrl, classLoader, binding, testType)

	testType.metaClass.eachSourceFile = splitter.eachSourceFileHotReplace
	testType.metaClass.testSplitter = splitter

	grailsConsole.addStatus("Done adding split support")
}

eventTestCompileStart = { testType ->
	log "eventTestCompileStart called"

	def testReportsUrl = null
	if (binding.hasVariable('testReportsUrl')) {
		testReportsUrl = binding.getVariable('testReportsUrl') as String
	}

	if (binding.hasVariable("doingCalculateSplits")) {
		doCalculateSplits(testReportsUrl, testType)
		return
	}

	doPartitionTests(testReportsUrl, testType)
}

eventTestCompileEnd = {testType ->
    event("SplitTestTestCompileEnd", [testType])
}

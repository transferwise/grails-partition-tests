package grails.plugin.partitiontests

class TestResultBucket {
	int totalTime
	List<TestResult> testResults = []

	def addTestResult(TestResult testResult) {
		testResults << testResult
		totalTime += testResult.time
	}

	String toString() {
		"[Total $totalTime ms] $testResults"
	}
}

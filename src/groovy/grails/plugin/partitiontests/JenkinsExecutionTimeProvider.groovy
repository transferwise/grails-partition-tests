package grails.plugin.partitiontests

import net.sf.json.groovy.JsonSlurper

class JenkinsExecutionTimeProvider implements IExecutionTimeProvider {

	def testResults

	JenkinsExecutionTimeProvider(String jenkinsUrl) {
		testResults = new JsonSlurper().parse(new URL(jenkinsUrl))
	}

	@Override
	int getExecutionTime(String testName) {
		def suite = testResults.suites.find {
			it.name.split("\\.").last() == testName
		}

		return suite ? suite.duration : getAverageExecutionTime()
	}

	private int getAverageExecutionTime() {
		testResults.duration / testResults.passCount
	}
}

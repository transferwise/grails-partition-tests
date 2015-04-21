package grails.plugin.partitiontests


class ArchiveExecutionTimeProvider implements IExecutionTimeProvider {

	List<TestResult> testReports

	ArchiveExecutionTimeProvider(TestReportProvider testReportProvider) {
		this.testReports = parseSource(testReportProvider.testReports)
	}

	int getExecutionTime(String testName) {
		TestResult testResult = testReports.find {
			it.name == testName
		}

		return testResult ? testResult.time : getAverageExecutionTime()
	}

	private int getAverageExecutionTime() {
		testReports.collect {
			it.time
		}.sum() / testReports.size()
	}

	private List<TestResult> parseSource(List<InputStream> testReports) {
		testReports.collect {
			parseReport(it)
		}
	}

	private TestResult parseReport(InputStream reportFile) {
		def report = new XmlSlurper().parse(reportFile)
		return new TestResult(
				name: report.@name.toString().split('\\.').last(),
				time: (int) (Double.parseDouble(report.@time.toString()) * 1000))
	}
}

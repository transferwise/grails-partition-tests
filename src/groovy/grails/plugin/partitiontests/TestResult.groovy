package grails.plugin.partitiontests

class TestResult {
	String name
	int time

	String toString() {
		"${name}[${time}]"
	}
}

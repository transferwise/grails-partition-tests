package grails.plugin.partitiontests

class TimedTestFile {
	File file
	int time

	String toString() {
		return "[$time] $file.name"
	}
}

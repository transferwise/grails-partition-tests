package grails.plugin.partitiontests

class FileBucket {
	int totalTime
	List<TimedTestFile> files = []

	def addFile(TimedTestFile file) {
		files << file
		totalTime += file.time
	}

	String toString() {
		"[$totalTime] $files"
	}
}

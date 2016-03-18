package grails.plugin.partitiontests

class VoodooTestSplitter extends GrailsTestSplitter {

    private IExecutionTimeProvider executionTimeProvider

    VoodooTestSplitter(Integer currentSplit, String testReportUrl, List testTargetPatterns, String testTypeName,
                       Binding buildBinding) {
        this(currentSplit, new JenkinsExecutionTimeProvider(testReportUrl), testTargetPatterns, testTypeName, buildBinding)
    }

    VoodooTestSplitter(Integer currentSplit, IExecutionTimeProvider executionTimeProvider, List testTargetPatterns,
                       String testTypeName, Binding buildBinding) {
        super(currentSplit, testTargetPatterns, testTypeName, buildBinding)
        this.executionTimeProvider = executionTimeProvider
    }

    List getFilesForThisSplit(allSourceFiles) {
        List<FileBucket> buckets = putTestsIntoBuckets(allSourceFiles)
        if (buckets.size() < currentSplit) {
            return null
        }

        buckets.get(currentSplit - 1).files.collect {
            it.file
        }
    }

    List<FileBucket> putTestsIntoBuckets(List<File> testFiles) {
        println 'Test files:'
        testFiles.each {
            println "->$it"
        }

        List<FileBucket> buckets = splitTestFilesIntoBuckets(assignTimesToTestFiles(testFiles))

        println "Final file buckets:"
        buckets.each {
            println "->$it"
        }

        return buckets
    }

    def List<TimedTestFile> assignTimesToTestFiles(List<File> testFiles) {
        testFiles.collect {
            new TimedTestFile(file: it, time: executionTimeProvider.getExecutionTime(it.name.split("\\.")[0]))
        }
    }

    List<FileBucket> splitTestFilesIntoBuckets(List<TimedTestFile> testFiles) {
	    testFiles.sort{ x, y ->
		    if (x.time == y.time){
			    x.file.name <=> y.file.name
		    } else {
			    x.time <=> y.time
		    }
	    }
        TimedTestFile max = testFiles.get(testFiles.size() - 1)
        List<FileBucket> buckets = []

        while (true) {
            FileBucket bucket = new FileBucket()
            TimedTestFile fileToAdd = testFiles.remove(testFiles.size() - 1)
            bucket.addFile(fileToAdd)

            while (!testFiles.isEmpty() && bucket.totalTime + testFiles.get(0).time <= max.time) {
                bucket.addFile(testFiles.remove(0))
            }

            buckets.add(bucket);

            if (testFiles.isEmpty()) {
                break
            }
        }

        println "Buckets:"
        buckets.each {
            println "->$it"
        }

        return buckets
    }
}

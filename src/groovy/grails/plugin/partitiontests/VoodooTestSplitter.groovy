package grails.plugin.partitiontests

class VoodooTestSplitter extends GrailsTestSplitter {

    private IExecutionTimeProvider executionTimeProvider

    VoodooTestSplitter(Integer currentSplit, Integer totalSplits, String testReportUrl, List testTargetPatterns, String testTypeName,
                       Binding buildBinding) {
        this(currentSplit, totalSplits, new JenkinsExecutionTimeProvider(testReportUrl), testTargetPatterns, testTypeName, buildBinding)
    }

    VoodooTestSplitter(Integer currentSplit, Integer totalSplits, IExecutionTimeProvider executionTimeProvider, List testTargetPatterns,
                       String testTypeName, Binding buildBinding) {
        super(currentSplit, totalSplits, testTargetPatterns, testTypeName, buildBinding)
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

        if (totalSplits != null && buckets.size() > totalSplits) {
            println "Ooops, we have only $totalSplits available, but could use $buckets.size"
            adjustNumberOfBuckets(buckets)
        }

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

    def adjustNumberOfBuckets(List<FileBucket> buckets) {
        if (totalSplits != null && totalSplits < buckets.size()) {
            spreadSmallestBucket(buckets)
            adjustNumberOfBuckets(buckets)
        }
    }

    def spreadSmallestBucket(List<FileBucket> buckets) {
        buckets.sort { it.totalTime }
        FileBucket smallestBucket = buckets.remove(0)

        smallestBucket.files.eachWithIndex { it, i ->
            buckets.getAt(-1 * i).addFile(it)
        }
    }

    List<FileBucket> splitTestFilesIntoBuckets(List<TimedTestFile> testFiles) {
        /* When two jobs have the same build time, sorting is indeterministic. This means a test could end up
           in two different buckets. Sorting by name as well to avoid this situation. */
	    testFiles.sort { x, y ->
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

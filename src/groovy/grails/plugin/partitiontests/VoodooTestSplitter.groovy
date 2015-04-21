package grails.plugin.partitiontests

class VoodooTestSplitter extends GrailsTestSplitter {

    private IExecutionTimeProvider executionTimeProvider

    VoodooTestSplitter(Integer currentSplit, Integer totalSplits, String testReportUrl) {
        this(currentSplit, totalSplits, new JenkinsExecutionTimeProvider(testReportUrl))
    }

    VoodooTestSplitter(Integer currentSplit, Integer totalSplits, IExecutionTimeProvider executionTimeProvider) {
        super(currentSplit, totalSplits)
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

        if (buckets.size() > totalSplits) {
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
        if (totalSplits < buckets.size()) {
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
        testFiles.sort { it.time }
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

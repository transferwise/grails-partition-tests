package grails.plugin.partitiontests

import org.apache.commons.io.comparator.CompositeFileComparator
import org.apache.commons.io.comparator.DirectoryFileComparator
import org.apache.commons.io.comparator.NameFileComparator
import org.apache.commons.io.comparator.PathFileComparator
import org.apache.commons.io.comparator.SizeFileComparator
import org.codehaus.groovy.grails.test.GrailsTestTargetPattern

class GrailsTestSplitter {
    Integer currentSplit
    Integer totalSplits
	String testTypeName
	List<GrailsTestTargetPattern> testTargetPatterns
	Binding buildBinding

    GrailsTestSplitter(Integer currentSplit, Integer totalSplits, List testTargetPatterns, String testTypeName,
                       Binding buildBinding) {
        this.currentSplit = currentSplit
        this.totalSplits = totalSplits
	    this.testTargetPatterns = testTargetPatterns
	    this.testTypeName = testTypeName
	    this.buildBinding = buildBinding
    }

    List getFilesForThisSplit(allSourceFiles) {
        def collated = collateSourceFiles(allSourceFiles, totalSplits)
        return collated.get(currentSplit - 1)
    }

    List collateSourceFiles(List candidates, splitCount) {
        //Sort should be as deterministic as possible - size and path
        if (splitCount > 0) {
            CompositeFileComparator comparator = new CompositeFileComparator(
                    SizeFileComparator.SIZE_REVERSE,
                    PathFileComparator.PATH_COMPARATOR)
            List sorted = comparator.sort(candidates)
            List buckets = distributeToBuckets(splitCount, sorted)
            int resultSize = 0
            buckets.each {List l -> resultSize += l.size() }
            //Don't want to lose any tests
            assert resultSize == candidates.size()
            return buckets
        } else {
            []
        }
    }

    List distributeToBuckets(Integer bucketSize, List list) {
        if (!bucketSize) {
            throw new IllegalArgumentException("Bucket size not specified")
        }

        List buckets = (0..<bucketSize).collect {[]}
        int bucketIndex = 0
        list.each {f ->

            buckets.get(bucketIndex).add(f)
            bucketIndex++
            if (bucketIndex == bucketSize) {
                bucketIndex = 0
            }
        }
        return buckets
    }

    def eachSourceFileHotReplace = {Closure body ->
	    //println "testTargetPatterns: " + testTargetPatterns
	    testTargetPatterns.each { GrailsTestTargetPattern testTargetPattern ->
		    def debugString = "Getting sources files for split: ${currentSplit}"
		    if (totalSplits != null)
			    debugString += "of ${totalSplits}"

		    debugString += " | Test type: ${testTypeName} | Test target pattern: ${testTargetPattern}"
            println(debugString)

		    def specFinder = new SpecFinder(buildBinding)
            List allFiles = specFinder.getTestClassNames(testTargetPattern)
            println("All source files size: ${allFiles?.size()}")

            if (allFiles && !allFiles.isEmpty()) {
                def splitSourceFiles = getFilesForThisSplit(allFiles)
                splitSourceFiles.each { sourceFile ->
                    body(testTargetPattern, sourceFile)
                }
            }
        }
    }
}

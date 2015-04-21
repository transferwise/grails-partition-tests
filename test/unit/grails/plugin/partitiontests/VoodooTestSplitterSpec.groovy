package grails.plugin.partitiontests

import spock.lang.Specification

class VoodooTestSplitterSpec extends Specification {

    class MockExecutionTimeProvider implements IExecutionTimeProvider {

        @Override
        int getExecutionTime(String testName) {
            return (testName[-1] as Integer) * 10
        }
    }

    def "Files should be split according to previous results"(split, expectedFiles) {
        given:
            File testSourceDir = File.createTempDir()
            def files = []
            (1..6).each {
                files << new File("Spec${it}.groovy", testSourceDir)
            }
            Collections.shuffle(files)

        when:
            GrailsTestSplitter grailsTestSplitter = new VoodooTestSplitter(split, 4, new MockExecutionTimeProvider())
            List splitResults = grailsTestSplitter.getFilesForThisSplit(files)

        then:
            println splitResults
            def splitResultFileNames = splitResults.collect { it.name }
            splitResultFileNames == expectedFiles
        where:
            split | expectedFiles
            1     | ["Spec6.groovy"]
            2     | ["Spec5.groovy", "Spec1.groovy"]
            3     | ["Spec4.groovy", "Spec2.groovy"]
            4     | ["Spec3.groovy"]
    }

    def "Should spread tests if not enough buckets"(split, expectedFiles) {
        given:
            File testSourceDir = File.createTempDir()
            def files = []
            (1..6).each {
                files << new File("Spec${it}.groovy", testSourceDir)
            }
            Collections.shuffle(files)

        when:
            GrailsTestSplitter grailsTestSplitter = new VoodooTestSplitter(split, 3, new MockExecutionTimeProvider())
            List splitResults = grailsTestSplitter.getFilesForThisSplit(files)

        then:
            println splitResults
            def splitResultFileNames = splitResults.collect { it.name }
            splitResultFileNames == expectedFiles
        where:
            split | expectedFiles
            1     | ["Spec6.groovy", "Spec3.groovy"]
            2     | ["Spec5.groovy", "Spec1.groovy"]
            3     | ["Spec4.groovy", "Spec2.groovy"]
    }

    def "New files should be put to smallest bucket"(split, expectedFiles) {
        given:
            File testSourceDir = File.createTempDir()
            def files = []
            (1..7).each {
                files << new File("Spec${it}.groovy", testSourceDir)
            }
            Collections.shuffle(files)

        when:
            GrailsTestSplitter grailsTestSplitter = new VoodooTestSplitter(split, 4, new MockExecutionTimeProvider())
            List splitResults = grailsTestSplitter.getFilesForThisSplit(files)

        then:
            println splitResults
            def splitResultFileNames = splitResults.collect { it.name }
            splitResultFileNames == expectedFiles
        where:
            split | expectedFiles
            1     | ["Spec7.groovy"]
            2     | ["Spec6.groovy", "Spec1.groovy"]
            3     | ["Spec5.groovy", "Spec2.groovy"]
            4     | ["Spec4.groovy", "Spec3.groovy"]
    }

    def "survives when has too many buckets"(split, expectedFiles) {
        given:
            File testSourceDir = File.createTempDir()
            def files = []
            (1..7).each {
                files << new File("Spec${it}.groovy", testSourceDir)
            }
            Collections.shuffle(files)

        when:
            GrailsTestSplitter grailsTestSplitter = new VoodooTestSplitter(split, 5, new MockExecutionTimeProvider())
            List splitResults = grailsTestSplitter.getFilesForThisSplit(files)

        then:
            println splitResults
            def splitResultFileNames = splitResults.collect { it.name }
            splitResultFileNames == expectedFiles
        where:
            split | expectedFiles
            1     | ["Spec7.groovy"]
            2     | ["Spec6.groovy", "Spec1.groovy"]
            3     | ["Spec5.groovy", "Spec2.groovy"]
            4     | ["Spec4.groovy", "Spec3.groovy"]
            5     | []
    }
}

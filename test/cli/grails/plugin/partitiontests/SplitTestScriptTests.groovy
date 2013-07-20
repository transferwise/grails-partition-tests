package grails.plugin.partitiontests
import grails.test.AbstractCliTestCase
import org.junit.Ignore

/**
 * Run with: test-app --other
 * See target/cli-output for any output generated by the script
 * CLI script testing
 * http://www.cacoethes.co.uk/blog/groovyandgrails/testing-your-grails-scripts
 */
class SplitTestScriptTests extends AbstractCliTestCase{
 def scriptName = 'partition-test'
    @Override
    protected void setUp() {
        timeout = 10000
    }

    @Override
    protected void tearDown() {
        println "--------------------------------Script output---------------------${output}"
    }
    void testShouldSoThisTestTypeDoesntExplodeGrailsTestApp(){
        assert true
    }

    @Ignore
    void testShouldRequireBothArgs(){
        execute([scriptName,  '--skip'])
        assert 0 == waitForProcess()
        verifyHeader()
        assert output.contains('split and totalSplits must be suppplied')
    }

    @Ignore
    void testShouldRequireCurrentSplitLessThanTotalSplits(){
        execute([scriptName,  '--skip', "--split=2",  "--totalSplits=1"])
        assert 0 == waitForProcess()
        assert output.contains(' must not be greater than totalSplits')
    }

    @Ignore
    void testShouldNotAllowNegativeSplits(){
        execute([scriptName,  '--skip', "--split=-1",  "--totalSplits=-3"])
        assert 0 == waitForProcess()
        assert output.contains('Split arguments must not be negative!')
    }

    @Ignore
    void testValidSplits(){
        execute([scriptName,  '--skip', "--split=1",  "--totalSplits=3"])
        assert 0 == waitForProcess()
        assert output.contains("** Running Tests in split mode. Rinning split (1) of (${3}) split's**")
    }
}


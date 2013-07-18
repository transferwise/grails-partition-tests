package grails.plugin.splittest

import org.codehaus.groovy.grails.test.GrailsTestType
import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.junit4.result.JUnit4ResultGrailsTestTypeResultAdapter
import org.junit.runner.Result

import java.util.concurrent.Callable

class GrailsTestTypeRunner implements Callable<GrailsSplitTestTypeResult>{
    GrailsTestType testType
    def testEventPublisher
    String phase
    Integer split
    Integer shard
    List splitTestClasses
    List shardTestClasses


    @Override
    GrailsSplitTestTypeResult call() {

        GrailsSplitTestTypeResult result = new GrailsSplitTestTypeResult()
        result.start = new Date()
        if(shard == 1){
            Thread.sleep(2000L)
        }else{
            Thread.sleep(5000L)
        }
        result.phase = phase
        result.split = split
        result.shard = shard
        result.shardTestClasses = shardTestClasses
        result.splitTestClasses = splitTestClasses
        GrailsTestTypeResult grailsTestTypeResult = testType.run(testEventPublisher)
        result.finish = new Date()
        result.grailsTestTypeResult = grailsTestTypeResult//new JUnit4ResultGrailsTestTypeResultAdapter(new Result())
        return result
    }
}
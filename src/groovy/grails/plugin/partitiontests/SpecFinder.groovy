package grails.plugin.partitiontests

import org.codehaus.groovy.grails.test.GrailsTestTargetPattern
import org.codehaus.groovy.grails.test.GrailsTestTypeResult
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport

class SpecFinder extends GrailsTestTypeSupport {
	SpecFinder(binding) {
		super('name', 'functional')
		buildBinding = binding
	}

	int doPrepare() {
		0
	}

	GrailsTestTypeResult doRun(GrailsTestEventPublisher eventPublisher) {
		null
	}

	Collection<File> getTestClassNames(GrailsTestTargetPattern pattern) {
		findSourceFiles(pattern)
	}
}

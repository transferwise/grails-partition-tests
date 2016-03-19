scriptEnv="test"
// Probably not all of these scripts are required.
includeTargets << grailsScript("_GrailsInit")
includeTargets << grailsScript("_GrailsClasspath")
includeTargets << grailsScript("_GrailsEvents")
includeTargets << grailsScript("_GrailsTest")
includeTargets << grailsScript("TestApp")

classpath()

def log = {msg ->
	grailsConsole.log(msg)
}
def error = {msg ->
	grailsConsole.error(msg)
}


target(calculateSplits: "Splits tests into buckets and returns the number of required buckets") {
	String testReportsUrl = argsMap.testReportsUrl

	if (testReportsUrl) {
		log("Using testReportsUrl for splitting tests")
		getBinding().setVariable("testReportsUrl", testReportsUrl)
	}

	getBinding().setVariable("doingCalculateSplits", true)

	//log "Calling depends(default)"
	depends('default')

}

setDefaultTarget(calculateSplits)

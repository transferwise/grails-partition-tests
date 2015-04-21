package grails.plugin.partitiontests

interface IExecutionTimeProvider {

	int getExecutionTime(String testName)
}
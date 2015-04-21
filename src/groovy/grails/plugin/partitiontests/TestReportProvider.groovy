package grails.plugin.partitiontests

import org.apache.commons.io.IOUtils

import java.util.zip.ZipFile

class TestReportProvider {

	String url
	File workDir = new File("work")

	TestReportProvider(String url) {
		this.url = url
	}

	File download(address) {
		println "Downloading test reports from $address"
		workDir.delete()
		workDir.mkdir()
		File archive = new File("archive.zip", workDir)
		def file = new FileOutputStream(archive)
		def out = new BufferedOutputStream(file)
		out << new URL(address).openStream()
		out.close()
		return archive
	}

	List<InputStream> unzip(File file) {
		ZipFile zipFile = new ZipFile(file)
		zipFile.entries().toList().findAll() {
			if (it.name.endsWith(".xml")) {
				println "Found report $it.name"
				true
			}
		}.collect {
			zipFile.getInputStream(it)
		}
	}

	List<InputStream> getTestReports() {
		unzip(download(url))
	}
}

import groovy.transform.SourceURI

import java.nio.file.Paths
import java.time.LocalDate

@SourceURI URI sourceUri
def me = Paths.get(sourceUri)
def versionPropertiesFile = me.parent.resolve('version.properties')

def version = LocalDate.now().format('Y.MM.dd')

def template = """\
version=${version}
downloadUrl=https://github.com/macmarrum/freeplane-ScriptExecutionOnMindmapOpen/releases/download/v${version}/ScriptExecutionOnMindmapOpen-${version}.addon.mm
freeplaneVersionFrom=1.7.10
"""
println template
versionPropertiesFile.text = template
println "saved to ${versionPropertiesFile}"

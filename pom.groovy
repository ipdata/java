project {
  modelVersion '4.0.0'
  groupId 'co.ipdata.client'
  artifactId 'ipdata-java-client'
  version '0.1.0-SNAPSHOT'
  licenses {
    license {
      name 'The Apache Licence, Version 2.0'
      url 'http://www.apache.org/licenses/LICENCE-2.0.txt'
    }
  }
  scm {
    connection 'git@github.com:yassine/ipdata-java-client.git'
    developerConnection 'git@github.com:yassine/ipdata-java-client.git'
    url 'https://github.com/yassine/ipdata-java-client'
  }
  developers {
    developer {
      name 'ipdata.co'
      email 'support@ipdata.co'
    }
  }
  properties {
    'project.build.sourceEncoding' 'UTF-8'
    'maven.compiler.source' '6'
    'maven.compiler.target' '6'
    'version.client.feign' '9.7.0'
    'version.build.jacoco' '0.8.4'
    'version.build.surefire' '3.0.0-M3'
    'version.build.sonar' '3.7.0.1746'
    'sonar.organization' 'yassine-github'
    'sonar.coverage.jacoco.xmlReportPaths' '${project.build.directory}/site/code-coverage/jacoco.xml'
    'sonar.links.homepage' 'https://github.com/yassine/ipdata-java-client'
    'sonar.links.issue' 'https://github.com/yassine/ipdata-java-client'
    'sonar.links.scm' 'https://github.com/yassine/ipdata-java-client'
    'sonar.projectKey' 'yassine_ipdata-java-client'
    'sonar.projectName' 'ipdata-java-client'
    'sonar.projectVersion' '${project.version}'
    'sonar.host.url' 'https://sonarcloud.io'
  }
  dependencies {
    dependency('io.github.openfeign:feign-core:${version.client.feign}')
    dependency('io.github.openfeign:feign-jackson:${version.client.feign}')
    dependency('io.github.openfeign:feign-httpclient:${version.client.feign}')
    dependency('com.google.guava:guava:20.0')
    dependency('org.slf4j:slf4j-api:1.7.30')
    dependency('org.slf4j:slf4j-log4j12:1.7.30')
    dependency('org.projectlombok:lombok:1.18.10')
    /* testing */
    dependency('org.hamcrest:hamcrest:2.2:test')
    dependency('junit:junit:4.13:test')
    dependency('org.skyscreamer:jsonassert:1.5.0:test')
  }
  profiles {
    profile {
      id 'release'
      properties {
        'gpg.executable' 'gpg2'
      }
      build {
        plugins {
          plugin('org.apache.maven.plugins:maven-gpg-plugin:1.6') {
            executions {
              execution {
                phase 'verify'
                goals 'sign'
              }
            }
          }
        }
      }
    }
  }
  build {
    plugins {
      plugin('org.apache.maven.plugins:maven-resources-plugin:2.6') {
        configuration {
          encoding '${project.build.sourceEncoding}'
        }
      }
      plugin('org.jacoco:jacoco-maven-plugin:${version.build.jacoco}') {
        executions {
          execution {
            id 'prepare-agent'
            phase 'test-compile'
            goals 'prepare-agent'
            configuration {
              propertyName 'surefireArgLine'
              destFile '${project.build.directory}/coverage-reports/jacoco-ut.exec'
            }
          }
          execution {
            id 'post-test-reports'
            phase 'post-integration-test'
            goals 'report'
            configuration {
              dataFile '${project.build.directory}/coverage-reports/jacoco-ut.exec'
              outputDirectory '${project.reporting.outputDirectory}/code-coverage'
            }
          }
        }
      }
      plugin {
        artifactId 'maven-surefire-plugin'
        version '${version.build.surefire}'
        configuration {
          useFile 'false'
          includes {}
          additionalClasspathElements {
            additionalClasspathElement '${project.basedir}/src/test/resources'
            additionalClasspathElement '${project.build.testOutputDirectory}'
          }
          argLine '${surefireArgLine}'
        }
      }
      plugin('org.codehaus.mojo:sonar-maven-plugin:${version.build.sonar}')
      plugin('org.apache.maven.plugins:maven-source-plugin:3.2.1') {
        executions {
          execution('attach-sources') {
            goals('jar')
          }
        }
      }
      plugin('org.apache.maven.plugins:maven-javadoc-plugin:3.1.1') {
        executions {
          execution('attach-javadocs') {
            goals('jar')
          }
        }
      }
    }
    resources {
      resource {
        directory 'src/main/resources'
        filtering true
        includes('io/ipdata/client/VERSION')
      }
    }
  }
}

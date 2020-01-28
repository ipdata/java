project {
  modelVersion '4.0.0'
  groupId 'io.ipdata.client'
  artifactId 'ipdata-java-client'
  version '0.1.0-SNAPSHOT'
  properties {
    'project.build.sourceEncoding' 'UTF-8'
    'maven.compiler.source' '6'
    'maven.compiler.target' '6'
    'version.client.feign' '9.7.0'
    'version.build.jacoco' '0.8.4'
    'version.build.surefire' '3.0.0-M3'
    'sonar.jacoco.reportPaths' '${project.build.directory}/coverage-reports/jacoco-ut.exec'
    'sonar.links.homepage' 'https://github.com/yassine/ipdata-java-client'
    'sonar.links.issue' 'https://github.com/yassine/ipdata-java-client'
    'sonar.links.scm' 'https://github.com/yassine/ipdata-java-client'
    'sonar.projectKey' 'yassine_ipdata-java-client'
    'sonar.projectName' 'spring-boot-sample'
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
  build {
    plugins {
      plugin('org.apache.maven.plugins:maven-resources-plugin:2.6') {
        configuration {
          encoding '${project.build.sourceEncoding}'
        }
      }
      plugin {
        groupId 'org.jacoco'
        artifactId 'jacoco-maven-plugin'
        version '${version.build.jacoco}'
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
      plugin {
        groupId 'org.codehaus.mojo'
        artifactId 'sonar-maven-plugin'
        version '3.6.0.1398'
      }
    }
    resources {
      resource {
        directory 'src/main/resources'
        filtering true
        includes('VERSION')
      }
    }
  }
}

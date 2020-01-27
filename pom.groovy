project {
  modelVersion '4.0.0'
  groupId 'io.ipdata.client'
  artifactId 'ipdata-java-client'
  version '0.1.0-SNAPSHOT'
  properties {
    'project.build.sourceEncoding' 'UTF-8'
    'maven.compiler.source' '6'
    'maven.compiler.target' '6'
  }
  dependencies{
    dependency ('io.github.openfeign:feign-core:9.7.0')
    dependency ('io.github.openfeign:feign-jackson:9.7.0')
    dependency ('io.github.openfeign:feign-httpclient:9.7.0')
    dependency ('com.google.guava:guava:20.0')
    dependency ('org.slf4j:slf4j-api:1.7.30')
    dependency ('org.slf4j:slf4j-log4j12:1.7.30')
    dependency ('org.projectlombok:lombok:1.18.10')
    /* testing */
    dependency('org.hamcrest:hamcrest:2.2:test')
    dependency('junit:junit:4.13')
  }
  build{
    plugins {
      plugin('org.apache.maven.plugins:maven-resources-plugin:2.6'){
        configuration{
          encoding '${project.build.sourceEncoding}'
        }
      }
    }
    resources{
      resource{
        directory 'src/main/resources'
        filtering true
        includes{
          'VERSION'
        }
      }
    }
  }
}

MAVEN_OPTS=-Xmx1024m
export MAVEN_OPTS

mvn -Dmaven.test.skip=true clean package install assembly:assembly -U
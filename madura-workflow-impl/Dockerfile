## Build this with the following command:
## docker build -t workflow-impl .
##
## run it with:
## docker run -v YOUR_BUNDLES_DIR:/bundles --env-file ./env.list -it --rm -p 8080:8080 workflow-impl:latest
##
## browse to http://localhost:8080/madura-workflow/
##

# Pull base image
From tomcat:8-jre8

# Maintainer
MAINTAINER "Roger Parkinson <roger.parkinson35@gmail.com">

# Copy to images tomcat path removing version because it messes up the url
ADD target/madura-workflow-impl*.war /usr/local/tomcat/webapps/madura-workflow.war
ADD tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml


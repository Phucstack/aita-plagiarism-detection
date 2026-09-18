FROM tomcat:10.1-jdk17
LABEL maintainer="SE20C Group 7 - AITA CodeDefend PRJ301"
ENV CATALINA_BASE=/usr/local/tomcat
RUN rm -rf /usr/local/tomcat/webapps/ROOT /usr/local/tomcat/webapps/docs /usr/local/tomcat/webapps/examples
COPY target/aita-plagiarism-detection-1.0.0-SNAPSHOT.war /usr/local/tomcat/webapps/plagiarism.war
EXPOSE 8080
CMD ["catalina.sh", "run"]

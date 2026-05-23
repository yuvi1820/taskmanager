# Use Tomcat as base image
FROM tomcat:9.0-jdk17

# Set working directory
WORKDIR /usr/local/tomcat

# Copy WAR file to Tomcat webapps
COPY target/taskmanager.war /usr/local/tomcat/webapps/

# Expose port 8080
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]

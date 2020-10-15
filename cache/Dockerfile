FROM harisekhon/ubuntu-java
RUN apt-get update -y && apt-get upgrade -y && \
apt-get update -y && \
	apt-get install -y mongodb
RUN mkdir /opt/app
COPY . /opt/app
CMD ["java", "-jar", "/opt/app/lab1-1.0-SNAPSHOT.jar"]
FROM openjdk:8-jre
ENV APP_HOME=/apps
WORKDIR $APP_HOME
COPY ./cloud-seckill-eureka-server-1.0-SNAPSHOT.jar ./eureka-server.jar
EXPOSE 8091
ENTRYPOINT ["java","-jar","eureka-server.jar","--spring.profiles.active=test"]
#CMD ["eureka-server.jar"]

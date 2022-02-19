FROM openjdk:8-jre
ENV APP_HOME=/apps
WORKDIR $APP_HOME
COPY ./cloud-seckill-auth-0.0.1-SNAPSHOT.jar ./auth.jar
EXPOSE 8093
ENTRYPOINT ["java","-jar","auth.jar","--spring.profiles.active=test"]
#CMD ["auth.jar"]

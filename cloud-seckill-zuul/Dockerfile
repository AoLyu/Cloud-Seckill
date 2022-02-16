FROM openjdk:8-jre
ENV APP_HOME=/apps
WORKDIR $APP_HOMEdocker
COPY ./cloud-seckill-zuul-0.0.1-SNAPSHOT.jar ./zuul.jar
EXPOSE 8092
ENTRYPOINT ["java","-jar","zuul.jar","--spring.profiles.active=test"]
#CMD ["zuul.jar"]

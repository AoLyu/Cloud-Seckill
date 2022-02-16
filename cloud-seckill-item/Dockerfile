FROM openjdk:8-jre
ENV APP_HOME=/apps
WORKDIR $APP_HOME
COPY ./cloud-seckill-item-0.0.1-SNAPSHOT-exec.jar ./item.jar
EXPOSE 8094
ENTRYPOINT ["java","-jar","item.jar","--spring.profiles.active=test"]
#CMD ["item.jar"]

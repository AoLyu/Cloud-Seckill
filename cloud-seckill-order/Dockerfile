FROM openjdk:8-jre
ENV APP_HOME=/apps
WORKDIR $APP_HOME
COPY ./cloud-seckill-order-0.0.1-SNAPSHOT.jar ./order.jar
EXPOSE 8095
ENTRYPOINT ["java","-jar","order.jar","--spring.profiles.active=test"]
#CMD ["order.jar"]

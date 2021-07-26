FROM adoptopenjdk/openjdk11:alpine
USER root

ENV JAVA_HEAP_OPTS="-Xms1g -Xmx2g"
ENV JAVA_OPTS=""

RUN echo -e "https://mirrors.aliyun.com/alpine/latest-stable/main\nhttps://mirrors.aliyun.com/alpine/latest-stable/community" >/etc/apk/repositories \
  && apk update -f && apk --no-cache add -f \
  curl tzdata tini\
  && rm -rf /var/cache/apk/* \
  && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo "Asia/Shanghai" > /etc/timezone && apk del tzdata \
  && rm -rf /var/cache/apk/*

COPY --from=hengyunabc/arthas:3.4.5-no-jdk /opt/arthas /opt/arthas

ENTRYPOINT ["/sbin/tini", "-s", "--"]

COPY ./target/*.jar /opt/target/app.jar

WORKDIR /opt/target

CMD java $JAVA_HEAP_OPTS $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar
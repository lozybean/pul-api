FROM flyway/flyway:7-alpine

ENV FLYWAY_USER='root'
ENV FLYWAY_URL='jdbc:mysql://mysql:3306?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC'
ENV FLYWAY_PASSWORD='L0ng1viE@CPC'
ENV FLYWAY_SCHEMAS='pul'
ENV FLYWAY_LOCATIONS='filesystem:/migrations'
ENV FLYWAY_VALIDATE_ON_MIGRATE='false'
ENV FLYWAY_CLEAN_DISABLED='true'

ADD ./sql /migrations

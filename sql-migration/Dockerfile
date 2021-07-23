FROM flyway/flyway:7-alpine

ENV FLYWAY_USER='pul'
ENV FLYWAY_URL='jdbc:postgresql://db:5432/pul'
ENV FLYWAY_PASSWORD='L0ng1viE@CPC'
ENV FLYWAY_SCHEMAS='pul'
ENV FLYWAY_LOCATIONS='filesystem:/migrations'
ENV FLYWAY_VALIDATE_ON_MIGRATE='false'
ENV FLYWAY_CLEAN_DISABLED='true'

ADD ./sql /migrations

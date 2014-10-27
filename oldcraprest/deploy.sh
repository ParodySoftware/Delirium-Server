#!/bin/bash

TOMCAT=~/apache-tomcat-8.0.14
APP=oldcrap-rest
mvn clean compile | grep ERROR
mvn install | grep -v DEBUG

if [ $? -eq 0 ]; then
    ${TOMCAT}/bin/shutdown.sh
    ${TOMCAT}/bin/shutdown.sh

    rm ${TOMCAT}/webapps/${APP}.war
    rm -rf $TOMCAT/webapps/${APP}/

    cp target/${APP}.war ${TOMCAT}/webapps/

    ${TOMCAT}/bin/shutdown.sh
    ${TOMCAT}/bin/shutdown.sh
    ${TOMCAT}/bin/startup.sh

    tail -f ${TOMCAT}/logs/catalina.out | grep -v DEBUG
fi

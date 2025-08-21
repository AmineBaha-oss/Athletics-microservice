#!/usr/bin/env bash


spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=team-service \
  --package-name=com.athletics.team \
  --groupId=com.athletics.team \
  --dependencies=web,webflux,validation \
  --version=1.0.0-SNAPSHOT \
  team-service


spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=sponsor-service \
  --package-name=com.athletics.sponsor \
  --groupId=com.athletics.sponsor \
  --dependencies=web,webflux,validation \
  --version=1.0.0-SNAPSHOT \
  sponsor-service


spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=facility-service \
  --package-name=com.athletics.facility \
  --groupId=com.athletics.facility \
  --dependencies=web,webflux,validation \
  --version=1.0.0-SNAPSHOT \
  facility-service


spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=competition-service \
  --package-name=com.athletics.competition \
  --groupId=com.athletics.competition \
  --dependencies=web,webflux,validation \
  --version=1.0.0-SNAPSHOT \
  competition-service


spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=api-gateway \
  --package-name=com.athletics.apigateway \
  --groupId=com.athletics.apigateway \
  --dependencies=web,webflux,validation,hateoas \
  --version=1.0.0-SNAPSHOT \
  api-gateway


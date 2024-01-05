# User Posts Lambda

## About this repo

A simple [Spring Cloud Function](https://spring.io/projects/spring-cloud-function/)-based project to allow users to 
register and manage posts.

Cognito is used for user management, and posts are stored in DynamoDB.

The routing expression used by Spring to route requests to functions is based on the headers, which are managed by 
the API Gateway service from AWS, in order to avoid clients having to send them.

## Building

The fat jar can be built using:

```commandline
./gradlew clean shadowJar
```

## Infrastructure

The AWS infrastructure repo can be found here: [user-posts-lambda-cdk](https://github.com/ciprian88m/user-posts-lambda-cdk)

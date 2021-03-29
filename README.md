# phone-service API

Phones API service

## Server
Regular spring boot app. Running the application -

    mvn spring-boot:run

Access the application using the root context - http://localhost:8780/test/api/v1/

## API details

Regular Swagger ui api details  - http://localhost:8780/test/swagger-ui.html

![SwaggerUI](https://github.com/dsrini-open/phone_service/blob/main/swagger.png)

## Assumptions and Explanation
* 1.1 Only phone numbers generated in the GET requests output.
    1. Could do phone model attributes as well.
    2. Provided some basic sorting capability on some phone attributes - region, number etc.
          1. start -> start record index
          2. limit -> Max records in the output.
              1. Max limit(upper limit) is set in application.properties.
          3. sort  -> Sort field
          4. dir   -> ASC / DESC
    3. To Get by customerId, use the query parameter customerId in /phones.
* 1.2 Get phones by customer id
    1. Generally, to be more expressive and restful, this data set is retrieved from customers service via the rest path /customers/{customerId}/phones.
* 1.3 Activate phone scenario -
    1. Will update the activate flag capturing the date of activation.
        1. Will capture the IMEI as well during the process
    2. Will not update once a number is already activated.
* 1.4 DB and static data structure
    1. Assumed some index capabilities (available in relational/no-sql DBs) for searching, aggregation purposes.For instance, in case of dynamo using a GSI.
* 1.5 Used Java 8 - which is still the most preferred by Enterprise. Could have used 15 as well with some updates to type inference - var, switch, optional etc.

## Coverage

Jacoco coverage attached below - could run using

    mvn test jacoco:report

![coverage](https://github.com/dsrini-open/phone_service/blob/main/coverage.png)

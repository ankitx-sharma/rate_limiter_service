# Rate Limiter Service
A lightweight, extensible, and production-ready rate limiting system built with Spring Boot.
This project implements three classic rate-limiting algorithms:
- Fixed Window
- Sliding Window
- Token Bucket
  
You can switch between algorithms easily by enabling the corresponding service in the filter.

### Features
- Three fully implemented rate limiter strategies
- Configurable request limits via application.properties
- IP-based rate limiting (request.getRemoteAddr())
- Global filter using OncePerRequestFilter
- Plug-and-play architecture (swap algorithms instantly)
- Example endpoint /limiter/api/check

### Implemented Algorithms
1. Fixed Window Rate Limiting
- Divides time into fixed windows (e.g., 1 minute)
- Tracks how many requests occur in each window
- Simple but can cause burst issues at window boundaries

<ins>Best for:</ins>
_Basic API throttling, simple enforcement rules._

2. Sliding Window Rate Limiting
- Stores exact timestamps of requests inside a queue
- Removes timestamps older than the window
- More accurate than Fixed Window

<ins>Best for:</ins>
_Fairer request distribution without boundary spikes._

3. Token Bucket Rate Limiting
- Bucket holds tokens up to a maximum capacity
- Tokens refill every second
- Each request consumes one token
- Allows configurable bursts

<ins>Best for:</ins>
_APIs that need flexibility + burst handling._

### Running the Project
**Requirements**
- Java 17+
- Maven 3+
- Spring Boot 3.x
**Command**
```
mvn spring-boot:run
```


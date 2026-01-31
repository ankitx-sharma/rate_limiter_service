# Rate Limiter Playground (Learning Project)

<img src="assets/ui_screen.png" alt="Project Title" width="1000" align="center">

A lightweight Rate Limiting microservice built with Java & Spring Boot.  
This project is a hands-on playground to learn and compare rate limiting algorithms by observing their real runtime behavior.  
It supports multiple classic rate-limiting algorithms and allows dynamic algorithm selection per request using HTTP headers.

---

 ## What This Project Is

- A **tutorial-style Spring Boot application**
- A **controlled experiment environment** for rate limiting
- A way to *see* algorithm behavior, not just read about it

You trigger requests → observe outcomes → understand why.

---

## Core Learning Goal

> “If I send requests in a certain pattern, how does each rate limiting algorithm respond — and why?”


### How Learning Happens Here

Each algorithm exposes a **dedicated trigger endpoint**.

When triggered, the system:
1. Executes a predefined request pattern
2. Records each request outcome
3. Returns a structured timeline

Example (Token Bucket):

```json
{
  "algorithm": "TOKEN_BUCKET",
  "summary": {
    "allowed": 10,
    "blocked": 3,
    "config": {
      "refillRatePerSec": 1,
      "capacity": 5
    }
  },
  "timeline": [
    { "status": 200, "remaining": 4 },
    { "status": 200, "remaining": 3 },
    { "status": 200, "remaining": 2 },
    { "status": 200, "remaining": 1 },
    { "status": 200, "remaining": 0 },
    { "status": 429, "retryAfterMs": 1000 },
    { "status": 429, "retryAfterMs": 1000 },
    { "status": 429, "retryAfterMs": 1000 },
    { "comment": "triggered calls at 1 sec interval", "event": "MARKER" },
    { "status": 200, "remaining": 0 }
  ]
}
```

## How to Use This Project

1. Open Swagger UI
2. Select an algorithm endpoint
3. Trigger the test scenario
4. Read the timeline top to bottom
5. Ask:
  - Why did requests fail here?
  - Why did they recover here?
  - What does this imply in real systems?

---

## Algorithms Covered

- Token Bucket
- Fixed Window
- Sliding Window

---

## How to Read the Timeline
```
status: 200 → request allowed
status: 429 → rate limited
remaining → remaining capacity (if applicable)
retryAfterMs → time until next possible success
MARKER → intentional pause or timing change
```

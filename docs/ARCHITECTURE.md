# High Level Architecture

```
Swagger UI
↓
Learning Trigger Controller
↓
Algorithm-Specific Executor
↓
Rate Limiting Algorithm
↓
Timeline Recorder
↓
Structured Response
```


---

## Separation of Concerns

### Controllers
- expose learning endpoints

### Executors
- simulate request patterns
- control timing and volume

### Algorithms
- implement rate limiting rules

### Timeline Recorder
- captures observable behavior
- translates decisions into output

---

## Architectural Constraints

- In-memory only
- Single-node only
- Deterministic execution

# Design — Rate Limiter Playground

This document explains how the project is **designed as a learning tool**, not a production service.

---

## Why Dedicated Triggers Per Algorithm

Each algorithm has a **specific trigger endpoint**.

Reasons:
- avoids cross-algorithm interference
- ensures repeatable experiments
- allows algorithm-specific timing patterns

---

## Controlled Request Execution

Each trigger:
- executes a known number of requests
- uses a known timing pattern
- records results sequentially

This ensures:
- predictable output
- meaningful comparison
- teachable behavior

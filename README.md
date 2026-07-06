# Ecommerce Platform — Spring Boot Modular Monolith

A production-quality ecommerce backend built with Java Spring Boot, structured as a modular monolith following Domain-Driven Design principles. Developed as a Master's thesis project across 7 progressive checkins.

---

## Architecture

```
src/main/java/com/example/ecomm/
├── EcommApplication.java
├── shared/                        ← cross-cutting concerns
│   ├── config/                    ← Security, JWT, Rate limiting, Request ID
│   ├── exception/                 ← GlobalExceptionHandler
│   ├── model/                     ← BaseModel (UUID id + JPA auditing)
│   └── util/                      ← InputSanitizer
├── user/                          ← C1 + C2
├── product/                       ← C3
├── cart/                          ← C4
├── order/                         ← C5
├── payment/                       ← C5
└── notification/                  ← C6
```

Each module is fully self-contained with its own `controller`, `service`, `repository`, `model`, `dto`, `exception`, and `event` packages — mirroring what would become separate microservices in a distributed architecture.

---

## Modules

| Module | Checkin | Key Responsibility |
|---|---|---|
| `user` | C1, C2 | Registration, login, profile, password reset, session management |
| `product` | C3 | Catalog browsing, Elasticsearch full-text search |
| `cart` | C4 | Cart CRUD (MongoDB), Redis caching, checkout event |
| `order` | C5 | Order creation and lifecycle management |
| `payment` | C5 | Payment processing via Strategy + Factory patterns |
| `notification` | C6 | Async email delivery via Spring ApplicationEvents |
| `shared` | C1, C7 | JWT auth, security config, rate limiting, sanitization |

---

## Design Patterns

| Pattern | Implementation | Location |
|---|---|---|
| Repository | `UserRepository extends JpaRepository` | All modules |
| Interface + Impl | `AuthService` / `AuthServiceImpl` | All service classes |
| DTO | Separate request/response objects per endpoint | All modules |
| Template Method | `BaseModel.prePersist()` with `@PrePersist` | `shared/model` |
| Observer / Event | `ApplicationEventPublisher` + `@EventListener` | user, cart, order, notification |
| Strategy | `PaymentStrategy` interface + 3 implementations | `payment/strategy` |
| Factory | `PaymentStrategyFactory` resolves strategy at runtime | `payment/strategy` |
| Filter Chain | `JwtAuthFilter`, `RateLimitFilter`, `RequestIdFilter` | `shared/config` |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2, Spring Security 6 |
| Primary DB | MySQL 8.0 (users, products, orders, payments) |
| Document DB | MongoDB 7.0 (cart) |
| Cache | Redis 7.2 (cart cache, 24h TTL) |
| Search | Elasticsearch 8.11 (product full-text search) |
| Auth | JWT (jjwt 0.11.5), BCrypt rounds=14 |
| Email | Spring Mail + AWS SES |
| Build | Maven 3.9, Java 17 |
| Testing | JUnit 5, Mockito, H2, Flapdoodle embedded Mongo |

---

## API Endpoints

### Auth — `/api/v1/auth`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/register` | No | Create account |
| POST | `/login` | No | Get access + refresh token |
| POST | `/refresh` | No | Rotate token pair |
| POST | `/logout` | Bearer | Invalidate session |
| POST | `/forgot-password` | No | Request reset link |
| POST | `/reset-password` | No | Confirm new password |

### Profile — `/api/v1/profile`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | Bearer | Get current user profile |
| PATCH | `/` | Bearer | Update name fields |

### Products — `/api/v1/products`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | No | List by category (`?categoryId=`) |
| GET | `/search` | No | Full-text search (`?q=&categoryId=`) |
| GET | `/{slug}` | No | Get product detail |

### Categories — `/api/v1/categories`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | No | List all categories |

### Cart — `/api/v1/cart`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | Bearer | Get cart with total |
| POST | `/items` | Bearer | Add item |
| PATCH | `/items/{productId}` | Bearer | Update quantity |
| DELETE | `/items/{productId}` | Bearer | Remove item |
| POST | `/checkout` | Bearer | Initiate checkout |

### Orders — `/api/v1/orders`
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/` | Bearer | Order history |
| GET | `/{orderId}` | Bearer | Order detail |

### Payments — `/api/v1/payments`
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/initiate` | Bearer | Create gateway order |
| POST | `/confirm` | No | Webhook — confirm payment |
| GET | `/{orderId}` | Bearer | Get payment status |

---

## Checkin History

| Checkin | Scope | Key Additions |
|---|---|---|
| C1 | Infra + register/login | Spring Boot setup, JWT, BCrypt, SecurityConfig, BaseModel |
| C2 | Full user module | Profile, password reset, refresh token, Spring Events, email |
| C3 | Product catalog | MySQL catalog, Elasticsearch full-text search |
| C4 | Cart | MongoDB document store, Redis cache, CheckoutInitiatedEvent |
| C5 | Orders + Payments | Strategy + Factory patterns, full purchase pipeline |
| C6 | Notifications | Observer pattern, async @EventListener, order confirmation email |
| C7 | Hardening | HSTS, CSP, rate limiting, input sanitization, integration tests |

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker + Docker Compose

### Start infrastructure

```bash
docker-compose up -d
```

This starts MySQL, MongoDB, Redis, and Elasticsearch.

### Configure environment

```bash
cp .env.example .env
# Edit .env with your DB credentials and JWT secrets
```

### Run the application

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080`.

### Run tests

```bash
mvn test
```

---

## Security

- Stateless JWT authentication — no server-side session
- BCrypt password hashing (rounds=12 dev, 14 prod)
- SHA-256 hashed password reset tokens — raw token never stored
- Refresh token rotation — old token invalidated on every refresh
- All sessions invalidated on password reset
- Rate limiting: 20 req/15min on auth, 5 req/hour on reset endpoints
- HTTP headers: HSTS, CSP, X-Frame-Options DENY, Referrer-Policy
- Input sanitization: HTML/script tag stripping on all request bodies
- Email enumeration prevention on forgot-password endpoint

---

## Migration Path to Microservices

This modular monolith is designed for progressive extraction using the **Strangler Fig pattern**:

```
Phase 1 (current): Single Spring Boot app, 6 bounded contexts
Phase 2:           Extract user-service (highest change frequency)
Phase 3:           Extract product-service (independent scaling)
Phase 4:           Extract cart-service (MongoDB already isolated)
Phase 5:           Extract order + payment services together
Phase 6:           Replace Spring ApplicationEvents with Kafka topics
```

Each module's internal dependencies are already minimal. The only shared dependency is `BaseModel` and `GlobalExceptionHandler` in the `shared` package.

---

## References

- Martin Fowler — [Monolith First](https://martinfowler.com/bliki/MonolithFirst.html)
- Martin Fowler — [Strangler Fig Application](https://martinfowler.com/bliki/StranglerFigApplication.html)
- Spring Boot 3.2 Reference Documentation
- OWASP Top Ten 2021

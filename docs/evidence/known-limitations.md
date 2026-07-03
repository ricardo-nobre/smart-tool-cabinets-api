# Known Limitations Evidence

This project is an academic MVP and not a production product.

Known limitations:

- authentication is simplified for the demonstrator;
- bearer tokens are accepted by prefix and are not persisted as real sessions;
- cabinet API keys and operator PINs use simple SHA-256 hashing for demo purposes;
- no real RFID hardware is integrated;
- the simulator sends HTTP requests that represent cabinet behavior;
- no dashboard, mobile app, Kubernetes, analytics or external integrations are included;
- audit logging is intentionally minimal;
- OpenAPI documents the demonstrable MVP endpoints, not a full commercial API surface;
- PostgreSQL is expected to run locally through Docker Compose for the demo.

Future work:

- integrate real RFID/cabinet hardware;
- replace demo tokens with a stronger authentication mechanism;
- improve role-based access control;
- add operational monitoring;
- package and deploy the backend through a production-ready container platform if required.

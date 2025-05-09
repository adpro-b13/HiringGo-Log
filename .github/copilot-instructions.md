## Coding Principles and Practices

### SOLID & Design Patterns

- Follow SOLID principles.
- use design patterns where applicable

### Clean, Secure, and Readable Code

- Use meaningful names, consistent formatting, and clear abstractions.
- Validate inputs, handle errors gracefully, and avoid vulnerabilities (e.g., injection, improper logging).

## Development Workflow

### Test-Driven Development (TDD)**

- Always write tests first (RED) then implement feature (GREEN), refactoring is optional.
- Always provide commit messages when changing something in codebase: `[RED]: <describe test>` / `[GREEN]: <implement feature>` / `[REFACTOR]: <describe cleanup>`.

### RESTful APIs**

- Design endpoints using proper HTTP verbs, status codes, and resource naming.
- Document contracts with OpenAPI in openapi.yaml in root directory.

## Quality and Maintenance

- Modularity & Reusability**: Decompose large modules; extract shared logic into libraries.
- Observability**: Ensure every process is fully logged for easier debugging, standard console logging is enough.
- Performance**: Profile critical paths; optimize algorithms and I/O.
- Security**: Conduct code reviews, static analysis, and dependency scans.

## CI/CD

- Always check whether the changes applied to the codebase will need adding or modifying the existing deployment configuration scripts or anything related to it, and if so, please do it.

## Key Guidelines
- Always ask for clarifications if the requirements are not clear.
- You can change the code structure if you think it will improve the code quality.

## Dependency with External Microservices
- to get a list of vacancies for courses that a student is accepted for, an external service will be called, but now use a dummy first
- to retrieve a list of courses opened by lecturers, an external service will be called, but now use a dummy first

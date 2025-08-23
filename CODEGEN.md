# jOOQ Code Generation Guide  

We generate jOOQ classes from the actual database schema by running Liquibase migrations first, then generating type-safe Java code from the resulting schema.

Prerequisites:  
* Java 21+  
* Maven 3.8+  
* PostgreSQL 17+ (local or Docker)

## Start Local PostgreSQL  
```text
# Start PostgreSQL with Docker
docker run --rm -d --name postgres-codegen \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=<<enter_pass_here>> \
  -e POSTGRES_USER=dbuser \
  -e POSTGRES_DB=stayforgedb \
  postgres:17

# Verify it's running
docker logs postgres-codegen
```

## Generate Code

```shell
# Option A: Use profile defaults (recommended)
mvn clean generate-sources -Pcodegen -Ddb.password="devpass"

# Option B: Override specific settings
mvn clean generate-sources -Pcodegen \
  -Ddb.url="jdbc:postgresql://localhost:5432/stayforgedb" \
  -Ddb.username="dbuser" \
  -Ddb.password="<<enter_pass_here>>"
```

## Verify Generation

Check that generated code appears in  

```text
target/generated-sources/jooq/io/github/bluething/stayforge/supplyapi/persistence/jooq/
├── Tables.java
├── tables/
│   ├── Area.java
│   ├── Hotel.java
│   └── ...
├── tables/records/
└── tables/pojos/
```

## What Happens During Generation

* Liquibase applies all changesets from src/main/resources/db/changelog/changelog-root.yaml  
* jOOQ introspects the resulting schema and generates type-safe Java classes  
* Build Helper adds generated sources to Maven's source path for IDE integration

## Environment Variables (Optional)  

Instead of -D parameters, you can set environment variables:  
```shell
export SUPPLY_DB_URL=jdbc:postgresql://localhost:5432/stayforgedb
export SUPPLY_DB_USER=dbuser  
export SUPPLY_DB_PASSWORD=<<enter_pass_here>>

# Then just run:
mvn clean generate-sources -Pcodegen
```

## IDE Integration

### IntelliJ IDEA

* Generated sources are automatically recognized
* If not, right-click target/generated-sources/jooq → "Mark Directory as" → "Generated Sources Root"

### VS Code

Add to .vscode/settings.json:  
```text
{
    "java.project.sourcePaths": [
        "src/main/java",
        "target/generated-sources/jooq"
    ]
}
```

## Generated Code Strategy

### 🎯 Recommended: Don't Commit Generated Code  

Pros:  
* Clean repository (only source code)  
* Always up-to-date with schema  
* No merge conflicts on generated files  
* Forces proper database setup

Cons:  
* Developers need local database  
* CI must run generation

### Alternative: Commit Generated Code  

Pros:  
* Developers don't need database for basic development  
* Faster CI builds

Cons:  
* Repository bloat  
* Risk of stale generated code  
* Merge conflicts

## Team Workflow

### Schema Changes  

* Update Liquibase changesets in `src/main/resources/db/changelog/`  
* Regenerate code: `mvn clean generate-sources -Pcodegen -Ddb.password="..."`  
* Update business logic to handle any breaking changes  
* Test thoroughly before committing

### New Team Member Setup

```text
# Clone repo
git clone <repo-url>
cd supply-api

# Start database
docker run --rm -d --name postgres-codegen \
  -p 5432:5432 -e POSTGRES_PASSWORD=<<enter_pass_here>> \
  -e POSTGRES_USER=dbuser -e POSTGRES_DB=stayforgedb \
  postgres:17

# Generate code
mvn clean generate-sources -Pcodegen -Ddb.password="<<enter_pass_here>>"

# Ready to develop!
mvn compile
```

## Troubleshooting

### Common Issues

#### "Connection could not be created"

* Verify PostgreSQL is running: `docker ps`
* Check connection details: URL, username, password
* Test connection: for example `psql -h localhost -U dbuser -d stayforgedb`

#### "liquibase.exception.DatabaseException"

* Check Liquibase changesets for syntax errors
* Avoid non-deterministic expressions in schema (e.g., CURRENT_DATE in indexes)
* Review changelog file path: `src/main/resources/db/changelog/changelog-root.yaml`

#### "No tables generated"

* Verify Liquibase ran successfully (check Maven output)
* Check jOOQ includes/excludes pattern
* Confirm schema name is correct (default: `public`)

#### IDE doesn't recognize generated code

* Run `mvn clean generate-sources -Pcodegen` again
* Refresh/reimport Maven project
* Manually mark target/generated-sources/jooq as source root

#### Debug Mode

For detailed output:  
```shell
mvn clean generate-sources -Pcodegen -Ddb.password="..." -X
```

#### Reset Everything

```text
# Stop and remove database
docker stop postgres-codegen 2>/dev/null || true

# Clean Maven
mvn clean

# Start fresh
docker run --rm -d --name postgres-codegen \
  -p 5432:5432 -e POSTGRES_PASSWORD=<<enter_pass_here>> \
  -e POSTGRES_USER=dbuser -e POSTGRES_DB=stayforgedb \
  postgres:16

# Regenerate
mvn clean generate-sources -Pcodegen -Ddb.password="<<enter_pass_here>>"
```

## Configuration Details

The codegen profile includes:
* Liquibase: Applies database migrations
* jOOQ: Generates type-safe database access code
* Build Helper: Integrates generated sources with Maven/IDE

Default configuration:
* Database: jdbc:postgresql://localhost:5432/stayforgedb
* Username: dbuser
* Package: io.github.bluething.stayforge.supplyapi.persistence.jooq
* Output: target/generated-sources/jooq/

Further Reading

* [jOOQ Manual](https://www.jooq.org/doc/latest/manual/)
* [Liquibase Documentation](https://docs.liquibase.com/)
* [Spring Boot + jOOQ Guide](https://www.sivalabs.in/spring-boot-jooq-tutorial-getting-started/)
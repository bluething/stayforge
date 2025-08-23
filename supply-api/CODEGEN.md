# jOOQ Code Generation with Testcontainers  

Generate jOOQ classes using the official [Testcontainers jOOQ plugin](https://github.com/testcontainers/testcontainers-jooq-codegen-maven-plugin/tree/main) - zero setup required!

Prerequisites:  
* Java 21+  
* Maven 3.8+  
* Docker (for Testcontainers)

## Quick Start  
```text
# Generate jOOQ code (starts container, applies migrations, generates code)
mvn clean generate-sources -Pcodegen

# Or with compilation
mvn clean compile -Pcodegen
```  
That's it! No manual container management needed.

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

## What Happens

* PostgreSQL container starts automatically (postgres:17)
* Liquibase migrations apply from src/main/resources/db/changelog/changelog-root.yaml
* jOOQ code generation creates type-safe classes from schema
* Container cleanup happens automatically
* Generated sources added to Maven build path

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
* Run `mvn clean generate-sources -Pcodegen`
* Generated code automatically reflects schema changes
* Commit changes (excluding generated code)

### New Team Member Setup

```text
# Clone repo
git clone <repo-url>
cd supply-api

# Generate code (Docker required)
mvn clean generate-sources -Pcodegen

# Ready to develop!
mvn compile
```

## Troubleshooting

### Common Issues

#### "Docker not running"

```text
# Ensure Docker is running
docker version

# Start Docker service (Linux)
sudo systemctl start docker
```

#### Plugin version not found

Make sure you're using version 0.0.4 of the plugin:
```text
<version>0.0.4</version>
```

#### "No tables generated"

* Verify Liquibase ran successfully (check Maven output)
* Check jOOQ includes/excludes pattern
* Confirm schema name is correct (default: `public`)

#### Schema not found

Verify your Liquibase changelog path  
```text
<changeLogPath>src/main/resources/db/changelog/changelog-root.yaml</changeLogPath>
```

#### IDE doesn't recognize generated code

* Run `mvn clean generate-sources -Pcodegen` again
* Refresh/reimport Maven project
* Manually mark target/generated-sources/jooq as source root

#### Debug Mode

For detailed output:  
```shell
mvn clean generate-sources -Pcodegen -Ddb.password="..." -X
```

## Migration from Manual Setup

If you're currently using manual Docker + Maven commands:
* Replace your existing codegen profile with the plugin configuration
* Remove manual container scripts
* Run `mvn clean generate-sources -Pcodegen`

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

The codegen profile uses:
* Plugin Version: 0.0.4
* Testcontainers: 1.19.1
* PostgreSQL: 42.6.0
* Container Image: postgres:17

Further Reading

* [jOOQ Manual](https://www.jooq.org/doc/latest/manual/)
* [Liquibase Documentation](https://docs.liquibase.com/)
* [Spring Boot + jOOQ Guide](https://www.sivalabs.in/spring-boot-jooq-tutorial-getting-started/)
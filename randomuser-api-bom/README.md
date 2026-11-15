# randomuser-api-bom

A Maven Bill of Materials (BOM) module for the randomuser-api project. It centralizes the versions
of the project's own modules — `randomuser-api-core` and `randomuser-api-openfeign` — so a consumer
can import it once and depend on any module without declaring a version.

It manages only this project's own modules. It does not manage any third-party dependency; third-party
version conflicts remain the responsibility of the developer using the library.

## Import the BOM

Add the BOM to `dependencyManagement` with `scope=import`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>xyz.opcal.tools</groupId>
            <artifactId>randomuser-api-bom</artifactId>
            <version>2.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Depend on a module without a version

After importing the BOM, depend on any project module by `groupId` + `artifactId` only — the version
is resolved from the BOM:

```xml
<dependencies>
    <dependency>
        <groupId>xyz.opcal.tools</groupId>
        <artifactId>randomuser-api-core</artifactId>
    </dependency>
</dependencies>
```

The same applies to `randomuser-api-openfeign`.

## Version bump

To change a module's version, edit only the BOM. No consuming POM needs to change — the consumer
picks up the new version from the imported BOM.

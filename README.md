# BattlegroundCombat

## Build

This project targets Java 17 and uses a single-module Maven build.

```bash
mvn -U clean package
```

### Local setup for CommandAPI (offline)

If you cannot reach the CodeMC Maven repository, install the CommandAPI jar into
your local Maven cache first. The `-Dfile` path must point to the CommandAPI jar
on your machine (for example, wherever your server plugins are stored).

**Windows (Command Prompt)**

```bat
mvn install:install-file ^
  -Dfile="C:\server\plugins\CommandAPI-10.1.2-Mojang-Mapped.jar" ^
  -DgroupId=dev.jorel ^
  -DartifactId=commandapi-bukkit-mojang-mapped ^
  -Dversion=10.1.2 ^
  -Dpackaging=jar
```

After installing, run the build:

```bash
mvn -U clean package
```

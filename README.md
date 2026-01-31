# BattlegroundCombat

## Build

This project targets Java 17 and uses a single-module Maven build.

```bash
mvn -U clean package
```

### CommandAPI dependency fallback (offline/local)

The CommandAPI Mojang-mapped artifact is normally resolved from the CodeMC Maven
repository (see `pom.xml`). If your environment cannot reach that repository,
install the CommandAPI jar into your local Maven repo and retry the build.

**macOS/Linux**

```bash
mvn install:install-file \
  -Dfile=plugins/CommandAPI-10.1.2-Mojang-Mapped.jar \
  -DgroupId=dev.jorel \
  -DartifactId=commandapi-bukkit-mojang-mapped \
  -Dversion=10.1.2 \
  -Dpackaging=jar
```

**Windows (PowerShell)**

```powershell
mvn install:install-file `
  -Dfile=plugins/CommandAPI-10.1.2-Mojang-Mapped.jar `
  -DgroupId=dev.jorel `
  -DartifactId=commandapi-bukkit-mojang-mapped `
  -Dversion=10.1.2 `
  -Dpackaging=jar
```

After installing, run:

```bash
mvn -U clean package
```

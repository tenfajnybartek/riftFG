# NMS adapters

riftFG requires **Paper 26.3 and Java 25**. This directory contains only `api`
and the standalone `v26_3` implementation. Both use the pinned Paper development
bundle `26.3.build.249-stable`; there are no dependencies on older adapters.

`NmsAccessorHolder` rejects server data versions below **5023** (Minecraft 26.3).
`plugin.yml` also declares `api-version: '26.3'`. A newer server data version
tries `v26_3` with an explicit unverified-compatibility warning; this is not a
guarantee that future server internals remain compatible.

All Java/Kotlin bytecode, tests and CI target **Java 25**. Gradle must run on JDK 25
or newer and needs the JDK 25 toolchain. Older JDKs are not required.
Pin Paper development bundles and the plugin's Paper API together when updating.
Verify compilation and the packaged JAR, then run server startup, join/quit, heart,
tablist and shutdown smoke tests on the affected server build.

Local verification: `./gradlew clean build`.
Database integration tests: `./gradlew :plugin:databaseTest` (requires Docker; also run in CI).
The three `:nms:api:test` cases check the supported-version boundary without a running server.
The distributable JAR is `plugin/build/libs/riftFG … (MC 26.3+, Java 25).jar`.

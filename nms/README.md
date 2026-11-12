# NMS adapters

`NmsAccessorHolder` selects an adapter using the server data version and `floorEntry`.
The baselines are `v1_21`, `v1_21_2`, `v1_21_4`, `v1_21_9`, `v26_1_1` and `v26_3`.
Adapters inherit unchanged implementations and override only changed server internals.
Servers newer than the latest baseline get an explicit unverified-compatibility warning.

The plugin and older adapters target Java 21; the 26.x adapters target Java 25.
CI installs both toolchains. Pin Paper development bundles when updating an adapter;
verify compilation and the packaged JAR, then run server startup, join/quit, heart,
tablist and shutdown smoke tests on the affected server build.

Local verification: `./gradlew :plugin:test :plugin:shadowJar`.
Database integration tests: `./gradlew :plugin:databaseTest` (requires Docker; also run in CI).
A successful compilation is not a claim that every intervening server version was tested.

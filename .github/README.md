# riftFG

**Gildie, tereny i rywalizacja dla serwerów Paper.**

[![Build](https://github.com/tenfajnybartek/riftFG/actions/workflows/gradle.yml/badge.svg?branch=5.x)](https://github.com/tenfajnybartek/riftFG/actions/workflows/gradle.yml)
[![License](https://img.shields.io/badge/licencja-Apache%202.0-blue)](../LICENSE)
[![Java](https://img.shields.io/badge/Java-21%20%2F%2025-orange)](#wymagania-i-wsparcie-wersji)

[Pobierz wydanie](https://github.com/tenfajnybartek/riftFG/releases) · [Changelog](../CHANGELOG.md) · [Dokumentacja PL](../docs/src/content/docs/introduction.mdx) · [English docs](../docs/src/content/docs/en/introduction.mdx)

riftFG to niezależnie rozwijany **fork [FunnyGuilds 5.x](https://github.com/FunnyGuilds/FunnyGuilds/tree/5.x)**. Łączy bazę oryginalnego projektu z własnymi poprawkami stabilności, obsługi konfiguracji i mechanik gildyjnych. Kod bazowy powstał dzięki FunnyGuilds Team i [współtwórcom FunnyGuilds](https://github.com/FunnyGuilds/FunnyGuilds/graphs/contributors); rozwój tego forka odbywa się w repozytorium [tenfajnybartek/riftFG](https://github.com/tenfajnybartek/riftFG).

> **Ostatnie wydanie dla Minecraft 1.21.x**
>
> Bieżące wydanie zamyka wsparcie dla serii **1.21.x**. Kolejne wydania riftFG będą przeznaczone wyłącznie dla **Minecraft 26.3 i nowszych** i przejdą w całości na **Javę 25**. Zostaną usunięte adaptery i obsługa wszystkich wersji poniżej 26.3, także wcześniejszych wydań 26.x.

## Co oferuje riftFG

- **Gildie i członkowie** — zakładanie gildii, zaproszenia, lider, zastępcy, sojusze i ważność gildii.
- **Tereny i serca** — ochrona regionów, powiększanie terenu, baza i konfiguracja eksplozji.
- **Wojny** — tryby LEGACY i MODERN, życia i HP serca, wykup żyć, statystyki oraz serie zwycięstw.
- **Ranking PvP** — punkty, zabójstwa, asysty, topki przez placeholdery i ochrona przed farmieniem.
- **Konfiguracja przedmiotów** — `items.yml`, zestawy zależne od uprawnień, koszty założenia gildii i GUI wymaganych przedmiotów.
- **Prezentacja** — czat gildii i sojuszników, tablista, nametagi, hologramy i MiniMessage.
- **Dane i integracje** — zapis FLAT lub SQL oraz opcjonalna współpraca z innymi pluginami.

## Co zmienia fork

Względem bazowego FunnyGuilds 5.x riftFG poprawia działanie istniejących opcji i zabezpiecza operacje wykonywane przez graczy oraz administratorów:

| Obszar | Zmiany w riftFG |
|---|---|
| Zapis danych | Ponawianie nieudanych zapisów, zachowanie nowszych zmian, transakcje SQL i bezpieczniejsza podmiana plików FLAT. |
| Wojny i antyfarming | Spójne rozliczanie ataków, brak nagrody po anulowanym ataku, wspólne reguły nagród w MODERN i cooldowny zachowywane po restarcie. |
| Wykup życia | Kontrola dostawcy ekonomii Vault i wyniku wypłaty; nieudana płatność nie przyznaje życia ani nie zabiera przedmiotów. |
| Konfiguracja | Reload obejmuje `items.yml`, waliduje ustawienia i wskazuje sekcje wymagające restartu. |
| Administracja | Poprawione odbanowanie gildii i graczy; potwierdzenie usunięcia gildii jest przypisane do niej i ważne przez 60 sekund. |
| Rozgrywka i integracje | Poprawione rozliczanie teleportu do bazy, wybór asyst, moderacja czatu i sprzątanie tablisty oraz scoreboardu. |

Pełny opis zmian dla bieżącego wydania znajduje się w [changelogu](../CHANGELOG.md). Funkcje takie jak GUI przedmiotów, wykup żyć czy statystyki wojen są częścią odziedziczonej bazy FunnyGuilds 5.x; własne poprawki forka opisujemy osobno.

## Wymagania i wsparcie wersji

| Wydanie | Minecraft / Paper | Java |
|---|---|---|
| Bieżące — ostatnie z obsługą 1.21.x | Adaptery dla 1.21, 1.21.2/1.21.3, 1.21.4, 1.21.9, 26.1.1 i 26.3 | 21 dla 1.21.x; 25 dla 26.x |
| Kolejne wydania | **26.3+** | **25** |

Pomiędzy punktami bazowymi plugin dobiera wcześniejszy adapter NMS. Lista adapterów opisuje obecną bazę kodu; zgodność z nowszymi wersjami serwera wymaga osobnej weryfikacji. Wymagania archiwalnych wydań FunnyGuilds są niezależne od polityki wydań riftFG.

## Instalacja

1. Pobierz JAR z [Releases tego repozytorium](https://github.com/tenfajnybartek/riftFG/releases).
2. Zatrzymaj serwer i zachowaj kopię dotychczasowych danych oraz konfiguracji gildii.
3. Umieść JAR w `plugins/`, zastępując poprzednią wersję pluginu.
4. Uruchom serwer, skonfiguruj pliki w `plugins/FunnyGuilds/` i nadaj graczom odpowiednie uprawnienia.

W tym wydaniu zachowano techniczną nazwę pluginu `FunnyGuilds`, katalog danych, uprawnienia `funnyguilds.*` i istniejące komendy. Marka repozytorium i własnych wydań to **riftFG**.

## Integracje

| Plugin | Zastosowanie |
|---|---|
| Vault + dostawca ekonomii | Koszty pieniężne i uprawnienia graczy offline. |
| PlaceholderAPI | Placeholdery graczy, gildii i rankingów dla innych pluginów. |
| WorldGuard / WorldEdit | Flagi regionów i wklejanie schematów. |
| DecentHolograms | Hologramy nad sercami gildii. |
| dynmap | Znaczniki i granice terenów na mapie. |

## Dokumentacja i budowanie

Dokumentacja funkcji i konfiguracji jest dostępna w repozytorium po [polsku](../docs/src/content/docs/introduction.mdx) i [angielsku](../docs/src/content/docs/en/introduction.mdx). Najczęściej potrzebne strony: [komendy](../docs/src/content/docs/commands.mdx), [uprawnienia](../docs/src/content/docs/permissions.mdx) i [konfiguracja](../docs/src/content/docs/configuration.mdx).

Do zbudowania bieżącej wersji potrzebne są **JDK 21 i JDK 25**:

```sh
./gradlew :plugin:test :plugin:shadowJar
```

Na Windows użyj `gradlew.bat`. Gotowy JAR znajduje się w `plugin/build/libs/`. Testy integracyjne MySQL/MariaDB uruchamia `:plugin:databaseTest` i wymagają Dockera.

## Autorzy i licencja

riftFG bazuje na [FunnyGuilds](https://github.com/FunnyGuilds/FunnyGuilds), rozwijanym przez **FunnyGuilds Team i współtwórców oryginału**. Projekt oraz wprowadzone tu modyfikacje udostępniane są na licencji **[Apache License 2.0](../LICENSE)**. Zachowujemy oryginalną licencję, informacje o pochodzeniu kodu i historię autorów.

Wydania riftFG są przygotowywane niezależnie od oficjalnych wydań FunnyGuilds. Zmiany forka opisuje [CHANGELOG.md](../CHANGELOG.md).

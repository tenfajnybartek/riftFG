import { NAV_CONTENT, type NavContent } from './nav-content';

export interface DownloadContent extends NavContent {
  pageTitle: string;
  pageDescription: string;
  eyebrow: string;
  headline: string;
  lede: string;
  whatYouGet: string;
  snapshot: {
    badge: string;
    title: string;
    description: string;
    buttonLabel: string;
    ciLabel: string;
    note: string;
  };
  intermediate: {
    badge: string;
    mcRange: string;
    title: string;
    description: string;
    note: string;
  };
  stable: {
    title: string;
    description: string;
    buttonLabel: string;
    historyLabel: string;
  };
  legacy: {
    badge: string;
    mcRange: string;
    title: string;
    description: string;
    buttonLabel: string;
    sizeLabel: string;
    archiveLabel: string;
  };
  mcVersionTable: {
    title: string;
    intro: string;
    currentLabel: string;
    nightlyLabel: string;
    changelogLinkLabel: string;
    changelogEyebrow: string;
    releasedLabel: string;
  };
  community: {
    fallbackName: string;
    description: string;
    buttonLabel: string;
    onlineLabel: string;
    membersLabel: string;
  };
  nextSteps: {
    introductionHref: string;
    introductionLabel: string;
    faqHref: string;
    faqLabel: string;
  };
}

export const DOWNLOAD_CONTENT: Record<'pl' | 'en', DownloadContent> = {
  pl: {
    ...NAV_CONTENT.pl,
    plHref: '/download/',
    enHref: '/en/download/',
    pageTitle: 'Pobierz — riftFG',
    pageDescription: 'Pobierz riftFG dla Paper 26.3+ i Javy 25. Dostępne jest też archiwum oryginalnego FunnyGuilds.',
    eyebrow: 'Pobierz',
    headline: 'Pobierz riftFG',
    lede: 'Bieżąca linia wymaga Paper 26.3+ i Javy 25.',
    whatYouGet: 'riftFG to niezależny fork FunnyGuilds. Poniżej znajdziesz jego wydania oraz osobno historyczne wydania oryginalnego projektu.',
    snapshot: {
      badge: 'Zalecane',
      title: 'riftFG — Minecraft 26.3+',
      description: 'Wydania forka z poprawkami zapisu, wojen, ekonomii i konfiguracji. Gotowy JAR zawiera adapter 26.3 i wymagane biblioteki.',
      buttonLabel: 'Wydania riftFG ↗',
      ciLabel: 'Lista buildów na CI ↗',
      note: 'Java 25 · katalog danych plugins/FunnyGuilds/. Migracja bezpośrednio z FunnyGuilds 4.x nie jest obsługiwana.',
    },
    intermediate: {
      badge: 'Wymagania',
      mcRange: 'MC 26.3+',
      title: 'Java 25',
      description: 'Do uruchomienia potrzebujesz Paper 26.3 i Javy 25. Budowanie oraz testy korzystają wyłącznie z JDK 25. Nowsze wersje serwera wymagają sprawdzenia zgodności adaptera.',
      note: 'Starsze serwery nie są obsługiwane',
    },
    stable: {
      title: 'Archiwum FunnyGuilds 4.x',
      description: 'Ostatnie oficjalne wydanie oryginalnej serii 4.x, z osobnymi wymaganiami wersji serwera i Javy.',
      buttonLabel: 'Pobierz',
      historyLabel: 'Starsze wersje ↓',
    },
    legacy: {
      badge: 'Historyczna',
      mcRange: 'MC 1.7.x',
      title: 'FunnyGuilds 3.9.9.15',
      description: 'Ostatnie wydanie z obsługą Minecrafta 1.7. Nowsze wersje wymagają 1.8+.',
      buttonLabel: 'Pobierz',
      sizeLabel: '4,5 MB · 2017',
      archiveLabel: 'Pełne archiwum wersji 1.x–3.x ↗',
    },
    mcVersionTable: {
      title: 'riftFG i archiwum FunnyGuilds',
      intro: 'Bieżący riftFG obsługuje Minecraft 26.3+. Starsze wpisy są archiwalnymi wydaniami oryginalnego FunnyGuilds i nie określają zgodności tego forka.',
      currentLabel: 'aktualne',
      nightlyLabel: 'NIGHTLY',
      changelogLinkLabel: 'Zobacz na GitHubie ↗',
      changelogEyebrow: 'Changelog',
      releasedLabel: 'Wydano',
    },
    community: {
      fallbackName: 'Discord',
      description: 'Dołącz do społeczności FunnyGuilds — pytania, pomoc, dyskusje o rozwoju pluginu.',
      buttonLabel: 'Dołącz ↗',
      onlineLabel: 'online',
      membersLabel: 'członków',
    },
    nextSteps: {
      introductionHref: '/introduction/',
      introductionLabel: 'Wprowadzenie',
      faqHref: '/faq/',
      faqLabel: 'FAQ',
    },
  },
  en: {
    ...NAV_CONTENT.en,
    plHref: '/download/',
    enHref: '/en/download/',
    pageTitle: 'Download — riftFG',
    pageDescription: 'Download riftFG for Paper 26.3+ and Java 25. Original FunnyGuilds releases are also archived here.',
    eyebrow: 'Download',
    headline: 'Download riftFG',
    lede: 'The current line requires Paper 26.3+ and Java 25.',
    whatYouGet: 'riftFG is an independent FunnyGuilds fork. Its releases and the original project’s historical releases are listed separately below.',
    snapshot: {
      badge: 'Recommended',
      title: 'riftFG — Minecraft 26.3+',
      description: 'Fork releases with persistence, war, economy and configuration fixes. The distributable JAR includes the 26.3 adapter and required libraries.',
      buttonLabel: 'riftFG releases ↗',
      ciLabel: 'Build list on CI ↗',
      note: 'Java 25 · data directory plugins/FunnyGuilds/. Direct migration from FunnyGuilds 4.x is not supported.',
    },
    intermediate: {
      badge: 'Requirements',
      mcRange: 'MC 26.3+',
      title: 'Java 25',
      description: 'Running requires Paper 26.3 and Java 25. Builds and tests use only JDK 25. Newer server versions need adapter compatibility checks.',
      note: 'Older servers are not supported',
    },
    stable: {
      title: 'FunnyGuilds 4.x archive',
      description: 'The latest official release of the original 4.x series, with separate server and Java requirements.',
      buttonLabel: 'Download',
      historyLabel: 'Older versions ↓',
    },
    legacy: {
      badge: 'Legacy',
      mcRange: 'MC 1.7.x',
      title: 'FunnyGuilds 3.9.9.15',
      description: 'The last release supporting Minecraft 1.7. Newer versions require 1.8+.',
      buttonLabel: 'Download',
      sizeLabel: '4.5 MB · 2017',
      archiveLabel: 'Full 1.x–3.x archive ↗',
    },
    mcVersionTable: {
      title: 'riftFG and the FunnyGuilds archive',
      intro: 'Current riftFG supports Minecraft 26.3+. Older entries are archived releases of the original FunnyGuilds project and do not describe this fork’s compatibility.',
      currentLabel: 'current',
      nightlyLabel: 'NIGHTLY',
      changelogLinkLabel: 'View on GitHub ↗',
      changelogEyebrow: 'Changelog',
      releasedLabel: 'Released',
    },
    community: {
      fallbackName: 'Discord',
      description: 'Join the FunnyGuilds community — questions, help, and discussion about plugin development.',
      buttonLabel: 'Join ↗',
      onlineLabel: 'online',
      membersLabel: 'members',
    },
    nextSteps: {
      introductionHref: '/en/introduction/',
      introductionLabel: 'Introduction',
      faqHref: '/en/faq/',
      faqLabel: 'FAQ',
    },
  },
};

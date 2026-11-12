// Copy + HUD mockup strings for the landing page, one entry per locale. The isometric
// map itself (src/lib/isomap/) needs no localization — the only text it renders is the
// four territory names, which are proper nouns.
import { NAV_CONTENT, type NavContent } from './nav-content';

export interface LandingContent extends NavContent {
  pageTitle: string;
  pageDescription: string;
  eyebrow: string;
  headline: string;
  sub: string;
  tags: [string, string, string, string];
  ctaPrimary: string;
  ctaSecondary: string;
  metaParts: [string, string, string];
  hud: {
    topPoints: string;
    topKdr: string;
    topKills: string;
    scoreRank: string;
    scorePoints: string;
    scoreKdr: string;
    scoreLives: string;
  };
}

export const LANDING_CONTENT: Record<'pl' | 'en', LandingContent> = {
  pl: {
    ...NAV_CONTENT.pl,
    plHref: '/',
    enHref: '/en/',
    pageTitle: 'riftFG — tereny, wojny i rankingi gildii dla Paper 26.3+',
    pageDescription:
      'riftFG to fork FunnyGuilds dla Paper 26.3+ i Javy 25. Tereny, wojny, rankingi i społeczność w jednym pluginie.',
    eyebrow: 'Open-source · Fork FunnyGuilds · Paper 26.3+',
    headline: 'Wszystko, czego potrzebuje gildia.',
    sub: 'Tereny, wojny, rankingi i społeczność — w jednym pluginie, w pełni pod kontrolą administratora.',
    tags: ['Tereny', 'Wojny', 'Rankingi', 'Społeczność'],
    ctaPrimary: 'Pobierz najnowszą wersję',
    ctaSecondary: 'Zobacz kod źródłowy ↗',
    metaParts: ['Apache-2.0', 'Minecraft 26.3+', 'Java 25'],
    hud: {
      topPoints: 'TOP: Punkty',
      topKdr: 'TOP: KDR',
      topKills: 'TOP: Zabójstwa',
      scoreRank: 'Ranga',
      scorePoints: 'Punkty',
      scoreKdr: 'KDR',
      scoreLives: 'Życia',
    },
  },
  en: {
    ...NAV_CONTENT.en,
    plHref: '/',
    enHref: '/en/',
    pageTitle: 'riftFG — guild territory, war, and rank for Paper 26.3+',
    pageDescription:
      'riftFG is a FunnyGuilds fork for Paper 26.3+ and Java 25. Territory, wars, ranks and community in one plugin.',
    eyebrow: 'Open-source · FunnyGuilds fork · Paper 26.3+',
    headline: 'Everything a guild needs.',
    sub: 'Territory, wars, ranks and community — in one plugin, fully under the admin’s control.',
    tags: ['Territory', 'Wars', 'Ranks', 'Community'],
    ctaPrimary: 'Download latest version',
    ctaSecondary: 'View source code ↗',
    metaParts: ['Apache-2.0', 'Minecraft 26.3+', 'Java 25'],
    hud: {
      topPoints: 'TOP: Points',
      topKdr: 'TOP: KDR',
      topKills: 'TOP: Kills',
      scoreRank: 'Rank',
      scorePoints: 'Points',
      scoreKdr: 'KDR',
      scoreLives: 'Lives',
    },
  },
};

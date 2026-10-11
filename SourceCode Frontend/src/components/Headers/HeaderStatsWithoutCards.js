/**
 * Legacy spacer retained for the layouts that still mount it.
 *
 * It previously rendered an empty navy band that views pulled themselves over
 * with a negative margin. The design system now handles page rhythm through
 * `.ds-content` / `.ds-stack`, so this renders nothing. The component is kept
 * (not removed) so existing layout imports keep working.
 */
export default function HeaderStatsWithoutCards() {
  return null;
}

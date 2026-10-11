const plugin = require("tailwindcss/plugin");
const colors = require("tailwindcss/colors");
const withMT = require("@material-tailwind/react/utils/withMT");

/* ==========================================================================
 * NCRE DESIGN TOKENS
 * --------------------------------------------------------------------------
 * Palette values live as CSS custom properties in
 *   src/assets/styles/design-system.css  (section "1. DESIGN TOKENS")
 * and are consumed here through `<alpha-value>` so a single change updates
 * both the generated utilities and the `ds-*` component primitives.
 *
 * Scales:   navy (brand) · ink (blue-grey neutrals) ·
 *           success · warning · critical
 * ========================================================================== */
const tokenColor = (name) => `rgb(var(--ncre-${name}) / <alpha-value>)`;

const brandScale = {
  50: tokenColor("navy-50"),
  100: tokenColor("navy-100"),
  200: tokenColor("navy-200"),
  300: tokenColor("navy-300"),
  400: tokenColor("navy-400"),
  500: tokenColor("navy-500"),
  600: tokenColor("navy-600"),
  700: tokenColor("navy-700"),
  800: tokenColor("navy-800"), // primary
  900: tokenColor("navy-900"),
  950: tokenColor("navy-950"),
};

const inkScale = {
  50: tokenColor("ink-50"),
  100: tokenColor("ink-100"), // page background
  200: tokenColor("ink-200"), // hairline border
  300: tokenColor("ink-300"),
  400: tokenColor("ink-400"),
  500: tokenColor("ink-500"), // muted text
  600: tokenColor("ink-600"), // secondary text
  700: tokenColor("ink-700"), // body text
  800: tokenColor("ink-800"), // headings
  900: tokenColor("ink-900"), // page titles
};

const semanticScale = (name) => ({
  50: tokenColor(`${name}-50`),
  100: tokenColor(`${name}-100`),
  200: tokenColor(`${name}-100`),
  500: tokenColor(`${name}-500`),
  600: tokenColor(`${name}-600`),
  700: tokenColor(`${name}-700`),
  800: tokenColor(`${name}-800`),
});

/* NSO brand red (formerly `red4` / #7c0000 in the legacy config). Kept as a
   named token rather than a magic hex, with the two near-identical dark shades
   that had accumulated collapsed onto a single scale. */
const brandRed = {
  DEFAULT: tokenColor("brandred"),
  dark: tokenColor("brandred-dark"),
  deep: tokenColor("brandred-deep"),
};

module.exports = withMT({
  content: [
    "./public/**/*.html",
    "./public/*.html",
    "./src/**/*.{js,jsx,ts,tsx}",
    "./src/*.{js,jsx,ts,tsx}",
    "./src/**/*.html",
    "./src/*.html",
    "./src/**/*.css",
    "./public/**/*.js",
    "./public/*.js",
    "./node_modules/flowbite-react/**/*.js"
  ],

  theme: {
    extend: {
      colors: {
        ...colors,
        'red4': '#7c0000',

        /* Brand + neutrals + semantics. Use these instead of raw hex. */
        navy: brandScale,
        ink: inkScale,
        success: semanticScale("success"),
        warning: semanticScale("warning"),
        critical: semanticScale("critical"),
        brandred: brandRed,
      },

      /* ── Typography ───────────────────────────────────────────────────
       * Clear hierarchy: page > section > body > caption > label.
       * `kpi` is large but restrained (30px) and rendered tabular.        */
      fontFamily: {
        sans: [
          "Inter",
          "ui-sans-serif",
          "system-ui",
          "-apple-system",
          "Segoe UI",
          "Roboto",
          "Helvetica Neue",
          "Arial",
          "sans-serif",
        ],
        mono: [
          "ui-monospace",
          "SFMono-Regular",
          "Menlo",
          "Consolas",
          "Liberation Mono",
          "monospace",
        ],
      },
      fontSize: {
        label: ["11px", { lineHeight: "14px", letterSpacing: "0.08em" }],
        caption: ["12px", { lineHeight: "16px" }],
        "body-sm": ["13px", { lineHeight: "20px" }],
        body: ["14px", { lineHeight: "22px" }],
        "body-lg": ["16px", { lineHeight: "24px" }],
        section: ["18px", { lineHeight: "26px", fontWeight: "600" }],
        title: ["20px", { lineHeight: "28px", fontWeight: "600" }],
        page: ["24px", { lineHeight: "32px", fontWeight: "700", letterSpacing: "-0.01em" }],
        kpi: ["30px", { lineHeight: "36px", fontWeight: "700", letterSpacing: "-0.02em" }],
        55: "55rem",
      },

      /* ── Radii: moderate, matching the login page controls (10px) ─────── */
      borderRadius: {
        sm: "3px",
        DEFAULT: "5px",
        md: "6px",
        lg: "8px",
        xl: "10px",
        "2xl": "12px",
        "3xl": "16px",
      },

      /* ── Shadows: very light. Borders do the structural work. ─────────── */
      boxShadow: {
        xs: "0 1px 2px 0 rgb(16 24 40 / 0.04)",
        sm: "0 1px 2px 0 rgb(16 24 40 / 0.05)",
        card: "0 1px 3px 0 rgb(16 24 40 / 0.05), 0 1px 2px -1px rgb(16 24 40 / 0.03)",
        md: "0 2px 4px -1px rgb(16 24 40 / 0.06), 0 2px 6px -2px rgb(16 24 40 / 0.04)",
        lg: "0 4px 8px -2px rgb(16 24 40 / 0.08), 0 2px 4px -2px rgb(16 24 40 / 0.04)",
        xl: "0 8px 16px -4px rgb(16 24 40 / 0.10), 0 4px 8px -4px rgb(16 24 40 / 0.04)",
        overlay: "0 12px 32px -8px rgb(16 24 40 / 0.16)",
      },

      /* ── Focus ring → brand navy ──────────────────────────────────────── */
      ringColor: {
        DEFAULT: tokenColor("navy-800"),
      },
      ringOffsetColor: {
        DEFAULT: "#ffffff",
      },

      /* ── Motion ─────────────────────────────────────────────────────────
       * Two short, subtle fades only. The legacy stylesheet defined these
       * names unlayered, so they are declared here to make them real
       * utilities the design system can compose with.                      */
      animation: {
        "fade-in": "ds-fade-in 150ms ease-out",
        "fade-in-up": "ds-fade-in-up 180ms ease-out",
      },
      keyframes: {
        "ds-fade-in": {
          from: { opacity: "0" },
          to: { opacity: "1" },
        },
        "ds-fade-in-up": {
          from: { opacity: "0", transform: "translateY(6px)" },
          to: { opacity: "1", transform: "none" },
        },
      },

      minHeight: {
        "screen-75": "75vh",
      },
      /* ── Opacity steps used by tinted surfaces / hairlines ─────────────── */
      opacity: {
        6: ".06",
        7: ".07",
        8: ".08",
        12: ".12",
        15: ".15",
        16: ".16",
        18: ".18",
        80: ".8",
      },
      zIndex: {
        2: 2,
        3: 3,
      },
      spacing: {
        /* Explicit 8px rhythm (see the "Spacing" block in the config header). */
        "ds-1": "4px",
        "ds-2": "8px",
        "ds-3": "12px",
        "ds-4": "16px",
        "ds-5": "24px",
        "ds-6": "32px",
        "ds-7": "40px",
        "ds-8": "48px",
        "ds-9": "64px",
        "sidebar": "260px",
        "topbar": "64px",
        "-100": "-100%",
        "-225-px": "-225px",
        "-160-px": "-160px",
        "-150-px": "-150px",
        "-94-px": "-94px",
        "-50-px": "-50px",
        "-29-px": "-29px",
        "-20-px": "-20px",
        "25-px": "25px",
        "40-px": "40px",
        "95-px": "95px",
        "145-px": "145px",
        "195-px": "195px",
        "210-px": "210px",
        "260-px": "260px",
        "65": "16.25rem",
        "66": "16.5rem",
        "67": "16.75rem",
        "68": "17rem",
        "69": "17.25rem",
        "70": "17.5rem",
        "71": "17.75rem",
        "72": "18rem",
        "73": "18.25rem",
        "76": "19rem",
        "80": "20rem",
      },
      height: {
        "95-px": "95px",
        "70-px": "70px",
        "350-px": "350px",
        "500-px": "500px",
        "600-px": "600px",
      },
      maxHeight: {
        "860-px": "860px",
      },
      maxWidth: {
        "100-px": "100px",
        "120-px": "120px",
        "150-px": "150px",
        "180-px": "180px",
        "200-px": "200px",
        "210-px": "210px",
        "580-px": "580px",
      },
      minWidth: {
        "140-px": "140px",
        48: "12rem",
      },
      backgroundSize: {
        full: "100%",
      },
    },
  },

  plugins: [
    require("@tailwindcss/forms"),
    plugin(function ({ addComponents, theme }) {
      // Custom plugin
    }),
  ],
});
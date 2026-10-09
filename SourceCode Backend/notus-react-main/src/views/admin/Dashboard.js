import React, { useEffect, useState } from "react";

// components
import HeaderStats from "components/Headers/HeaderStats.js";
import JobStatusPie from "./JobStatusPie";
import DifferenceChart from "./DifferenceChart";

// banner background (src/assets/img/greetingBackground.png)
import greetingBackground from "assets/img/greetingBackground.png";

/* Time-of-day greeting derived from the user's own clock — never hard-coded. */
const greetingFor = (hour) => {
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
};

/* Icon + colour that match the greeting (sun / cloud-sun / moon). */
const greetingIconFor = (hour) => {
  if (hour < 12) return { icon: "fa-sun", color: "text-amber-500" };
  if (hour < 17) return { icon: "fa-cloud-sun", color: "text-amber-500" };
  return { icon: "fa-moon", color: "text-indigo-500" };
};

/* Date like "Tue, 28 Sep 2026", time like "10:24 AM" */
const formatDate = (d) =>
  `${d.toLocaleDateString("en-GB", { weekday: "short" })}, ${d.toLocaleDateString(
    "en-GB",
    { day: "numeric", month: "short", year: "numeric" }
  )}`;

const formatTime = (d) =>
  d.toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });

export default function Dashboard() {
  /* Existing user identity — read from the same session storage the header
     and the auth context already use. No new request, no new dependency. */
  const userName =
    sessionStorage.getItem("user_name") || sessionStorage.getItem("user_id") || "";

  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    // Re-render once a minute so the clock and greeting stay accurate.
    const id = setInterval(() => setNow(new Date()), 60000);
    return () => clearInterval(id);
  }, []);

  const hour = now.getHours();
  const greetingIcon = greetingIconFor(hour);

  return (
    <div className="flex flex-col gap-ds-6">
      {/* ── Greeting banner ── */}
      <header
        className="relative isolate overflow-hidden rounded-2xl border border-ink-200 shadow-sm"
        style={{
          backgroundImage: `url(${greetingBackground})`,
          backgroundSize: "cover",
          backgroundPosition: "right center",
          backgroundRepeat: "no-repeat",
        }}
      >
        <div
          className="absolute inset-0 -z-10 bg-gradient-to-r from-white/95 via-white/70 to-transparent sm:from-white/90 sm:via-white/45"
          aria-hidden="true"
        />

        <div className="flex min-h-[240px] flex-col justify-center gap-5 px-6 py-10 sm:px-10 lg:min-h-[240px]">
          <i
            className={`fas ${greetingIcon.icon} ${greetingIcon.color} text-3xl`}
            aria-hidden="true"
          ></i>

          <h1 className="text-3xl font-semibold tracking-tight text-ink-900 sm:text-4xl">
            {greetingFor(hour)}
            {userName && (
              <>
                , <span className="text-[#4F5FD0]">{userName}</span>
              </>
            )}
          </h1>

          <div className="flex flex-wrap items-center gap-x-5 gap-y-2 text-body-sm text-ink-700 tabular-nums">
            <div className="flex items-center gap-2">
              <i className="far fa-calendar-alt text-ink-500" aria-hidden="true"></i>
              <time dateTime={now.toISOString().slice(0, 10)}>
                {formatDate(now)}
              </time>
            </div>

            <span className="hidden h-5 w-px bg-ink-300 sm:block" aria-hidden="true" />

            <div className="flex items-center gap-2">
              <i className="far fa-clock text-ink-500" aria-hidden="true"></i>
              <time dateTime={now.toTimeString().slice(0, 5)}>
                {formatTime(now)}
              </time>
            </div>
          </div>
        </div>
      </header>

      <div className="grid gap-ds-5 xl:grid-cols-[minmax(0,0.9fr)_minmax(0,2.1fr)] xl:items-stretch">
        <div className="min-w-0">
          <HeaderStats />
        </div>

        <div className="grid min-w-0 gap-ds-5 md:grid-cols-[minmax(0,1.5fr)_minmax(0,0.9fr)] xl:grid-cols-[minmax(0,1.6fr)_minmax(0,0.9fr)]">
          <div className="min-w-0">
            <JobStatusPie />
          </div>

          <div className="min-w-0">
            <div className="ds-card h-full overflow-hidden">
              <div className="ds-card-header">
                <div>
                  <h3 className="ds-card-title">Reading Trends</h3>
                  <p className="ds-card-subtitle mt-0.5">Reserved for future analysis</p>
                </div>
                <span className="ds-badge ds-badge-neutral">Planned</span>
              </div>

              <div className="ds-card-body flex min-h-[320px] flex-col items-center justify-center text-center">
                <div className="flex h-14 w-14 items-center justify-center rounded-xl border border-navy-100 bg-navy-50 text-lg text-navy-700">
                  <i className="fas fa-chart-line" aria-hidden="true"></i>
                </div>

                <p className="ds-section-title mt-4">Trend analysis</p>
                <p className="ds-body-sm mt-2 max-w-[220px] text-ink-500">
                  This panel is reserved for the upcoming reading-trend chart.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="w-full">
        <DifferenceChart />
      </div>
    </div>
  );
}
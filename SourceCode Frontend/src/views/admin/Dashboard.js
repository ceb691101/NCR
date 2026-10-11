import React, { useEffect, useState } from "react";

// components
import HeaderStats from "components/Headers/HeaderStats.js";
import JobStatusPie from "./JobStatusPie";
import InvoiceStageChart from "./InvoiceStageChart";
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
    <div className="flex flex-col gap-ds-4">
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
          className="absolute inset-0 -z-10 bg-gradient-to-r from-white/75 via-white/35 to-transparent sm:from-white/60 sm:via-white/30"
          aria-hidden="true"
        />

        {/* Greeting on the first line, date and time directly beneath it on a single
            horizontal row, aligned to the greeting text rather than pushed to
            the far edge of the banner. */}
        <div className="flex min-h-[116px] flex-col justify-center gap-2 px-6 py-5 sm:px-8">
          <div className="flex min-w-0 items-center gap-3">
            <i
              className={`fas ${greetingIcon.icon} ${greetingIcon.color} text-2xl flex-none`}
              aria-hidden="true"
            ></i>

            <h1 className="truncate text-2xl font-semibold tracking-tight text-ink-900 sm:text-[28px]">
              {greetingFor(hour)}
              {userName && (
                <>
                  , <span className="text-[#4F5FD0]">{userName}</span>
                </>
              )}
            </h1>
          </div>

          <div className="flex flex-wrap items-center gap-x-3 gap-y-1 pl-9 text-body-sm text-ink-700 tabular-nums">
            <span className="inline-flex items-center gap-1.5">
              <i className="far fa-calendar-alt text-ink-500" aria-hidden="true"></i>
              <time dateTime={now.toISOString().slice(0, 10)}>{formatDate(now)}</time>
            </span>

            <span className="h-4 w-px bg-ink-300" aria-hidden="true" />

            <span className="inline-flex items-center gap-1.5">
              <i className="far fa-clock text-ink-500" aria-hidden="true"></i>
              <time dateTime={now.toTimeString().slice(0, 5)}>{formatTime(now)}</time>
            </span>
          </div>
        </div>
      </header>

      {/* Four columns in one flat grid: summary stack, the two equally sized
          donuts, then the wider stages card. JobStatusPie renders its two cards
          through a `contents` wrapper, so it contributes columns 2 and 3 while
          fetching once. `items-stretch` plus `h-full` on the summary stack makes
          its three tiles exactly as tall as the donut beside them. */}
      <div className="grid gap-ds-4 md:grid-cols-2 xl:grid-cols-[minmax(0,0.58fr)_minmax(0,1fr)_minmax(0,1fr)_minmax(0,1.3fr)] xl:items-stretch">
        <HeaderStats />
        <JobStatusPie />
        <InvoiceStageChart />
      </div>

      <div className="w-full">
        <DifferenceChart />
      </div>
    </div>
  );
}
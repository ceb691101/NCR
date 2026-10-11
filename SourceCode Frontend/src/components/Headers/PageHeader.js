import React from "react";

export default function PageHeader({ title, subtitle, stats = [], actions }) {
  return (
    <header className="flex min-w-0 flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
      <div className="min-w-0">
        <h1 className="ds-page-title">{title}</h1>
        {subtitle && <p className="ds-page-subtitle mt-1">{subtitle}</p>}
      </div>

      {(stats.length > 0 || actions) && (
        <div className="flex min-w-0 flex-col gap-3 lg:items-end">
          {stats.length > 0 && (
            <div className="flex max-w-full flex-nowrap gap-2 overflow-x-auto pb-1 sm:flex-wrap sm:overflow-visible sm:pb-0 lg:justify-end">
              {stats.map((stat) => {
                const className = `inline-flex min-h-10 items-center gap-2 rounded-full border bg-white px-3.5 py-2 text-sm shadow-xs ${
                  stat.active ? "border-navy-300 ring-2 ring-navy-700/15" : "border-ink-200"
                } ${stat.onClick ? "cursor-pointer hover:border-navy-300 focus:outline-none focus-visible:ring-2 focus-visible:ring-navy-800/20" : ""}`;
                const content = (
                  <>
                    <span className={`font-extrabold tabular-nums ${stat.valueClassName || "text-navy-800"}`}>
                      {stat.value}
                    </span>
                    <span className="whitespace-nowrap font-medium text-ink-600">{stat.label}</span>
                  </>
                );

                return stat.onClick ? (
                  <button
                    key={stat.key || stat.label}
                    type="button"
                    onClick={stat.onClick}
                    aria-pressed={stat.active}
                    title={stat.detail || stat.label}
                    className={className}
                  >
                    {content}
                  </button>
                ) : (
                  <span
                    key={stat.key || stat.label}
                    title={stat.detail || undefined}
                    className={className}
                  >
                    {content}
                  </span>
                );
              })}
            </div>
          )}
          {actions}
        </div>
      )}
    </header>
  );
}
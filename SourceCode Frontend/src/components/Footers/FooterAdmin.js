import React from "react";

export default function FooterAdmin() {
  return (
    <footer className="px-4 sm:px-6 lg:px-8 pb-ds-6">
      <div className="w-full mx-auto">
        {/* Quiet closing rule so the footer reads as part of the page, not a
            separate band. Gutter matches .ds-content for alignment. */}
        <div className="border-t border-ink-200 pt-ds-4">
          <div className="flex flex-wrap items-center justify-center gap-x-2 gap-y-1 text-center">
            <span className="text-caption text-ink-500">
              &copy; {new Date().getFullYear()} Utility Solutions &amp; Automation Branch,
              Electricity Distribution Lanka (Private) Limited.
            </span>
            <span className="text-ink-300" aria-hidden="true">
              &middot;
            </span>
            <span className="ds-mono text-ink-400">v1.2.1</span>
          </div>
        </div>
      </div>
    </footer>
  );
}

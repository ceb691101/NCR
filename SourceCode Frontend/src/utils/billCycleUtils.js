const MIN_MONTH = 1;
const MAX_MONTH = 12;

/**
 * Shared bill-cycle display helpers.
 *
 * Month names are resolved from the real month number using the browser locale, so a
 * stored month of 8 always renders as the user's own name for August. No month name,
 * year or cycle number is hardcoded anywhere.
 */

const toMonthNumber = (month) => {
  if (month === null || month === undefined || month === "") return null;
  const value = Number(month);
  if (!Number.isInteger(value) || value < MIN_MONTH || value > MAX_MONTH) return null;
  return value;
};

/** "8" -> "August" (locale aware), or null when the value is not a real month. */
export const formatBillMonthName = (month) => {
  const monthNumber = toMonthNumber(month);
  if (monthNumber === null) return null;
  return new Intl.DateTimeFormat(undefined, { month: "long" }).format(
    new Date(2000, monthNumber - 1, 1)
  );
};

/** (8, 2025) -> "August 2025", or null when the month is not usable. */
export const formatBillPeriod = (month, year) => {
  const monthName = formatBillMonthName(month);
  if (monthName === null) return null;
  if (year === null || year === undefined || String(year).trim() === "") return monthName;
  return `${monthName} ${String(year).trim()}`;
};

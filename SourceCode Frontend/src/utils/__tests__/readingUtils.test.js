import { calculateRuDifference, isRuWithinLimit } from "../readingUtils";

describe("readingUtils RU Calculation and Validation", () => {
  describe("calculateRuDifference", () => {
    test("Formula: kwtUnits - (kwdUnits + kwpUnits + kwoUnits) with positive difference", () => {
      // kwt=1000, kwd=300, kwp=250, kwo=400 -> diff = 1000 - 950 = 50
      const diff = calculateRuDifference(300, 250, 400, 1000);
      expect(diff).toBe(50);
    });

    test("Formula: kwtUnits - (kwdUnits + kwpUnits + kwoUnits) with negative difference", () => {
      // kwt=900, kwd=300, kwp=300, kwo=350 -> diff = 900 - 950 = -50
      const diff = calculateRuDifference(300, 300, 350, 900);
      expect(diff).toBe(-50);
    });

    test("Supports object argument { kwdUnits, kwpUnits, kwoUnits, kwtUnits }", () => {
      const diff = calculateRuDifference({
        kwdUnits: 200,
        kwpUnits: 200,
        kwoUnits: 200,
        kwtUnits: 620,
      });
      expect(diff).toBe(20);
    });

    test("Returns null if total is missing or empty", () => {
      expect(calculateRuDifference(100, 100, 100, null)).toBeNull();
      expect(calculateRuDifference(100, 100, 100, "")).toBeNull();
      expect(calculateRuDifference(100, 100, 100, undefined)).toBeNull();
    });
  });

  describe("isRuWithinLimit", () => {
    test("abs(difference) < accept_ru is ACCEPTED (true)", () => {
      expect(isRuWithinLimit(10, 24)).toBe(true);
      expect(isRuWithinLimit(-10, 24)).toBe(true);
      expect(isRuWithinLimit(0, 24)).toBe(true);
    });

    test("abs(difference) == accept_ru is ACCEPTED (true) on exact positive boundary", () => {
      expect(isRuWithinLimit(24, 24)).toBe(true);
    });

    test("abs(difference) == accept_ru is ACCEPTED (true) on exact negative boundary", () => {
      expect(isRuWithinLimit(-24, 24)).toBe(true);
    });

    test("abs(difference) > accept_ru is REJECTED (false)", () => {
      expect(isRuWithinLimit(25, 24)).toBe(false);
      expect(isRuWithinLimit(-25, 24)).toBe(false);
      expect(isRuWithinLimit(100, 24)).toBe(false);
    });

    test("accept_ru == null / undefined / empty string is REJECTED (false)", () => {
      expect(isRuWithinLimit(10, null)).toBe(false);
      expect(isRuWithinLimit(10, undefined)).toBe(false);
      expect(isRuWithinLimit(10, "")).toBe(false);
    });

    test("null / invalid difference is REJECTED (false)", () => {
      expect(isRuWithinLimit(null, 24)).toBe(false);
      expect(isRuWithinLimit(undefined, 24)).toBe(false);
      expect(isRuWithinLimit(NaN, 24)).toBe(false);
    });

    test("Multiple developers with different accept_ru limits", () => {
      const diff = 15;
      // Developer A limit = 10 -> 15 > 10 -> REJECTED
      expect(isRuWithinLimit(diff, 10)).toBe(false);

      // Developer B limit = 24 -> 15 <= 24 -> ACCEPTED
      expect(isRuWithinLimit(diff, 24)).toBe(true);

      // Developer C limit = 50 -> 15 <= 50 -> ACCEPTED
      expect(isRuWithinLimit(diff, 50)).toBe(true);
    });
  });
});

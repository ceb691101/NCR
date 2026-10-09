import {
  normalizeAreaCode,
  matchAreaCode,
} from "../AreaAndBillService";

describe("normalizeAreaCode", () => {
  it("strips leading zeros so CHAR(2) codes compare with their numeric form", () => {
    expect(normalizeAreaCode("01")).toBe("1");
    expect(normalizeAreaCode("02")).toBe("2");
    expect(normalizeAreaCode("1")).toBe("1");
  });

  it("leaves codes without leading zeros untouched", () => {
    expect(normalizeAreaCode("55")).toBe("55");
    expect(normalizeAreaCode("10")).toBe("10");
  });

  it("never collapses to the empty string", () => {
    // "00" and "0" must agree with each other and with the backend,
    // which returns "0" rather than "".
    expect(normalizeAreaCode("00")).toBe("0");
    expect(normalizeAreaCode("0")).toBe("0");
  });

  it("treats blank and null input as no code", () => {
    expect(normalizeAreaCode(null)).toBe("");
    expect(normalizeAreaCode(undefined)).toBe("");
    expect(normalizeAreaCode("")).toBe("");
    expect(normalizeAreaCode("   ")).toBe("");
  });

  it("matches padded and unpadded forms of the same area", () => {
    expect(normalizeAreaCode("01")).toBe(normalizeAreaCode("1"));
    expect(matchAreaCode("01", "1")).toBe(true);
    expect(matchAreaCode("01", "01")).toBe(true);
  });

  it("does not match different areas", () => {
    expect(matchAreaCode("01", "02")).toBe(false);
    expect(matchAreaCode("01", "55")).toBe(false);
  });

  it("does not match when either side is blank", () => {
    expect(matchAreaCode("", "1")).toBe(false);
    expect(matchAreaCode("1", null)).toBe(false);
    expect(matchAreaCode(null, null)).toBe(false);
  });
});
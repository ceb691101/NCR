import { normalizeAreaCode, matchAreaCode } from "../AreaAndBillService";

// Mirrors the areas table: every region holds several provinces, and every province
// holds several areas whose codes are CHAR(2) ("01"). The dialog builds the
// region -> province -> area cascade from exactly this list.
const PERMITTED = [
  {
    region_code: "R1",
    province_code: "1",
    province_name: "Colombo",
    area_code: "01",
    area_name: "Colombo North",
  },
  {
    region_code: "R1",
    province_code: "1",
    province_name: "Colombo",
    area_code: "02",
    area_name: "Colombo East",
  },
  {
    region_code: "R1",
    province_code: "3",
    province_name: "Western",
    area_code: "55",
    area_name: "Gampaha",
  },
  {
    region_code: "R2",
    province_code: "2",
    province_name: "Kalutara",
    area_code: "21",
    area_name: "Ratmalana",
  },
  {
    region_code: "R2",
    province_code: "5",
    province_name: "Gampaha",
    area_code: "27",
    area_name: "Jaela",
  },
];

// These mirror the memoised derivations inside AreaAndBillDialog.
const regionsOf = (list) =>
  Array.from(
    new Set(list.map((a) => a.region_code).filter(Boolean))
  ).sort();

const provincesOf = (list, region) => {
  if (!region) return [];
  return Array.from(
    new Set(
      list.filter((a) => a.region_code === region).map((a) => a.province_code)
    )
  ).sort();
};

const areasOf = (list, region, province) => {
  if (!region || !province) return [];
  return list
    .filter((a) => a.region_code === region && a.province_code === province)
    .map((a) => a.area_code)
    .sort();
};

const codesOfAll = (list) => list.map((a) => a.area_code).sort();

describe("region -> province -> area cascade", () => {
  it("offers only the regions present in the permitted set", () => {
    expect(regionsOf(PERMITTED)).toEqual(["R1", "R2"]);
  });

  it("offers only the provinces inside the chosen region", () => {
    expect(provincesOf(PERMITTED, "R1")).toEqual(["1", "3"]);
    expect(provincesOf(PERMITTED, "R2")).toEqual(["2", "5"]);
  });

  it("offers only the areas inside the chosen region and province", () => {
    expect(areasOf(PERMITTED, "R1", "1")).toEqual(["01", "02"]);
    expect(areasOf(PERMITTED, "R1", "3")).toEqual(["55"]);
    expect(areasOf(PERMITTED, "R2", "5")).toEqual(["27"]);
  });

  it("keeps the cascade closed until a region is chosen", () => {
    expect(provincesOf(PERMITTED, "")).toEqual([]);
    expect(areasOf(PERMITTED, "", "")).toEqual([]);
  });

  it("keeps the cascade closed until a province is chosen", () => {
    expect(areasOf(PERMITTED, "R1", "")).toEqual([]);
  });

  it("never offers an area belonging to another province", () => {
    expect(areasOf(PERMITTED, "R1", "1")).not.toContain("55");
    expect(areasOf(PERMITTED, "R1", "3")).not.toContain("01");
  });

  it("never offers an area belonging to another region", () => {
    expect(areasOf(PERMITTED, "R1", "1")).not.toContain("21");
    expect(areasOf(PERMITTED, "R2", "2")).toEqual(["21"]);
  });

  it("cannot reach an area the user is not permitted for", () => {
    // Walk the whole cascade the way the dialog does, one level at a time.
    const reachable = [];
    regionsOf(PERMITTED).forEach((region) => {
      provincesOf(PERMITTED, region).forEach((province) => {
        areasOf(PERMITTED, region, province).forEach((code) => reachable.push(code));
      });
    });
    // Everything reachable equals the permitted set, nothing more.
    expect(reachable.sort()).toEqual(codesOfAll(PERMITTED));
  });

  it("selects a padded area by its unpadded form", () => {
    // The header pill stores "1" while the areas table stores "01".
    expect(PERMITTED.some((a) => matchAreaCode(a.area_code, "1"))).toBe(true);
    expect(areasOf(PERMITTED, "R1", "1").some((c) => matchAreaCode(c, "1"))).toBe(
      true
    );
  });

  it("does not select an area belonging to a different province", () => {
    const inProvince1 = areasOf(PERMITTED, "R1", "1");
    expect(inProvince1.some((c) => matchAreaCode(c, "21"))).toBe(false);
    expect(inProvince1.some((c) => matchAreaCode(c, "55"))).toBe(false);
  });

  it("normalises consistently for every code in the set", () => {
    PERMITTED.forEach((area) => {
      expect(normalizeAreaCode(area.area_code)).not.toBe("");
    });
    expect(normalizeAreaCode("01")).toBe("1");
  });
});
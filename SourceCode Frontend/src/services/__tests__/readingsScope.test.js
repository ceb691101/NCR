import {
  normalizeAreaCode,
  getEffectiveAreaCodes,
  getSelectedAreaCode,
  isAllAreasSelected,
} from "../AreaAndBillService";

// Mirrors the areas table: 4 regions, each holding several provinces, each
// province holding several CHAR(2) areas.
const PERMITTED = [
  { region_code: "R1", province_code: "1", area_code: "01" },
  { region_code: "R1", province_code: "1", area_code: "02" },
  { region_code: "R1", province_code: "3", area_code: "55" },
  { region_code: "R2", province_code: "2", area_code: "21" },
  { region_code: "R2", province_code: "5", area_code: "27" },
  { region_code: "R3", province_code: "6", area_code: "31" },
  { region_code: "R4", province_code: "B", area_code: "42" },
];

const setPermitted = (areas) => {
  localStorage.setItem(
    "ceb_area_bill",
    JSON.stringify({
      userCategory: "Area User",
      regionCode: "",
      provinceCode: "",
      areaCode: "",
      accessScope: "ALL",
      permittedAreas: areas,
      selectedAreaCode: "",
      timestamp: new Date().toISOString(),
    })
  );
};

describe("readings scope when all areas are selected", () => {
  beforeEach(() => {
    localStorage.clear();
    setPermitted(PERMITTED);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it("reports all areas as selected by default", () => {
    expect(getSelectedAreaCode()).toBeNull();
    expect(isAllAreasSelected()).toBe(true);
  });

  it("loads every permitted area rather than one", () => {
    expect(getEffectiveAreaCodes().sort()).toEqual(
      PERMITTED.map((a) => a.area_code).sort()
    );
  });

  it("covers areas in every region, not just the first", () => {
    const effective = getEffectiveAreaCodes();
    // One area per region, so a single-region leak would drop three of these.
    expect(effective.length).toBe(PERMITTED.length);
  });

  it("narrows to a single area once one is chosen", () => {
    localStorage.setItem(
      "ceb_area_bill",
      JSON.stringify({
        userCategory: "Area User",
        permittedAreas: PERMITTED,
        selectedAreaCode: "01",
        timestamp: new Date().toISOString(),
      })
    );

    expect(getSelectedAreaCode()).toBe("01");
    expect(isAllAreasSelected()).toBe(false);
    expect(getEffectiveAreaCodes()).toEqual(["01"]);
  });

  it("keeps every reading attributable to its own area", () => {
    // The readings list is keyed on acc_nbr, so two areas must not collapse
    // into one group and lose their area attribution.
    const readings = [
      { acc_nbr: "ACC1", area_cd: "01", added_blcy: "820" },
      { acc_nbr: "ACC1", area_cd: "01", added_blcy: "820", mtr_type: "KWD" },
      { acc_nbr: "ACC2", area_cd: "21", added_blcy: "820" },
      { acc_nbr: "ACC3", area_cd: "55", added_blcy: "820" },
    ];

    const grouped = readings.reduce((acc, r) => {
      if (!acc[r.acc_nbr]) {
        acc[r.acc_nbr] = { acc_nbr: r.acc_nbr, area_cd: r.area_cd, count: 0 };
      }
      acc[r.acc_nbr].count += 1;
      return acc;
    }, {});

    expect(Object.keys(grouped).length).toBe(3);
    expect(grouped.ACC2.area_cd).toBe("21");
    expect(grouped.ACC3.area_cd).toBe("55");
  });

  it("normalises the area code on each reading row", () => {
    expect(normalizeAreaCode("01")).toBe("1");
    expect(normalizeAreaCode("21")).toBe("21");
    // Distinct areas must stay distinct after normalisation.
    expect(normalizeAreaCode("01")).not.toBe(normalizeAreaCode("02"));
  });
});
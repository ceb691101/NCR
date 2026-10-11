// src/services/amendmentValidationRules.js
export const validationRules = {
  BFBL: {
    validate: (value) => /^-?\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a valid number (decimals and negatives allowed)",
    transform: (value) => value,
  },
  AREA: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  MNBR: {
    validate: (value) => /^[a-zA-Z0-9]+$/.test(value),
    errorMsg: "Must contain only letters and numbers",
    transform: (value) => value.toUpperCase(),
  },

  TRFC: {
    validate: (value) => true,
    errorMsg: "Invalid tariff value",
    transform: (value) => value.toUpperCase(),
  },
  CNTR: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  AGRN: {
    validate: (value) => true,
    errorMsg: "Invalid agreement number",
    transform: (value) => value.toUpperCase(),
  },
  TRCB: {
    validate: (value) => true,
    errorMsg: "Invalid transformer/cable value",
    transform: (value) => value.toUpperCase(),
  },
  TMET: {
    validate: (value) => true,
    errorMsg: "Invalid metering type",
    transform: (value) => value.toUpperCase(),
  },
  CUST: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  LNST: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  NOLN: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  TSEC: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  WLKO: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  SDEP: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  LINS: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  LBAL: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  TVLT: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  TVL2: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  EAMT: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  ADEP: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  ITYP: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  TLNO: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  FRFC: {
    validate: (value) => true,
    errorMsg: "Invalid value",
    transform: (value) => value.toUpperCase(),
  },
  IDDT: {
    validate: (value) => {
      const d = new Date(value);
      return !isNaN(d.getTime()) && d <= new Date();
    },
    errorMsg: "Date cannot be in the future",
    transform: (value) => value,
  },
  DPIV: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  CCAT: {
    validate: (value) => /^[bBcC]$/.test(value),
    errorMsg: "Must be B or C",
    transform: (value) => value.toUpperCase(),
  },
  TELN: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  GSTE: {
    validate: (value) => /^[yYnN]$/.test(value),
    errorMsg: "Must be Y or N",
    transform: (value) => value.toUpperCase(),
  },
  VATE: {
    validate: (value) => /^[yYnN]$/.test(value),
    errorMsg: "Must be Y or N",
    transform: (value) => value.toUpperCase(),
  },
  CGST: {
    validate: (value) => /^[yYnN]$/.test(value),
    errorMsg: "Must be Y or N",
    transform: (value) => value.toUpperCase(),
  },
  CAUT: {
    validate: (value) => true,
    errorMsg: "Invalid authorization letter value",
    transform: (value) => value.toUpperCase(),
  },
  TAXN: {
    validate: (value) => /^\d+-\d+$/.test(value),
    errorMsg: "Must be in format like 134001755-7000",
    transform: (value) => value,
  },
  CCCD: {
    validate: (value) => true,
    errorMsg: "Invalid customer code",
    transform: (value) => value.toUpperCase(),
  },
  CTYP: {
    validate: (value) => true,
    errorMsg: "Invalid customer type",
    transform: (value) => value.toUpperCase(),
  },
  CNET: {
    validate: (value) => /^[a-zA-Z0-9]+$/.test(value),
    errorMsg: "Must contain only letters and numbers",
    transform: (value) => value.toUpperCase(),
  },
  CCCT: {
    validate: (value) => true,
    errorMsg: "Invalid category code",
    transform: (value) => value.toUpperCase(),
  },
  SOFF: {
    validate: (value) => true,
    errorMsg: "Invalid setoff value",
    transform: (value) => value.toUpperCase(),
  },
  CSCH: {
    validate: (value) => /^\d+$/.test(value),
    errorMsg: "Must be a positive integer",
    transform: (value) => value,
  },
  CCAP: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
  CRAT: {
    validate: (value) => /^\d+(\.\d+)?$/.test(value),
    errorMsg: "Must be a positive number (decimals allowed)",
    transform: (value) => value,
  },
};

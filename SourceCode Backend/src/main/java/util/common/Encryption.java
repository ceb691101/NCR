package util.common;

import org.springframework.stereotype.Component;
import java.text.DecimalFormat;
import java.util.ArrayList;

@Component
public class Encryption {

    public boolean validateLogin(String userName, String password, String storedEncryptedPassword) throws Exception {
        Double M_PS = Double.parseDouble(checkPass(password));
        Double M_PU = Double.parseDouble(checkPass(userName.trim().toUpperCase()));

        Double M_P = ((M_PU + M_PS) / 100005600000.9987);

        // Format both values to 8 decimal places
        DecimalFormat df = new DecimalFormat("#.########");
        double calculatedVal = Double.parseDouble(df.format(M_P));
        double storedVal = Double.parseDouble(df.format(Double.parseDouble(storedEncryptedPassword)));

        return calculatedVal / storedVal == 1.0;
    }

    public String checkPass(String literal) {
        int PAS_LEN = 1;
        String M_PASS = "";
        char[] cArray = literal.trim().toCharArray();
        ArrayList<String> strList = new ArrayList<>();

        for (char c : cArray) strList.add(Character.toString(c));

        int cArrayLength = cArray.length;
        long LEN_TOT = 0L;

        while (PAS_LEN <= 10) {
            long PAS_CHAR, POS_CHAR;

            if (cArrayLength < PAS_LEN) {
                PAS_CHAR = (PAS_LEN == 0) ? 9999L : LEN_TOT;
            } else {
                PAS_CHAR = decrypt(strList.get(PAS_LEN - 1));
            }

            if (PAS_CHAR == 0L) return "";

            POS_CHAR = decrypt(String.valueOf(PAS_LEN));
            long ENC_CHAR = PAS_CHAR + POS_CHAR;
            LEN_TOT += ENC_CHAR;

            M_PASS = M_PASS.isEmpty() ? String.valueOf(ENC_CHAR) : M_PASS + ENC_CHAR;
            PAS_LEN++;
        }

        return M_PASS;
    }

    private long decrypt(String str) {
        switch (str) {
            case "A": return 2457L; case "B": return 2459L; case "C": return 2460L; case "D": return 2461L;
            case "E": return 2462L; case "F": return 2463L; case "G": return 2464L; case "H": return 2465L;
            case "I": return 2466L; case "J": return 2468L; case "K": return 2469L; case "L": return 2470L;
            case "M": return 2471L; case "N": return 2472L; case "O": return 2472L; case "P": return 2473L;
            case "Q": return 2473L; case "R": return 2474L; case "S": return 2475L; case "T": return 2476L;
            case "U": return 2478L; case "V": return 2478L; case "W": return 2479L; case "X": return 2481L;
            case "Y": return 2482L; case "Z": return 2483L;
            case "a": return 4055L; case "b": return 4056L; case "c": return 4057L; case "d": return 4060L;
            case "e": return 4061L; case "f": return 4063L; case "g": return 4064L; case "h": return 4065L;
            case "i": return 4066L; case "j": return 4068L; case "k": return 4069L; case "l": return 5470L;
            case "m": return 5471L; case "n": return 5472L; case "o": return 5472L; case "p": return 5473L;
            case "q": return 5473L; case "r": return 5474L; case "s": return 5475L; case "t": return 5476L;
            case "u": return 5478L; case "v": return 5478L; case "w": return 5479L; case "x": return 5481L;
            case "y": return 5482L; case "z": return 9999L;
            case "0": return 2502L; case "1": return 2503L; case "2": return 2504L; case "3": return 2505L;
            case "4": return 2506L; case "5": return 2507L; case "6": return 2508L; case "7": return 2509L;
            case "8": return 2561L; case "9": return 2562L; case "10": return 2563L;
            default: return 0L;
        }
    }
}
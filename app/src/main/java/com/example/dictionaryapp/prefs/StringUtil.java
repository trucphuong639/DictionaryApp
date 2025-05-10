package com.example.dictionaryapp.prefs;

import java.text.NumberFormat;
import java.util.Locale;

public class StringUtil {

    public static boolean isEmpty(String input) {
        return input == null || input.isEmpty() || ("").equals(input.trim());
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String formatVND(long amount){
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat formatter = NumberFormat.getInstance(localeVN);
        return formatter.format(amount) + " ₫";
    }
}

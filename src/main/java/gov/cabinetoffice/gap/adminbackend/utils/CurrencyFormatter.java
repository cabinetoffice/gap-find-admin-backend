package gov.cabinetoffice.gap.adminbackend.utils;

import java.math.BigInteger;
import java.text.CompactNumberFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class CurrencyFormatter {

    // UK Government definitions
    // https://commonslibrary.parliament.uk/research-briefings/sn04440/

    private static final String SYMBOL = Currency.getInstance(Locale.UK).getSymbol(Locale.UK);

    // format strings are positive, semicolon, negative - we want brackets around negative
    private static final String DEFAULT_PATTERN = SYMBOL + "#,##0.00;(" + SYMBOL + "#,##0.00)";

    // @formatter:off
    private static final String[] FORMAT_STRINGS = {
            "", "", "", // Units, Tens, Hundreds - use default
            "", "", "", // for Thousands, TenThousands, HundredThousands - use default
            SYMBOL + "0 million;("    + SYMBOL + "0 million)",
            SYMBOL + "00 million;("   + SYMBOL + "00 million)",
            SYMBOL + "000 million;("  + SYMBOL + "000 million)",
            SYMBOL + "0 billion;("    + SYMBOL + "0 billion)",
            SYMBOL + "00 billion;("   + SYMBOL + "00 billion)",
            SYMBOL + "000 billion;("  + SYMBOL + "000 billion)",
            SYMBOL + "0 trillion;("   + SYMBOL + "0 trillion)",
            SYMBOL + "00 trillion;("  + SYMBOL + "00 trillion)",
            SYMBOL + "000 trillion;(" + SYMBOL + "000 trillion)" };
    // @formatter:on

    private static final NumberFormat FORMATTER = new CompactNumberFormat(DEFAULT_PATTERN,
            DecimalFormatSymbols.getInstance(Locale.UK), FORMAT_STRINGS);

    static {
        FORMATTER.setMinimumFractionDigits(0);
        FORMATTER.setMaximumFractionDigits(2);
    }

    private CurrencyFormatter() {
    }

    public static String format(int i) {
        return FORMATTER.format(i);
    }

    public static String format(BigInteger bi) {
        if (bi == null) {
            return "";
        }
        return FORMATTER.format(bi);
    }

}

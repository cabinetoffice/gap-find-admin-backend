package gov.cabinetoffice.gap.adminbackend.utils;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class CurrencyFormatterTest {

    @Test
    void formatNull_returnsEmptyString() {
        String result = CurrencyFormatter.format(null);
        assertThat(result, is(equalTo("")));
    }

    @Test
    void format0() {
        String result = CurrencyFormatter.format(0);
        assertThat(result, is(equalTo("£0")));
    }

    @Test
    void formatNegative1() {
        String result = CurrencyFormatter.format(-1);
        assertThat(result, is(equalTo("(£1)")));
    }

    @Test
    void format1000() {
        String result = CurrencyFormatter.format(1000);
        assertThat(result, is(equalTo("£1,000")));
    }

    @Test
    void formatHalfMillion() {
        String result = CurrencyFormatter.format(500000);
        assertThat(result, is(equalTo("£500,000")));
    }

    @Test
    void format1million() {
        String result = CurrencyFormatter.format(1000000);
        assertThat(result, is(equalTo("£1 million")));
    }

    @Test
    void formatNegative1million() {
        String result = CurrencyFormatter.format(-1000000);
        assertThat(result, is(equalTo("(£1 million)")));
    }

    @Test
    void format1andOneHalfMillion() {
        String result = CurrencyFormatter.format(1500000);
        assertThat(result, is(equalTo("£1.5 million")));
    }

    @Test
    void formatNegative1andOneHalfMillion() {
        String result = CurrencyFormatter.format(-1500000);
        assertThat(result, is(equalTo("(£1.5 million)")));
    }

    @Test
    void format1andOneQuarterMillion() {
        String result = CurrencyFormatter.format(1250000);
        assertThat(result, is(equalTo("£1.25 million")));
    }

    @Test
    void format1andOneThirdMillion() {
        String result = CurrencyFormatter.format(1333333);
        assertThat(result, is(equalTo("£1.33 million")));
    }

    @Test
    void format1billion() {
        String result = CurrencyFormatter.format(1000000000);
        assertThat(result, is(equalTo("£1 billion")));
    }

    @Test
    void formatHalfBillion() {
        String result = CurrencyFormatter.format(500000000);
        assertThat(result, is(equalTo("£500 million")));
    }

    @Test
    void format10billion() {
        String result = CurrencyFormatter.format(new BigInteger("10000000000"));
        assertThat(result, is(equalTo("£10 billion")));
    }

    @Test
    void formatNegative10billion() {
        String result = CurrencyFormatter.format(new BigInteger("-10000000000"));
        assertThat(result, is(equalTo("(£10 billion)")));
    }

    @Test
    void format1trillion() {
        String result = CurrencyFormatter.format(new BigInteger("1000000000000"));
        assertThat(result, is(equalTo("£1 trillion")));
    }

    @Test
    void formatNegative2AndThreeQuarterTrillion() {
        String result = CurrencyFormatter.format(new BigInteger("-2750000000000"));
        assertThat(result, is(equalTo("(£2.75 trillion)")));
    }

}

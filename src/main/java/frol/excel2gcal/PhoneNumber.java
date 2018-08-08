package frol.excel2gcal;

public class PhoneNumber {
    private final String country;
    private final String prefix;
    private final String number;

    public PhoneNumber(String country, String prefix, String number) {
        this.country = country;
        this.prefix = prefix;
        this.number = number;
    }

    public String getCountry() {
        return country;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return String.format("+%s %s %s", country, prefix, number);
    }
}

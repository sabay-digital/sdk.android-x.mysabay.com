package kh.com.mysabay.sdk.ui.holder;

public class CountryItem {
    private String name;
    private String dial_code;
    private String code;

    public CountryItem(String name, String dial_code, String code) {
        this.name = name;
        this.dial_code = dial_code;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getDial_code() {
        return dial_code;
    }

    public String getCode() {
        return code;
    }
}

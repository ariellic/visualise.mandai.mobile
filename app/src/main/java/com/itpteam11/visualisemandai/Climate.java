package com.itpteam11.visualisemandai;

/**
 * This class represents the PSI, weather and temperature data model
 */
public class Climate {
    private String description;
    private String value;
    private String valueLong;

    public Climate() {}

    public String getDescription() { return description; }
    public String getValue() { return value; }
    public String getValueLong() { return valueLong; }

    public void setValueLong(String value) { this.valueLong = valueLong; }
    public void setValue(Object value) { this.value = String.valueOf(value); }
}

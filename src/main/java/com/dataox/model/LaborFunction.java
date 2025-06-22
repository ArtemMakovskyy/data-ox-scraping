package com.dataox.model;

import java.util.Arrays;

public enum LaborFunction {
    ACCOUNTING_FINANCE("Accounting & Finance"),
    ADMINISTRATION("Administration"),
    COMPLIANCE_REGULATORY("Compliance / Regulatory"),
    CUSTOMER_SERVICE("Customer Service"),
    DATA_SCIENCE("Data Science"),
    DESIGN("Design"),
    IT("IT"),
    LEGAL("Legal"),
    MARKETING_COMMUNICATIONS("Marketing & Communications"),
    OPERATIONS("Operations"),
    OTHER_ENGINEERING("Other Engineering"),
    PEOPLE_HR("People & HR"),
    PRODUCT("Product"),
    QUALITY_ASSURANCE("Quality Assurance"),
    SALES_BUSINESS_DEVELOPMENT("Sales & Business Development"),
    SOFTWARE_ENGINEERING("Software Engineering");

    private final String label;

    LaborFunction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static LaborFunction fromString(String input) {
        return Arrays.stream(values())
                .filter(f -> f.label.equalsIgnoreCase(input.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid labor function: " + input));
    }
}

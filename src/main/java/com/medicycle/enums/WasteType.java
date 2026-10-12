package com.medicycle.enums;

public enum WasteType {
    SHARPS("Sharps (Needles, Scalpels)"),
    INFECTIOUS("Infectious Waste (Cultures, Swabs)"),
    PATHOLOGICAL("Pathological Waste (Tissues, Organs)"),
    PHARMACEUTICAL("Pharmaceutical Waste (Expired Drugs)"),
    CHEMICAL("Chemical Waste (Laboratory Chemicals)"),
    RADIOACTIVE("Radioactive Waste"),
    PRESSURIZED("Pressurized Containers (Aerosols)"),
    GENERAL_CLINICAL("General Clinical Waste (Non-Hazardous)");

    private final String displayName;

    WasteType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

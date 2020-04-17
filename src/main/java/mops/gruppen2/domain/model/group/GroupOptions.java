package mops.gruppen2.domain.model.group;

import lombok.Value;

//TODO: doooooodododo
@Value
class GroupOptions {

    // Gruppe
    boolean showClearname;
    boolean hasBody;
    boolean isLeavable;
    boolean hasLink;

    String customLogo;
    String customBackground;
    String customTitle;

    // Integrations
    boolean hasMaterialIntegration;
    boolean hasTermineIntegration;
    boolean hasPortfolioIntegration;
    boolean hasForumsIntegration;
    boolean hasModulesIntegration;

    static GroupOptions DEFAULT() {
        return new GroupOptions(true,
                                false,
                                true,
                                false,
                                null,
                                null,
                                null,
                                true,
                                true,
                                true,
                                true,
                                true);
    }
}

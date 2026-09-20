package com.krisapps.pantry.types.data.inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PantryItem(
        int databaseId,
        String name,
        String description,
        double quantity,
        double quantityPerPack,
        double preferredMinimumQuantity,
        LocalDate expiresAt,
        LocalDateTime lastStockedAt,
        double lastStockedQuantity,
        ItemLocation location,
        ItemType type,
        ItemGroup group,
        ItemCategory category
) {

    boolean isLowOnStock() {
        return quantity < preferredMinimumQuantity;
    }

    boolean hasExpired() {
        return expiresAt.isBefore(LocalDate.now());
    }

    /**
     * Returns the amount of packs owned
     * @return the total units divided by unit count per pack
     */
    double packs() {
        return quantity / quantityPerPack;
    }
}

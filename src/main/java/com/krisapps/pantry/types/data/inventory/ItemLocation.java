package com.krisapps.pantry.types.data.inventory;

import org.jetbrains.annotations.Nullable;

public record ItemLocation(
        int databaseId,
        String name,
        @Nullable String description,
        @Nullable ItemLocation parent
) {
    public boolean hasParentLocation() {
        return parent != null;
    }
}

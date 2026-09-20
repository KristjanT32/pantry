package com.krisapps.pantry.types.data.inventory;

import org.jetbrains.annotations.Nullable;

public record ItemCategory(
        int databaseId,
        String name,
        @Nullable String description
) {
    public static final ItemCategory NONE = new ItemCategory(0, "No category assigned", "There is no category assigned to this item.");
}

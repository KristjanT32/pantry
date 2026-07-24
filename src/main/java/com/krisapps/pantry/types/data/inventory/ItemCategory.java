package com.krisapps.pantry.types.data.inventory;

import org.jetbrains.annotations.Nullable;

public record ItemCategory(
        int databaseId,
        String name,
        @Nullable String description
) {
}

package com.krisapps.pantry.types.data.inventory;

import org.jetbrains.annotations.Nullable;

public record ItemGroup(
        int databaseId,
        String name,
        @Nullable String description,
        @Nullable ItemGroup parent
) {
    public boolean hasParentGroup() {
        return parent != null;
    }
}

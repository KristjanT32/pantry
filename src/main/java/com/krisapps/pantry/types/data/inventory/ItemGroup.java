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

    public static final ItemGroup NONE = new ItemGroup(0, "No group assigned", "There is no group assigned to this item.", null);
}

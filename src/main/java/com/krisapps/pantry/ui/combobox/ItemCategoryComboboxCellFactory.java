package com.krisapps.pantry.ui.combobox;

import com.krisapps.pantry.types.data.inventory.ItemCategory;
import com.krisapps.pantry.types.data.inventory.ItemGroup;
import com.krisapps.pantry.ui.combobox.cell.ItemCategoryComboboxCell;
import com.krisapps.pantry.ui.combobox.cell.ItemGroupComboboxCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ItemCategoryComboboxCellFactory implements Callback<ListView<ItemCategory>, ListCell<ItemCategory>> {

    @Override
    public ListCell<ItemCategory> call(ListView<ItemCategory> param) {
        return new ItemCategoryComboboxCell();
    }
}

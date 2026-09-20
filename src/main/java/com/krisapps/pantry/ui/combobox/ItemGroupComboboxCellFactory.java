package com.krisapps.pantry.ui.combobox;

import com.krisapps.pantry.types.data.inventory.ItemGroup;
import com.krisapps.pantry.ui.combobox.cell.ItemGroupComboboxCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ItemGroupComboboxCellFactory implements Callback<ListView<ItemGroup>, ListCell<ItemGroup>> {

    @Override
    public ListCell<ItemGroup> call(ListView<ItemGroup> param) {
        return new ItemGroupComboboxCell();
    }
}

package com.krisapps.pantry.ui.listview;

import com.krisapps.pantry.types.data.inventory.PantryItem;
import com.krisapps.pantry.ui.listview.cell.ReadOnlyInventoryItemCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ReadOnlyInventoryItemCellFactory implements Callback<ListView<PantryItem>, ListCell<PantryItem>> {
    @Override
    public ListCell<PantryItem> call(ListView<PantryItem> param) {
        return new ReadOnlyInventoryItemCell();
    }
}

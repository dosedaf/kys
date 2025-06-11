package src.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import src.model.Category;

public class CategoryTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Name", "Description", "Type"};
    private List<Category> categories;

    public CategoryTableModel() {
        this.categories = new ArrayList<>();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        fireTableDataChanged();
    }

    public Category getCategoryAt(int rowIndex) {
        return categories.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return categories.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Category category = categories.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return category.getId();
            case 1:
                return category.getName();
            case 2:
                return category.getDescription();
            case 3:
                return category.getType();
            default:
                return null;
        }
    }
}
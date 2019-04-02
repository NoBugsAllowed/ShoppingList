package com.example.shoppinglist;

public class ListItem {
    private String text;
    private boolean isSelected;

    public ListItem(String text, boolean isSelected) {
        this.text = text;
        this.isSelected = isSelected;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

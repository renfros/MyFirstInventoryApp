package com.example.inventory;

public class InventoryItems implements Comparable<InventoryItems>{

    private String name;
    private int amount;

    InventoryItems(String name, int amount){
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public int compareTo(InventoryItems o) {
        return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
    }
}

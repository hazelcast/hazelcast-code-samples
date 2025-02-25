package com.hazelcast.hibernate.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.ArrayList;
import java.util.List;

@Entity
@Cache(region = "Item-Cache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Item {

    @Id
    @Column(name = "ITEM_ID")
    private int id;

    @Column(name = "ITEM_NAME", nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    @Cache(region = "SubItems-Collection-Cache", usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<SubItem> subItems = new ArrayList<>();

    public Item() { }

    public Item(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public Item addSubItem(SubItem s) {
        this.subItems.add(s);
        return this;
    }

    public List<SubItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<SubItem> subItems) {
        this.subItems = subItems;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

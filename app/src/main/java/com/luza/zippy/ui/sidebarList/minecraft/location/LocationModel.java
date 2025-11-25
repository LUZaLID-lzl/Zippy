package com.luza.zippy.ui.sidebarList.minecraft.location;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "locations")
public class LocationModel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int x;
    private int y;
    private String connectionType; // "X", "Y", "NONE"
    private String dimension; // "OVERWORLD", "NETHER", "END"
    private String description;

    public LocationModel(String name, int x, int y, String connectionType, String dimension, String description) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.connectionType = connectionType;
        this.dimension = dimension;
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getConnectionType() { return connectionType; }
    public String getDimension() { return dimension; }
    public String getDescription() { return description; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public void setDescription(String description) { this.description = description; }
} 
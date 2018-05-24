package com.ekylibre.android.utils;


public class Unit {

    public String name;
    public String key;
    public float quantity_factor;
    public float surface_factor;

    Unit(String key, float quantity_factor) {
        this.key = key;
        this.quantity_factor = quantity_factor;
        this.surface_factor = 0;
    }

    Unit(String key, float quantity_factor, float surface_factor) {
        this.key = key;
        this.quantity_factor = quantity_factor;
        this.surface_factor = surface_factor;
    }

    public Unit getUnit(String name, String dimension) {
        switch (dimension) {
            case "volume":
                for (Unit unit : Units.VOLUME_UNITS) {
                    if (unit.name.equals(name))
                        return unit;
                }
            case "mass":
                for (Unit unit : Units.MASS_UNITS) {
                    if (unit.name.equals(name))
                        return unit;
                }
        }
        return null;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

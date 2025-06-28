package com.carmanagement.carmanagement;

public class Vehicle {
    protected String numberplate;
    protected String registereddistrict;
    protected String enginenumber;
    protected int enginecapacity;
    protected String vehicletype;

    public Vehicle(String numberplate, String registereddistrict, String enginenumber, int enginecapacity, String vehicletype) {
        this.numberplate = numberplate;
        this.registereddistrict = registereddistrict;
        this.enginenumber = enginenumber;
        this.enginecapacity = enginecapacity;
        this.vehicletype = vehicletype;
    }

    public String getNumberplate() {
        return numberplate;
    }

    public void setNumberplate(String numberplate) {
        this.numberplate = numberplate;
    }

    public String getRegistereddistrict() {
        return registereddistrict;
    }

    public void setRegistereddistrict(String registereddistrict) {
        this.registereddistrict = registereddistrict;
    }

    public String getEnginenumber() {
        return enginenumber;
    }

    public void setEnginenumber(String enginenumber) {
        this.enginenumber = enginenumber;
    }

    public int getEnginecapacity() {
        return enginecapacity;
    }

    public void setEnginecapacity(int enginecapacity) {
        this.enginecapacity = enginecapacity;
    }

    public String getVehicletype() {
        return vehicletype;
    }

    public void setVehicletype(String vehicletype) {
        this.vehicletype = vehicletype;
    }
}

package edu.hitsz.prop;

public class FreezeProp extends BaseProp {
    public FreezeProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {

        System.out.println("FreezeProp active!");
    }
}

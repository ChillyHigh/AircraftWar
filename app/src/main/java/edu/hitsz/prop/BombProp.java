package edu.hitsz.prop;

public class BombProp extends BaseProp {
    public BombProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {

        System.out.println("BombProp active!");
    }
}

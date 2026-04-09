package edu.hitsz.factory;

import edu.hitsz.prop.*;

public class PropFactory {
    public static BaseProp creatProp(String type, double locationX, double locationY, double speedX, double speedY) {
        switch (type) {
            case "Blood":
                return new BloodProp(locationX, locationY, speedX, speedY);
            case "Bullet":
                return new BulletProp(locationX, locationY, speedX, speedY);
            case "BulletPlus":
                return new BulletPlusProp(locationX, locationY, speedX, speedY);
            case "Bomb":
                return new BombProp(locationX, locationY, speedX, speedY);
            case "Freeze":
                return new FreezeProp(locationX, locationY, speedX, speedY);
            default:
                return null;
        }
    }
}

package edu.hitsz.prop;

import edu.hitsz.observer.PropObserver;
import edu.hitsz.observer.PropSubject;

import java.util.ArrayList;
import java.util.List;

public class BombProp extends BaseProp implements PropSubject {
    private final List<PropObserver> observers = new ArrayList<>();

    public BombProp(double locationX, double locationY, double speedX, double speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void addObserver(PropObserver observer) {
        observers.add(observer);
    }

    @Override
    public void clearObservers() {
        observers.clear();
    }

    @Override
    public void notifyObservers() {
        for (PropObserver observer : observers) {
            observer.onBomb();
        }
    }

    @Override
    public void activate() {
        notifyObservers();
        clearObservers();
        System.out.println("BombProp active!");
    }
}

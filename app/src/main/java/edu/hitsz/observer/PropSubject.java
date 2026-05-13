package edu.hitsz.observer;

public interface PropSubject {
    void addObserver(PropObserver observer);

    void clearObservers();

    void notifyObservers();
}

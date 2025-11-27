package ru.rsreu.thirdeye;

@FunctionalInterface
public interface NewObjectCalback {
    void onNewObjectDetected(TrackedObject obj);

}

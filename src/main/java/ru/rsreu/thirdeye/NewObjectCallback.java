package ru.rsreu.thirdeye;

@FunctionalInterface
public interface NewObjectCallback {
    void onNewObjectDetected(TrackedObject obj);

}

package nz.ac.auckland.compass.haamos.util;




public interface IObserver {
    void update(Object theObserved, Object changedCode);
}
package nz.ac.auckland.compass.haamos.util;



public interface IObservable {
   public abstract void addIObserver(IObserver anIObserver);
   public abstract void deleteIObserver(IObserver anIObserver);
   public abstract void deleteIObservers();
}
package nz.ac.auckland.compass.haamos.util;


/**
* @version 1.0 28/08/97
* @author Babak Mahdavi
*/

import java.util.Vector;
import java.util.Enumeration;


public class ObservableComponent extends Object {
    private Vector myIObservers = new Vector();

    public void addIObserver(IObserver anIObserver) {
        this.myIObservers.addElement(anIObserver);
    }

    public void deleteIObserver(IObserver anIObserver) {
        this.myIObservers.removeElement(anIObserver);
    }

    public void deleteObservers() {
        this.myIObservers.removeAllElements();
    }

    public void notifyIObservers(Object theObserved, Object changedCode) {
        Enumeration myIObserversList = this.myIObservers.elements();

        while (myIObserversList.hasMoreElements()) {
            IObserver anIObserver = (IObserver) myIObserversList.nextElement();
            anIObserver.update(theObserved,changedCode);
        }
    }


}
package org.loezto.e.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

abstract class ModelElement {

	PropertyChangeSupport pcs;

	public ModelElement() {
		pcs = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (pcs == null)
			pcs = new PropertyChangeSupport(this);
		pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String property,
			PropertyChangeListener listener) {
		if (pcs == null)
			pcs = new PropertyChangeSupport(this);
		pcs.addPropertyChangeListener(property, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String property,
			PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(property, listener);
	}

	void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

}

package org.loezto.e.viewerfilters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.loezto.e.model.Entry;

public class TaskEntryFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Entry entry;

		if (element instanceof Entry)
			entry = (Entry) element;
		else
			return false;

		if (entry.getTask() == null)
			return true;

		return false;
	}
}

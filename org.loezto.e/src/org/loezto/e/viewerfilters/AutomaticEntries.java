package org.loezto.e.viewerfilters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.loezto.e.model.Entry;

public class AutomaticEntries extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof Entry)
			if (((Entry) element).getType().matches(" N"))
				return true;
			else
				return false;
		return false;
	}
}
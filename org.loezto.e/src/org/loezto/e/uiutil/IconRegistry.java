package org.loezto.e.uiutil;

import java.util.Calendar;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.loezto.e.model.Task;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class IconRegistry {
	
	private static IconRegistry instance;
	
	public static IconRegistry getInstance ()
	{
		if (instance == null)
			instance = new IconRegistry ();
		
		return instance;
	}

	private Image tickIcon;
	private Image dateIcon;
	private Image datePastIcon;
	
	IconRegistry ()
	{
		final Bundle bundle = FrameworkUtil.getBundle(this.getClass());

		tickIcon = new LocalResourceManager(JFaceResources.getResources()).createImage(
				ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("img/icons/tick.png"), null)));
		dateIcon = new LocalResourceManager(JFaceResources.getResources()).createImage(
				ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("img/icons/date.png"), null)));
		datePastIcon = new LocalResourceManager(JFaceResources.getResources()).createImage(
				ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("img/icons/date_error.png"), null)));

	}
	
	public Image of (Task task)
	{
		if (task.getCompletionDate() != null)
			return tickIcon;
		else if (task.getDueDate() != null)
			if (task.getDueDate().before(Calendar.getInstance().getTime()))
				return datePastIcon;
			else
				return dateIcon;
		return null;
	}

}

package org.loezto.e.lifecycle;

import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * 
 * Tried to create a life cycle manager to circumvent the weird looking
 * application name on Gnome (it shows as "Ã©")
 * 
 * However, could not yet find the API to set the app title. The class has not
 * been tied to the model, yet, so it is not in use at all
 * 
 * @author danilo
 *
 *
 */
public class LifeCycleManager {

	@PostContextCreate
	void postContextCreate(IApplicationContext ctx) {
		ctx.getBrandingName();

	}

}

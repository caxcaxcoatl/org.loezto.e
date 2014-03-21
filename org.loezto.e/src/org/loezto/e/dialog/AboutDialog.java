package org.loezto.e.dialog;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.FrameworkUtil;

final public class AboutDialog extends Dialog {
	public AboutDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	/**
	 * @wbp.parser.entryPoint 
	 */
	public Control createDialogArea(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		Label label = new Label(container, SWT.CENTER);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false,
				1, 1));
		label.setAlignment(SWT.CENTER);

		LocalResourceManager lrm = new LocalResourceManager(
				JFaceResources.getResources(), parent);
		ImageDescriptor desc = ImageDescriptor.createFromURL(FileLocator.find(
				FrameworkUtil.getBundle(this.getClass()), new Path(
						"img/e128.png"), null));
		Image eh = lrm.createImage(desc);
		label.setImage(eh);

		Label lblInfo = new Label(container, SWT.CENTER);
		lblInfo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true,
				1, 1));
		lblInfo.setText("\n\né - a personal information manager - v0.1\n\n\nReleased under the Eclipse Public License 1.0");

		label.pack();
		lblInfo.pack();
		return super.createDialogArea(parent);
	}
}

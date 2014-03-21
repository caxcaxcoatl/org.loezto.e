package org.loezto.e.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ReportDialog extends Dialog {

	String report;
	private Text text;

	public ReportDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// Composite container = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));

		text = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL
				| SWT.MULTI);

		text.setText(report);

		GridData textLayout = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		textLayout.heightHint = (int) (parent.getShell().getDisplay()
				.getBounds().height * 2f / 4f);
		textLayout.widthHint = (int) (parent.getShell().getDisplay()
				.getBounds().width * 3f / 4f);
		text.setLayoutData(textLayout);

		return parent;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}

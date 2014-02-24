package org.loezto.e.part;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class TestPart {
	private Text text;

	public TestPart() {
	}

	@PostConstruct
	void buildUI(Composite parent, MPart part) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		text = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		text.setText(part.getLabel());

	}
}

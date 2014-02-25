package org.loezto.e.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.loezto.e.model.Entry;
import org.loezto.e.model.Topic;

public class TestPart {
	private Text text;
	private Text txtAsdf;

	public TestPart() {
	}

	@PostConstruct
	void buildUI(Composite parent, MPart part) {
		parent.setLayout(new GridLayout(2, false));
		new Label(parent, SWT.NONE);

		text = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text.setText(part.getLabel());
		new Label(parent, SWT.NONE);

		txtAsdf = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL
				| SWT.MULTI);
		txtAsdf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		txtAsdf.setText("ASDF");

	}

	@Inject
	@Optional
	void selectionChanged(IStructuredSelection sel, MPart part) {

		text.setText(part.getLabel() + " "
				+ ((Topic) sel.getFirstElement()).getName());
	}

	@Inject
	@Optional
	void selectionChanged(@Named("E_CURRENT_TOPIC") Topic topic, MPart part) {
		text.setText(part.getLabel() + "-" + topic.getName());

	}

	@Inject
	@Optional
	void selectionChanged(@Named("E_CURRENT_ENTRY") Entry entry, MPart part) {
		txtAsdf.setText(part.getLabel() + "-" + entry.getText());

	}

}

package org.loezto.e.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.loezto.e.model.Topic;

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
}

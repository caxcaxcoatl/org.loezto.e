 
package org.loezto.e.part;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;

public class PlanningPart {
	private Table table;
	@Inject
	public PlanningPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		Group group = new Group(scrolledComposite, SWT.NONE);
		
		TableViewer tableViewer = new TableViewer(group, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setBounds(0, 112, 72, 72);
		scrolledComposite.setContent(group);
		scrolledComposite.setMinSize(group.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
	}
}
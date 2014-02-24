package org.loezto.e.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.loezto.e.model.EService;

public class TopicTreePart {

	public TopicTreePart() {
	}

	@Inject
	EService eService;

	@Inject
	Logger log;

	@Inject
	EMenuService menuService;

	private Tree tree;

	@PostConstruct
	void buidUI(Composite parent) {

		TreeViewer treeViewer = new TreeViewer(parent, SWT.BORDER);
		tree = treeViewer.getTree();
		tree.getItems();

		menuService.registerContextMenu(tree,
				"org.loezto.e.popupmenu.topictree");

	}

	@Focus
	void onFocus() {
		tree.setFocus();
	}

}

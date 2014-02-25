package org.loezto.e.part;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.loezto.e.events.EEvents;
import org.loezto.e.model.EService;
import org.loezto.e.model.Topic;

public class TopicTreePart {

	public TopicTreePart() {
	}

	@Inject
	@Optional
	EService eService;

	@Inject
	Logger log;

	@Inject
	EMenuService menuService;

	@Inject
	IEclipseContext eContext;

	private Tree tree;

	private TreeViewer treeViewer;

	private Topic rootTopic;

	@PostConstruct
	void buidUI(Composite parent) {

		treeViewer = new TreeViewer(parent, SWT.BORDER);
		tree = treeViewer.getTree();
		tree.setLinesVisible(true);

		menuService.registerContextMenu(tree,
				"org.loezto.e.popupmenu.topictree");

		setupViewer();

	}

	private void setupViewer() {

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IEclipseContext pContext = eContext.get(MPerspective.class)
						.getContext();
				pContext.set(IStructuredSelection.class.getName(),
						event.getSelection());

			}
		});

		treeViewer.setLabelProvider(new ILabelProvider() {

			@Override
			public String getText(Object element) {
				if (element == null)
					return "";
				if (element instanceof Topic)
					return ((Topic) element).getName();
				return null;
			}

			@Override
			public Image getImage(Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void addListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub

			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub

			}
		});

		treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// TODO Auto-generated method stub

			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof Topic)
					return !((Topic) element).getChildren().isEmpty();
				return false;
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof Topic)
					return ((Topic) element).getParent();
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement == null)
					return null;

				List<?> list;

				if (inputElement instanceof List<?>) {
					list = (List<?>) inputElement;
					if (!list.isEmpty() && list.get(0) instanceof Topic)
						return list.toArray();
					else
						return null;
				} else
					return null;

			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Topic)
					return ((Topic) parentElement).getChildren().toArray();
				return null;
			}
		});

		List<Topic> list = eService.getRootTopics();
		treeViewer.setInput(list);

		// rootTopic = eService.getRootTopic();
		// ViewerSupport.bind(treeViewer, rootTopic,
		// BeanProperties.list("children"), BeanProperties.value("name"));

		// BeansObservables.

		// WritableValue input = new WritableValue(eService.getTopic(87),
		// Topic.class);
		// ViewerSupport.bind(viewer, input, childrenProperty, labelProperty);
		// ViewerSupport.bind(treeViewer, BeansObservables.observeDetailList(
		// input, "children", Topic.class), BeanProperties.value("name"));

	}

	@Inject
	@Optional
	void updateTree(@UIEventTopic(EEvents.TOPIC_ADD) Topic topic) {
		log.debug("Refreshing topics from event");
		treeViewer.setInput(eService.getRootTopics());
		treeViewer.setSelection(new StructuredSelection(topic));
		tree.showSelection();

		// treeViewer.refresh(topic.getParent());
		// treeViewer.refresh(topic.getParent());
	}

	@Focus
	void onFocus() {
		tree.setFocus();
	}

}

package org.loezto.e.part;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
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

	@Inject
	Shell shell;

	private Tree tree;

	private TreeViewer treeViewer;

	@PostConstruct
	void buidUI(Composite parent) {

		treeViewer = new TreeViewer(parent, SWT.BORDER);
		tree = treeViewer.getTree();

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
				Object firstElement = ((IStructuredSelection) event
						.getSelection()).getFirstElement();
				if (firstElement instanceof Topic) {
					pContext.set("E_CURRENT_TASK", null);
					pContext.set("E_CURRENT_TOPIC", (Topic) firstElement);
				}
				if (firstElement != null)
					shell.setText("é - " + ((Topic) firstElement).getFullName());
				else
					shell.setText("é");

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
					return new Object[] {};

				List<?> list;

				if (inputElement instanceof List<?>) {
					list = (List<?>) inputElement;
					if (!list.isEmpty() && list.get(0) instanceof Topic)
						return list.toArray();
					else
						return new Object[] {};
				} else
					return new Object[] {};

			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Topic)
					return ((Topic) parentElement).getChildren().toArray();
				return null;
			}
		});

		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		int operations = DND.DROP_MOVE;
		treeViewer.addDragSupport(operations, transferTypes,
				new DragSourceListener() {

					@Override
					public void dragStart(DragSourceEvent event) {
					}

					@Override
					public void dragSetData(DragSourceEvent event) {
						System.out.println("Go");
						LocalSelectionTransfer.getTransfer().setSelection(
								treeViewer.getSelection());
					}

					@Override
					public void dragFinished(DragSourceEvent event) {
					}
				});

		treeViewer.addDropSupport(operations, transferTypes,
				new ViewerDropAdapter(treeViewer) {

					@Override
					public void dragEnter(DropTargetEvent event) {
						Object target = getCurrentTarget();
						if (!canDrop(target))
							event.detail = DND.DROP_NONE;
						setFeedbackEnabled(false);
					}

					@Override
					public boolean validateDrop(Object target, int operation,
							TransferData transferType) {
						return canDrop(target);
					}

					private boolean canDrop(Object target) {
						Topic targetTopic;
						Topic source;

						// http://www.eclipse.org/articles/Article-Workbench-DND/drag_drop.html
						//
						// In SWT, the transfer of data from the source to the
						// target is done lazily when the drop is initiated. So,
						// as the user is dragging, the destination has no way
						// of finding out what source object is being dragged
						// until the drop is performed
						//
						// TODO Take this out
						source = (Topic) ((IStructuredSelection) treeViewer
								.getSelection()).getFirstElement();

						// New root element
						if (target == null) {
							log.debug("Target is null, indicating it's being moved to the root topic.");
							if (source.getParent().equals(
									eService.getRootTopic())) {
								log.debug("Target is already under root topic");
								return false;
							} else {
								log.debug("DND is acceptable");
								return true;
							}
						}

						if (target instanceof Topic)
							targetTopic = (Topic) target;
						else {
							log.debug("Target is not a topic");
							return false;
						}

						if (targetTopic.equals(source)) {
							log.debug("Target is same as source");
							return false;
						}

						if (targetTopic.equals(source.getParent())) {
							log.debug("Target is alreay parent");
							return false;
						}

						if (source.isDescendant(targetTopic)) {
							log.debug("Source is descendant of target");
							return false;
						}
						log.debug("DND is acceptable");
						return true;
					}

					@Override
					public boolean performDrop(Object data) {
						Topic target = (Topic) getCurrentTarget();
						Topic topic = (Topic) ((IStructuredSelection) LocalSelectionTransfer
								.getTransfer().getSelection())
								.getFirstElement();
						log.debug("Dropping " + topic + " into " + target);

						if (target == null)
							target = eService.getRootTopic();
						eService.move(topic, target);

						return true;
					}

				});

		List<Topic> list;
		if (eService != null && eService.isActive()) {
			list = eService.getRootTopics();
			if (list != null && !list.isEmpty())
				treeViewer.setInput(list);
		}

		enableUI();

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
	void updateTree(@UIEventTopic(EEvents.TOPIC_ALL) Topic topic) {
		log.debug("Refreshing topics from event");
		treeViewer.setInput(eService.getRootTopics());
		treeViewer.setSelection(new StructuredSelection(topic));
		tree.showSelection();

		// treeViewer.refresh(topic.getParent());
		// treeViewer.refresh(topic.getParent());
	}

	@Inject
	@Optional
	void selectTopic(@UIEventTopic("E_SELECT_TOPIC") Topic topic) {
		treeViewer.setSelection(new StructuredSelection(topic));
	}

	@Focus
	void onFocus() {
		tree.setFocus();
	}

	private void enableUI() {
		enableUI(eService != null && eService.isActive());
	}

	private void enableUI(boolean enable) {
		tree.setEnabled(enable);
	}

	@Inject
	@Optional
	private void closeListener(@UIEventTopic("E_CLOSE") String s) {
		treeViewer.setInput(new ArrayList<Topic>());
		enableUI(false);

	}

	@Inject
	@Optional
	private void openListener(@UIEventTopic("E_OPEN") String s) {
		List<Topic> list = eService.getRootTopics();
		if (list != null && !list.isEmpty())
			treeViewer.setInput(list);
		enableUI();
	}

}

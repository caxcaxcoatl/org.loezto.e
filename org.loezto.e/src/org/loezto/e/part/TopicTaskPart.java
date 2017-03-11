package org.loezto.e.part;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

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
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.loezto.e.model.EService;
import org.loezto.e.model.Task;
import org.loezto.e.model.Topic;

class CompletedTasks extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof Task)
			if (((Task) element).getCompletionDate() == null)
				return true;
		return false;
	}

}

class SearchName extends ViewerFilter {

	String search = "";

	void setSearch(String search) {
		this.search = search.toUpperCase();
	}

	@Override
	public boolean isFilterProperty(Object element, String property) {
		return (property.equals("name"));
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Pattern p;
		try {
			p = Pattern.compile(search);
		} catch (PatternSyntaxException e) {
			return false;
		}

		if (element instanceof Task) {
			Task task = (Task) element;

			// Check element...
			if (p.matcher(task.getName().toUpperCase()).find())
				return true;

			// TODO decide whether this stays here

			// ...its ancestry...
			for (Task t : task.getPath())
				if (p.matcher(t.getName().toUpperCase()).find())
					return true;

			// ...and its descedency
			for (Task t : task.getDescendency())
				if (p.matcher(t.getName().toUpperCase()).find())
					return true;
		}
		return false;
	}
}

public class TopicTaskPart {

	@Inject
	@Optional
	EService eService;

	@Inject
	EMenuService menuService;

	@Inject
	IEclipseContext eContext;

	@Inject
	Shell shell;

	@SuppressWarnings("restriction")
	@Inject
	Logger log;

	private TreeViewer treeViewer;

	private Tree tree;
	private Text text;
	private Button btnShowCompleted;

	CompletedTasks completedTasks = new CompletedTasks();
	SearchName searchName = new SearchName();

	public TopicTaskPart() {
	}

	@PostConstruct
	void buildUI(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		composite.setLayout(new GridLayout(2, false));

		text = new Text(composite, SWT.BORDER | SWT.H_SCROLL | SWT.SEARCH
				| SWT.CANCEL);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				searchName.setSearch(text.getText());
				treeViewer.refresh();
			}
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnShowCompleted = new Button(composite, SWT.CHECK);
		btnShowCompleted.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnShowCompleted.getSelection()) {
					treeViewer.removeFilter(completedTasks);
				} else {
					treeViewer.addFilter(completedTasks);
				}
			}
		});
		btnShowCompleted.setAlignment(SWT.CENTER);
		btnShowCompleted.setText("Show completed");

		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tree.setLinesVisible(true);
		treeViewer.addFilter(searchName);
		treeViewer.addFilter(completedTasks);

		setupViewer();

		enableUI();

	}

	@Inject
	@Optional
	void updateList(@Named("E_CURRENT_TOPIC") Topic topic) {
		List<Task> list = eService.getRootTasks(topic);
		System.out.println(list);
		if (list != null)// && !list.isEmpty())
			treeViewer.setInput(list);
		else
			// Clear
			// treeViewer.
			;
	}

	@Inject
	@Optional
	void processTaskChanges(@UIEventTopic("TASK/*") Task task) {
		Topic currentTopic = (Topic) eContext.get("E_CURRENT_TOPIC");
		if (task.getTopic().equals(currentTopic))
			treeViewer.setInput(eService.getRootTasks(currentTopic));

	}

	private void setupViewer() {

		menuService
				.registerContextMenu(tree, "org.loezto.e.popupmenu.tasktree");

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IEclipseContext pContext = eContext.get(MPerspective.class)
						.getContext();
				IStructuredSelection sel = ((IStructuredSelection) event
						.getSelection());
				Object firstElement = sel.getFirstElement();
				System.out.println(sel.size());
				if (firstElement == null || sel.size() != 1)
					pContext.set("E_CURRENT_TASK", null);
				else if (firstElement instanceof Task) {
					pContext.set("E_CURRENT_TASK", (Task) firstElement);
				}
				if (firstElement != null)
					shell.setText("é - Task:  "
							+ ((Task) firstElement).getName());
				else
					shell.setText("é");

			}
		});

		treeViewer.setLabelProvider(new ILabelProvider() {

			@Override
			public String getText(Object element) {
				if (element == null)
					return "";
				if (element instanceof Task)
					return ((Task) element).getName();
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
				if (element instanceof Task)
					return !((Task) element).getChildren().isEmpty();
				return false;
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof Task)
					return ((Task) element).getParent();
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				List<?> list;

				if (inputElement == null)
					return new Object[] {};

				if (inputElement instanceof List<?>) {
					list = (List<?>) inputElement;
					if (!list.isEmpty() && list.get(0) instanceof Task)
						return list.toArray();
					else
						return new Object[] {};
				} else
					return new Object[] {};

			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof Task)
					return ((Task) parentElement).getChildren().toArray();
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

					// TODO Change this to canDrop (Object target, Object
					// source)

					@SuppressWarnings("restriction")
					private boolean canDrop(Object target) {
						Task targetTask;
						Task source;

						// http://www.eclipse.org/articles/Article-Workbench-DND/drag_drop.html
						//
						// In SWT, the transfer of data from the source to the
						// target is done lazily when the drop is initiated. So,
						// as the user is dragging, the destination has no way
						// of finding out what source object is being dragged
						// until the drop is performed
						//
						// TODO Take this out
						source = (Task) ((IStructuredSelection) treeViewer
								.getSelection()).getFirstElement();

						// New root element
						if (target == null) {
							log.debug("Target is null, indicating it's being moved to the root task.");
							if (source.getParent() == null) {
								log.debug("Target is already under root task");
								return false;
							} else {
								log.debug("DND is acceptable");
								return true;
							}
						}

						if (target instanceof Task)
							targetTask = (Task) target;
						else {
							log.debug("Target is not a task");
							return false;
						}

						if (targetTask.equals(source)) {
							log.debug("Target is same as source");
							return false;
						}

						if (targetTask.equals(source.getParent())) {
							log.debug("Target is alreay parent");
							return false;
						}

						if (source.isDescendant(targetTask)) {
							log.debug("Source is descendant of target");
							return false;
						}
						log.debug("DND is acceptable");
						return true;
					}

					@SuppressWarnings("restriction")
					@Override
					public boolean performDrop(Object data) {
						Task target = (Task) getCurrentTarget();

						// I deal with single drops, for now
						if (((IStructuredSelection) LocalSelectionTransfer
								.getTransfer().getSelection()).size() != 1)
							return false;

						Task task = (Task) ((IStructuredSelection) LocalSelectionTransfer
								.getTransfer().getSelection())
								.getFirstElement();
						log.debug("Dropping " + task + " into " + target);

						eService.move(task, target);

						return true;
					}

				});

	}

	void enableUI() {
		enableUI(eService != null && eService.isActive());
	}

	void enableUI(boolean enable) {
		tree.setEnabled(enable);
	}

	@Inject
	@Optional
	private void closeListener(@UIEventTopic("E_CLOSE") String s) {
		treeViewer.setInput(new ArrayList<Task>());
		enableUI(false);
	}

	@Inject
	@Optional
	private void openListener(@UIEventTopic("E_OPEN") String s) {
		treeViewer.setInput(new ArrayList<Task>());
		enableUI();
	}

	@Inject
	@Optional
	private void selectTask(@UIEventTopic("E_SELECT_TASK") Task task) {
		if (task != null) {
			treeViewer.setSelection(new StructuredSelection(task));
		}
	}

	@Focus
	void focus() {
		tree.setFocus();
	}

}

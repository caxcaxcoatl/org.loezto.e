
package org.loezto.e.part;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.loezto.e.dnd.TaskTransfer;
import org.loezto.e.model.EService;
import org.loezto.e.model.Task;
import org.loezto.e.model.Topic;

public class QueuePart {
	@Inject
	public QueuePart() {

	}

	@Inject
	EService eService;

	@Inject
	Logger log;

	@Inject
	IEventBroker broker;

	@Inject
	IEclipseContext eContext;

	private Table table;
	private TableViewer tableViewer;
	private WritableList<Task> wl;
	private Topic rootTopic;

	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		setupViewer();

	}

	private void setupViewer() {
		wl = new WritableList<>(new ArrayList<Task>(), Task.class);
		ViewerSupport.bind(tableViewer, wl, BeanProperties.values(new String[] { "name" }));

		Transfer[] transferTypes = new Transfer[] { TaskTransfer.getTransfer() };
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		tableViewer.addDragSupport(operations, transferTypes, new DragSourceListener() {

			@Override
			public void dragStart(DragSourceEvent event) {
				TaskTransfer tt = TaskTransfer.getTransfer();
				tt.setTask((Task) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement());
				tt.setSourceType(TaskTransfer.SourceType.Plan);
				tt.setSourceWidget(tableViewer.getTable());
			}

			@Override
			public void dragSetData(DragSourceEvent event) {

			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				// TODO Auto-generated method stub

			}
		});

		tableViewer.getTable().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.character == SWT.DEL) {
					rootTopic.getPlan().remove(tableViewer.getStructuredSelection().getFirstElement());
					eService.save(rootTopic);
					setPlan();
				}

			}
		});

		tableViewer.addDropSupport(operations, transferTypes, new ViewerDropAdapter(tableViewer) {

			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {

				TaskTransfer tt = TaskTransfer.getTransfer();

				if (tt.getSourceType() == TaskTransfer.SourceType.Structure)
					operation = DND.DROP_COPY;

				if (tt.getSourceWidget().equals(tableViewer.getTable()))
					operation = DND.DROP_MOVE;

				overrideOperation(operation);
				setFeedbackEnabled(true);

				return canDrop(target, operation, null);
			}

			private boolean canDrop(Object target, int operation, TransferData transferType) {

				TaskTransfer tt = TaskTransfer.getTransfer();

				if (rootTopic == null)
					return false;

				if (rootTopic.getPlan().contains(tt.getTask()))
					if (tt.getSourceWidget() == tableViewer.getTable() && operation == DND.DROP_MOVE)
						return true;
					else
						return false;
				if (tt.getSourceType() == TaskTransfer.SourceType.Plan)
					return true;
				else if (tt.getSourceType() == TaskTransfer.SourceType.Structure && operation == DND.DROP_COPY)
					return true;
				return false;
			}

			@Override
			public boolean performDrop(Object data) {
				TaskTransfer tt = TaskTransfer.getTransfer();

				Object target = getCurrentTarget();
				int currentLocation = getCurrentLocation();
				int op = getCurrentOperation();

				if (op == DND.DROP_COPY) {

					rootTopic.getPlan().add(calcPos(target, currentLocation), tt.getTask());
					eService.save(rootTopic);
					setPlan();
					return true;
				} else if (op == DND.DROP_MOVE) {
					System.out.println("Move me!");
					if (tt.getSourceWidget() == tableViewer.getTable()) {
						rootTopic.getPlan().remove(tt.getTask());
						rootTopic.getPlan().add(calcPos(target, currentLocation), tt.getTask());
						eService.save(rootTopic);
						setPlan();
						return true;
					}
				}

				return false;
			}

			private int calcPos(Object target, int currentLocation) {
				int pos = 0;

				if (target == null)
					return rootTopic.getPlan().size();
				else {

					if (currentLocation == LOCATION_AFTER)
						pos = rootTopic.getPlan().indexOf(target) + 1;
					else
						pos = rootTopic.getPlan().indexOf(target);
				}
				return pos;
			}
		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Task task;

				IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
				if (sel.size() != 1)
					return;

				task = (Task) sel.getFirstElement();

				broker.send("E_SELECT_TOPIC", task.getTopic());
				broker.post("E_SELECT_TASK", task);

			}
		});

		table.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				tableViewer.setSelection(null);
			}

			@Override
			public void focusGained(FocusEvent e) {
			}

		});

		setPlan();

	}

	protected void setPlan() {
		if (eService == null || !eService.isActive())
			return;

		rootTopic = eService.getRootTopic();
		log.debug("Set root topic for Queue part: " + rootTopic.getId() + " - " + rootTopic.getFullName());

		setPlan(rootTopic);

	}

	protected void setPlan(Topic topic) {

		this.rootTopic = topic;
		update();
	}

	protected void update() {
		if (wl == null)
			return;
		wl.clear();
		wl.addAll(rootTopic.getPlan());
	}

	@Focus
	public void onFocus() {
		tableViewer.getTable().setFocus();
	}

}
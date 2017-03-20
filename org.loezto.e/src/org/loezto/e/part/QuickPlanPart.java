
package org.loezto.e.part;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.loezto.e.dnd.TaskTransfer;
import org.loezto.e.model.CronoPlan;
import org.loezto.e.model.CronoType;
import org.loezto.e.model.EService;
import org.loezto.e.model.Task;

public class QuickPlanPart {
	private Table table;
	private TableViewer tableViewer;
	private DateTime dateTime;
	private Combo comboType;
	private Button btnPreviousDay;
	private Button btnToday;
	private Button btnNextDay;

	@Inject
	EService eService;
	private WritableList<Task> wl;
	private CronoPlan plan;

	@Inject
	protected IEventBroker broker;
	private Label lblInfo;

	@Inject
	public QuickPlanPart() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		GridLayout gl_parent = new GridLayout(1, false);
		gl_parent.verticalSpacing = 2;
		parent.setLayout(gl_parent);

		Composite composite_1 = new Composite(parent, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_1.setLayout(new GridLayout(2, false));

		comboType = new Combo(composite_1, SWT.READ_ONLY);
		comboType.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));

		comboType.setItems(
				(String[]) Arrays.stream(CronoType.values()).map(c -> c.toString()).toArray((n) -> new String[n]));
		comboType.setText(CronoType.Day.toString());

		comboType.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setPlan();
			}
		});

		dateTime = new DateTime(composite_1, SWT.BORDER + SWT.DROP_DOWN);
		dateTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		dateTime.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setPlan();
			}
		});

		Composite composite_2 = new Composite(parent, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_2.setLayout(new GridLayout(4, false));

		lblInfo = new Label(composite_2, SWT.NONE);
		lblInfo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblInfo.setText("Info");

		btnPreviousDay = new Button(composite_2, SWT.ARROW + SWT.LEFT);
		btnPreviousDay.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocalDate current = LocalDate.of(dateTime.getYear(), dateTime.getMonth() + 1, dateTime.getDay());
				LocalDate target = current.minusDays(1);
				dateTime.setDate(target.getYear(), target.getMonthValue() - 1, target.getDayOfMonth());
				setPlan();
			}
		});
		btnPreviousDay.setText("Previous day");

		btnToday = new Button(composite_2, SWT.NONE);
		btnToday.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocalDate target = LocalDate.now();
				dateTime.setDate(target.getYear(), target.getMonthValue() - 1, target.getDayOfMonth());
				setPlan();
			}
		});
		btnToday.setText("Today");

		btnNextDay = new Button(composite_2, SWT.ARROW + SWT.RIGHT);
		btnNextDay.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocalDate current = LocalDate.of(dateTime.getYear(), dateTime.getMonth() + 1, dateTime.getDay());
				LocalDate target = current.plusDays(1);
				dateTime.setDate(target.getYear(), target.getMonthValue() - 1, target.getDayOfMonth());
				setPlan();
			}
		});
		btnNextDay.setText("Next day");

		Composite composite_3 = new Composite(parent, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_3.setLayout(new GridLayout(1, false));

		tableViewer = new TableViewer(composite_3, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		enableUI(eService.isActive());
		setupViewer();
	}

	private void setupViewer() {

		wl = new WritableList<>(new ArrayList<Task>(), Task.class);
		ViewerSupport.bind(tableViewer, wl, BeanProperties.values(new String[] { "name" }));

		// tableViewer.setLabelProvider(new LabelProvider(){
		//
		// });

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
					plan.getTasks().remove(tableViewer.getStructuredSelection().getFirstElement());
					eService.save(plan);
					setPlan();
				}

			}
		});

		tableViewer.addDropSupport(operations, transferTypes, new ViewerDropAdapter(tableViewer) {

			// @Override
			// public void dragEnter(DropTargetEvent event) {
			// super.dragEnter(event);
			//
			//
			// System.out.println("Detail: " + event.detail);
			// }

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
				// return true;
			}

			// private boolean canDrop(Object target) {
			// return canDrop (target, getCurrentOperation(), null);
			// }

			private boolean canDrop(Object target, int operation, TransferData transferType) {

				TaskTransfer tt = TaskTransfer.getTransfer();

				if (plan == null)
					return false;

				if (plan.getTasks().contains(tt.getTask()))
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
					System.out.println("Copy me!");

					plan.getTasks().add(calcPos(target, currentLocation), tt.getTask());
					eService.save(plan);
					setPlan();
					return true;
				} else if (op == DND.DROP_MOVE) {
					System.out.println("Move me!");
					if (tt.getSourceWidget() == tableViewer.getTable()) {
						plan.getTasks().remove(tt.getTask());
						plan.getTasks().add(calcPos(target, currentLocation), tt.getTask());
						eService.save(plan);
						setPlan();
						return true;
					}
				}

				return false;
			}

			private int calcPos(Object target, int currentLocation) {
				int pos = 0;

				if (target == null)
					return plan.getTasks().size();
				else {

					if (currentLocation == LOCATION_AFTER)
						pos = plan.getTasks().indexOf(target) + 1;
					else
						pos = plan.getTasks().indexOf(target);
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

	}

	protected void setPlan() {
		if (eService == null || !eService.isActive())
			return;

		LocalDate date = LocalDate.of(dateTime.getYear(), dateTime.getMonth() + 1, dateTime.getDay());
		plan = eService.getPlan(CronoType.valueOf(comboType.getText()), date);
		if (plan == null) {
			plan = new CronoPlan(CronoPlan.CronoId.of(CronoType.valueOf(comboType.getText()), date));
		}

		lblInfo.setText(plan.getId().getDescription());
		lblInfo.getParent().layout();
		setPlan(plan);

	}

	protected void setPlan(CronoPlan plan) {

		this.plan = plan;
		update();
	}

	protected void update() {
		wl.clear();
		wl.addAll(plan.getTasks());
	}

	@Focus
	public void onFocus() {
		tableViewer.getTable().setFocus();

	}

	@Optional
	@Inject
	void processTaskChanges(@UIEventTopic("TASK/*") Task task) {
		setPlan();
	}

	@Inject
	@Optional
	private void closeListener(@UIEventTopic("E_CLOSE") String s) {
		wl.clear();
		enableUI(false);

	}

	private void enableUI(boolean b) {
		table.setEnabled(b);
		dateTime.setEnabled(b);
		comboType.setEnabled(b);
		btnPreviousDay.setEnabled(b);
		btnToday.setEnabled(b);
		btnNextDay.setEnabled(b);
	}

	@Inject
	@Optional
	private void openListener(@UIEventTopic("E_OPEN") String s) {
		setPlan();
		enableUI(true);
	}

}
 
package org.loezto.e.part;

import java.time.LocalDate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

public class QuickPlanPart {
	private Table table;
	@Inject
	public QuickPlanPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(4, false));
		
		final DateTime dateTime = new DateTime(parent, SWT.BORDER + SWT.DROP_DOWN);
		dateTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		Button btnPreviousDay = new Button(parent, SWT.ARROW + SWT.LEFT);
		btnPreviousDay.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocalDate current = LocalDate.of(dateTime.getYear(), dateTime.getMonth() + 1, dateTime.getDay());
				LocalDate target = current.minusDays(1);
				dateTime.setDate(target.getYear(), target.getMonthValue() - 1, target.getDayOfMonth());			}
		});
		btnPreviousDay.setText("Previous day");
		
		Button btnToday = new Button(parent, SWT.NONE);
		btnToday.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocalDate target = LocalDate.now();
				dateTime.setDate(target.getYear(), target.getMonthValue() -1, target.getDayOfMonth());
			}
		});
		btnToday.setText("Today");
		
		Button btnNextDay = new Button(parent, SWT.ARROW + SWT.RIGHT);
		btnNextDay.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocalDate current = LocalDate.of(dateTime.getYear(), dateTime.getMonth() + 1, dateTime.getDay());
				LocalDate target = current.plusDays(1);
				dateTime.setDate(target.getYear(), target.getMonthValue() - 1, target.getDayOfMonth());			}
		});
		btnNextDay.setText("Next day");
		
		TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
	}
	
	
	
	@Focus
	public void onFocus() {
		
	}
	
	
}
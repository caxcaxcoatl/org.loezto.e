package org.loezto.e.dialog;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class PeriodSelectionDialog extends Dialog {

	public PeriodSelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	Date begin;
	Date end;

	public void setBegin(Calendar begin) {
		begin.set(Calendar.HOUR_OF_DAY, 0);
		begin.set(Calendar.MINUTE, 0);
		begin.set(Calendar.SECOND, 0);
		begin.set(Calendar.MILLISECOND, 0);
		this.begin = begin.getTime();
	}

	public void setEnd(Calendar end) {
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 999);
		this.end = end.getTime();
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public Date getBegin() {
		return begin;
	}

	public Date getEnd() {
		Calendar end = Calendar.getInstance();
		end.setTime(this.end);
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);
		end.set(Calendar.MILLISECOND, 999);
		return end.getTime();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		CBanner banner = new CBanner(container, SWT.NONE);

		final DateTime cBegin = new DateTime(banner, SWT.BORDER | SWT.CALENDAR);
		banner.setLeft(cBegin);

		final DateTime cEnd = new DateTime(banner, SWT.BORDER | SWT.CALENDAR
				| SWT.LONG);
		banner.setRight(cEnd);

		final Label lblDays = new Label(banner, SWT.NONE);
		banner.setBottom(lblDays);
		lblDays.setText("Days");

		DataBindingContext ctx = new DataBindingContext();
		ctx.bindValue(WidgetProperties.selection().observe(cBegin),
				PojoProperties.value("begin").observe(this));
		ctx.bindValue(WidgetProperties.selection().observe(cEnd),
				PojoProperties.value("end").observe(this));

		cBegin.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (begin.after(end)) {
					cBegin.setYear(cEnd.getYear());
					cBegin.setMonth(cEnd.getMonth());
					cBegin.setDay(cEnd.getDay());
				}
				lblDays.setText(String.valueOf(((end.getTime() - begin
						.getTime()) / 1000 / 60 / 60 / 24) + 1) + " days");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		cEnd.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (begin.after(end)) {
					cBegin.setYear(cEnd.getYear());
					cBegin.setMonth(cEnd.getMonth());
					cBegin.setDay(cEnd.getDay());
				}
				lblDays.setText(String.valueOf(((end.getTime() - begin
						.getTime()) / 1000 / 60 / 60 / 24) + 1) + " days");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		return super.createDialogArea(parent);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

}

package org.loezto.e.model;

import static javax.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

@Entity
@Table(schema = "e")
public class CronoPlan extends ModelElement {

	@Embeddable
	public static class CronoId implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -396827659505318873L;
		CronoType cronoType;

		LocalDate start;
		LocalDate finish;

		public LocalDate getStart() {
			return start;
		}

		public CronoType getCronoType() {
			return cronoType;
		}

		public LocalDate getFinish() {
			return finish;
		}

		CronoId(CronoType cronoType, LocalDate start, LocalDate finish) {
			super();
			this.cronoType = cronoType;
			this.start = start;
			this.finish = finish;
		}

		public CronoId() {
			super();
		}

		public static CronoId of(CronoType type, LocalDate date) {
			LocalDate start;
			LocalDate finish;
			int finishMonth;

			switch (type) {
			case Day:
				return new CronoId(type, date, date);
			case Week:
				int i = date.get(WeekFields.SUNDAY_START.dayOfWeek()) - 1;
				start = date.minusDays(i);
				finish = start.plusDays(6);
				return new CronoId(type, start, finish);
			case Month:
				start = date.withDayOfMonth(1);
				finish = date.plusMonths(1).minusDays(1); // Deals with Feb
				return new CronoId(type, start, finish);
			case Quarter:
				int quarter = ((date.getMonthValue() - 1) / 3) + 1;
				start = date.withDayOfMonth(1).withMonth((quarter - 1) * 3 + 1);
				finishMonth = quarter * 3;
				finish = date.withMonth(finishMonth).withDayOfMonth(Month.of(finishMonth).maxLength());
				return new CronoId(type, start, finish);
			}
			return null;
		}

		public String getDescription() {
			switch (cronoType) {
			case Day:
				return start.format(DateTimeFormatter.ISO_DATE);
			case Week:
				return start.format(DateTimeFormatter.ofPattern("yyyy-'W'w"));
			case Month:
				return start.format(DateTimeFormatter.ofPattern("yyyy-MM"));
			case Quarter:
				return start.format(DateTimeFormatter.ofPattern("yyyy")) + "-Q" + (start.getMonthValue() / 3 + 1);
			default:
				return null;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cronoType == null) ? 0 : cronoType.hashCode());
			result = prime * result + ((finish == null) ? 0 : finish.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CronoId other = (CronoId) obj;
			if (cronoType != other.cronoType)
				return false;
			if (finish == null) {
				if (other.finish != null)
					return false;
			} else if (!finish.equals(other.finish))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			return true;
		}

	}

	@EmbeddedId
	CronoId id;

	@ManyToMany(fetch = EAGER)
	@OrderColumn(name = "place")
	@JoinTable(name = "cronoitem", schema = "e", inverseJoinColumns = @JoinColumn(name = "task", referencedColumnName = "id"))
	List<Task> tasks;

	public CronoId getId() {
		return id;
	}

	public CronoPlan(CronoId id) {
		super();
		this.id = id;
		this.tasks = new ArrayList<>();
	}

	public CronoPlan() {
		super();
	}

	public CronoPlan(CronoType type, LocalDate start, LocalDate finish) {
		this(type, start);
		if (!this.id.finish.equals(finish))
			throw new RuntimeException(
					"Given finish date (" + finish + ") different than calculated: " + this.id.finish);
	}

	public CronoPlan(CronoType type, LocalDate ref) {
		this(CronoId.of(type, ref));
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public String toString() {
		return id.getDescription();
	}

}

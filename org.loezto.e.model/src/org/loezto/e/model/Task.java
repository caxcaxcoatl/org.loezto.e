package org.loezto.e.model;

import static javax.persistence.GenerationType.SEQUENCE;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(schema = "e")
public class Task extends ModelElement {

	public Task() {
		super();
	}

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "task_seq")
	@SequenceGenerator(name = "task_seq", sequenceName = "task_seq", allocationSize = 2, initialValue = 1, schema = "e")
	long id;

	@ManyToOne
	@JoinColumn(name = "parent", referencedColumnName = "id")
	Task parent;

	@ManyToOne
	@JoinColumn(name = "topic", referencedColumnName = "id")
	Topic topic;

	String name;

	@Temporal(TIMESTAMP)
	@Column(insertable = false, updatable = false)
	Date creationDate;

	@Temporal(TIMESTAMP)
	Date completionDate;

	// TODO Timestamp?
	@Temporal(TIMESTAMP)
	Date dueDate;

	@OneToMany(mappedBy = "parent")
	List<Task> children;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Task getParent() {
		return parent;
	}

	public void setParent(Task parent) {
		if (parent != null)
			this.topic = parent.getTopic();
		this.parent = parent;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public List<Task> getChildren() {
		return children;
	}

	public void setChildren(List<Task> children) {
		this.children = children;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		Task other = (Task) obj;
		if (id != other.id)
			return false;
		return true;
	}

}

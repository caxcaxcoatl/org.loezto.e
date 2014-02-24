package org.loezto.e.model;

import static javax.persistence.GenerationType.SEQUENCE;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(schema = "e")
public class Topic extends ModelElement {

	@Id
	@GeneratedValue(strategy = SEQUENCE)
	@SequenceGenerator(name = "Topic_seq", sequenceName = "topic_seq")
	long id;
	public static final String FIELD_ID = "id";

	String Name;
	public static final String FIELD_NAME = "name";

	@ManyToOne
	@JoinColumn(name = "parent", referencedColumnName = "id")
	Topic parent;
	public static final String FIELD_PARENT = "parent";

	@Temporal(TIMESTAMP)
	Date creationDate;
	public static final String FIELD_CREATION_DATE = "creationDate";

	boolean root;
	public static final String FIELD_ROOT = "root";

	public Topic() {
		super();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public Topic getParent() {
		return parent;
	}

	public void setParent(Topic parent) {
		this.parent = parent;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
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
		Topic other = (Topic) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Topic [id=" + id + ", Name=" + Name + "]";
	}

}

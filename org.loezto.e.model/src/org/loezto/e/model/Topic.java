package org.loezto.e.model;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.GenerationType.SEQUENCE;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(schema = "e")
public class Topic extends ModelElement {

	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "Topic_seq")
	@SequenceGenerator(name = "Topic_seq", sequenceName = "topic_seq", schema = "e", initialValue = 2, allocationSize = 1)
	long id;
	public static final String FIELD_ID = "id";

	String name;
	public static final String FIELD_NAME = "name";
	public static final int FIELD_NAME_MAX = 255;

	@ManyToOne
	@JoinColumn(name = "parent", referencedColumnName = "id")
	Topic parent;
	public static final String FIELD_PARENT = "parent";

	@Temporal(TIMESTAMP)
	@Basic
	@Column(insertable = false, updatable = false)
	Date creationDate;
	public static final String FIELD_CREATION_DATE = "creationDate";

	boolean root;
	public static final String FIELD_ROOT = "root";

	@OneToMany(mappedBy = "parent", fetch = EAGER)
	@OrderBy("name")
	List<Topic> children;
	public static final String FIELD_CHILDREN = "children";

	// @OneToMany(mappedBy = "topic", fetch = EAGER)
	// @OrderColumn(name = "placement")
	// List<Task> tasks;

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
		return name;
	}

	public void setName(String name) {
		pcs.firePropertyChange(FIELD_NAME, this.name, this.name = name);
	}

	public Topic getParent() {
		if (id == 1)
			return null;
		return parent;
	}

	public void setParent(Topic parent) {
		pcs.firePropertyChange(FIELD_PARENT, this.parent, this.parent = parent);
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

	public List<Topic> getChildren() {
		return children;
	}

	public void setChildren(List<Topic> children) {
		pcs.firePropertyChange(FIELD_CHILDREN, this.children,
				this.children = children);
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
		return "Topic [id=" + id + ", Name=" + name + "]";
	}

	public void addChild(Topic topic) {
		children.add(topic);
	}

	public void removeChild(Topic topic) {
		children.remove(topic);
	}

	public boolean isDescendant(Topic t) {
		if (t.getParent() == null)
			return false;
		if (t.getParent().equals(this))
			return true;
		// TODO Change this for a constant
		if (t.getParent().getId() == 0)
			return false;
		return isDescendant(t.getParent());
	}

	public List<Topic> getPath() {
		if (id == 1)
			return new ArrayList<Topic>();
		else {
			List<Topic> parentPath = parent.getPath();
			parentPath.add(this);
			return parentPath;
		}
	}

	public String getFullName() {
		StringBuffer sb = new StringBuffer();
		for (Topic t : getPath()) {
			sb.append(t.getName());
			if (!t.equals(this))
				sb.append(" :: ");
		}

		return sb.toString();
	}

	public List<Topic> getSiblings() {
		List<Topic> siblings = new ArrayList<Topic>(getParent().getChildren());
		siblings.remove(this);

		return siblings;
	}

	public List<Topic> getDescendency() {
		ArrayList<Topic> list = new ArrayList<>();

		if (getChildren().size() == 0)
			return new ArrayList<Topic>();
		else
			for (Topic t : getChildren()) {
				list.addAll(t.getDescendency());
			}
		list.addAll(getChildren());
		return list;
	}

	// public List<Task> getTasks() {
	// return tasks;
	// }
	//
	// public void setTasks(List<Task> tasks) {
	// this.tasks = tasks;
	// }
}

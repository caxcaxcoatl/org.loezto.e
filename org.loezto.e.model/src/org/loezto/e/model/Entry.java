package org.loezto.e.model;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(schema = "e")
public class Entry extends ModelElement {

	@Id
	@SequenceGenerator(name = "entry_seq", sequenceName = "entry_seq", schema = "e", initialValue = 0, allocationSize = 5)
	@GeneratedValue(strategy = SEQUENCE, generator = "entry_seq")
	long id;

	@Column(length = 3)
	String type;

	@Temporal(TIMESTAMP)
	@Basic
	@Column(insertable = false, updatable = false)
	Date creationDate;

	@ManyToOne
	@JoinColumn(name = "topic", referencedColumnName = "id")
	Topic topic;

	String line;

	@Basic(fetch = LAZY)
	@Lob
	@Column(length = 1024)
	String text;

	public Entry() {
		super();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.line = text.trim();
		if (this.line.length() > 1024)
			this.line = this.line.substring(0, 1024);
		this.text = text;
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
		Entry other = (Entry) obj;
		if (id != other.id)
			return false;
		return true;
	}

}

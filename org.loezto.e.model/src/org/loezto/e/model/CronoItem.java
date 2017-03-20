package org.loezto.e.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

public class CronoItem extends ModelElement {
	
	@Embeddable
	public static class CronoItemId implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1337187953648132042L;

		@ManyToOne
		CronoPlan plan;
		
		@ManyToOne
		Task task;
		
		
	}

}

package org.loezto.e.test.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Test;
import org.loezto.e.model.CronoPlan;
import org.loezto.e.model.CronoPlan.CronoId;
import org.loezto.e.model.CronoType;

public class CronoPlanTest {

	@Test
	public void cronoIdOfDay() {
		CronoId id;
		
		id= CronoPlan.CronoId.of(CronoType.Day, LocalDate.now());
		
		assertEquals ("Start date of today's cronoId should be today", id.getStart(), LocalDate.now());
	}
	
	@Test
	public void cronoIdOfMonth() {
		CronoId id;
		
		id= CronoPlan.CronoId.of(CronoType.Month, LocalDate.now());
		
		assertEquals ("Start date of this month's cronoId should be the same month", LocalDate.now().getMonth(), id.getStart().getMonth());
		assertEquals ("Finish date of this month's cronoId should be the same month", LocalDate.now().getMonth(), id.getFinish().getMonth());
		assertEquals ("Start day of a month cronoId should be 1", 1, id.getStart().getDayOfMonth());
		assertEquals ("Finish day of this month's cronoId should be this month's max ", LocalDate.now().getMonth().maxLength(), id.getFinish().getDayOfMonth());
	}
	
	@Test
	public void cronoIdOfWeek ()
	{
		CronoId id;
		
		id = CronoPlan.CronoId.of(CronoType.Week, LocalDate.of(2017, 3, 20));
		
		assertEquals ("Start date of 2017-03-20 week should be 2017-03-19", LocalDate.of(2017, 3, 19), id.getStart());
	}
	
	@Test
	public void cronoIdOfQuarter ()
	{
		CronoId id;
		
		id = CronoPlan.CronoId.of(CronoType.Quarter, LocalDate.of(2017, 1, 1));
		assertEquals ("Quarter start of Jan 1st should be Jan 1st", LocalDate.of(2017, 1, 1), id.getStart());
		assertEquals ("Quarter finish of Jan 1st should be Mar 31", LocalDate.of(2017, 3, 31), id.getFinish());
		
		
		id = CronoPlan.CronoId.of(CronoType.Quarter, LocalDate.of(2017, 1, 2));
		assertEquals ("Quarter start of Jan 2nd should be Jan 1st", LocalDate.of(2017, 1, 1), id.getStart());
		assertEquals ("Quarter finish of Jan 2nd should be Mar 31", LocalDate.of(2017, 3, 31), id.getFinish());
		
		id = CronoPlan.CronoId.of(CronoType.Quarter, LocalDate.of(2017, 4, 2));
		assertEquals ("Quarter start of Apr 2nd should be Apr 1st", LocalDate.of(2017, 4, 1), id.getStart());
		assertEquals ("Quarter finish of Apr 2nd should be Jun 30", LocalDate.of(2017, 6, 30), id.getFinish());

		id = CronoPlan.CronoId.of(CronoType.Quarter, LocalDate.of(2017, 12, 15));
		assertEquals ("Quarter start of Dec 15 should be Oct 01", LocalDate.of(2017, 10, 1), id.getStart());
		assertEquals ("Quarter finish of Dec 15 should be Dec 31", LocalDate.of(2017, 12, 31), id.getFinish());

	}
	
	@Test
	public void cronoDescription ()
	{
		CronoPlan cronoPlan;
		
		cronoPlan = new CronoPlan (CronoType.Quarter, LocalDate.of (2017,3,1));
		assertEquals ("Quarter for Mar should be 1", "2017-Q1", cronoPlan.getId().getDescription());
		cronoPlan = new CronoPlan (CronoType.Quarter, LocalDate.of (2017,12,31));
		assertEquals ("Quarter for Dec should be 4", "2017-Q4", cronoPlan.getId().getDescription());
		cronoPlan = new CronoPlan (CronoType.Quarter, LocalDate.of (2017,1,1));
		assertEquals ("Quarter for Jan should be 1", "2017-Q1", cronoPlan.getId().getDescription());


	}


}

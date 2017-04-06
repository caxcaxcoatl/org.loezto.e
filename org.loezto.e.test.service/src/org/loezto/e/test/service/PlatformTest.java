package org.loezto.e.test.service;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Platform;
import org.junit.Test;

public class PlatformTest {

	@Test
	public void testPlatform() {
		assertTrue(Platform.isRunning());
	}

}

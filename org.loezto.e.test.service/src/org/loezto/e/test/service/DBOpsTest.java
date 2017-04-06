package org.loezto.e.test.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loezto.e.model.EDatabaseException;
import org.loezto.e.model.EService;
import org.loezto.e.service.EServiceImpl;

@RunWith(SWTBotJunit4ClassRunner.class)
public class DBOpsTest {

	private static SWTBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTBot();
		// bot.viewByTitle("Welcome").close();
	}

	@Inject
	IEclipseContext context;

	@Test
	public void dbCreation() throws IOException {

		Path tmpDir = Files.createTempDirectory("dbCreationTest");
		tmpDir.toFile().deleteOnExit();

		// IEventBroker broker = new EventBrokerMock();
		//
		// IEclipseContext context = EclipseContextFactory.create();
		// Logger log = new LoggerMock();
		//
		// context.set(IEventBroker.class, broker);
		// context.set(Logger.class, log);

		Properties props = new Properties();
		props.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
		props.setProperty("javax.persistence.jdbc.url", "jdbc:derby:" + tmpDir);

		context.set(EService.ESERVICE_PROPERTIES, props);

		System.out.println(context);

		EService eService = ContextInjectionFactory.make(EServiceImpl.class, context); // ,
																						// context);

		try {
			eService.activate();
		} catch (EDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bot.sleep(100000);

	}

}

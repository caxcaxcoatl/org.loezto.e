package org.loezto.e.service;

import java.lang.reflect.Field;

public class BeanReporter {

	public static String report(Object bean, String[] props) {

		// Throw exception, instead?
		if (bean == null)
			return null;

		StringBuffer sb = new StringBuffer();

		for (String prop : props) {
			try {
				Field f = bean.getClass().getDeclaredField(prop);

				f.setAccessible(true);

				Object oldValue = f.get(bean);

				sb.append(prop);
				sb.append(": '");
				sb.append(oldValue);
				sb.append("'\n");
			} catch (NoSuchFieldException | SecurityException
					| IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

		}

		return sb.toString();
	}

}

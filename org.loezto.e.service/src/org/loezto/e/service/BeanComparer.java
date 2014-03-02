package org.loezto.e.service;

import java.lang.reflect.Field;

public class BeanComparer {

	public static String compare(Object oldBean, Object newBean, String[] props) {

		// Throw exception, instead?
		if (oldBean == null || newBean == null
				|| !oldBean.getClass().equals(newBean.getClass()))
			return "No report";

		StringBuffer sb = new StringBuffer();

		for (String prop : props) {
			try {
				Field f = oldBean.getClass().getDeclaredField(prop);

				f.setAccessible(true);

				Object oldValue = f.get(oldBean);
				Object newValue = f.get(newBean);

				sb.append(prop);
				sb.append("\n");
				if (oldValue != null) {
					if (!oldValue.equals(newValue)) {
						sb.append("\tchanged from '");
						sb.append(oldValue);
						sb.append("' to '");
						sb.append(newValue);
						sb.append("'\n");
					} else {
						sb.append("\tunchanged: '");
						sb.append(newValue);
						sb.append("'\n");
					}
				} else if (newValue != null) {
					sb.append("\tchanged from '");
					sb.append(oldValue);
					sb.append("' to '");
					sb.append(newValue);
					sb.append("'\n");
				} else {
					sb.append("\tunchanged: '");
					sb.append(newValue);
					sb.append("'\n");
				}
				sb.append("\n");

			} catch (NoSuchFieldException | SecurityException
					| IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

		}

		// BeanInfo oldBeanInfo;
		// BeanInfo newBeanInfo;
		//
		// try {
		// oldBeanInfo = Introspector.getBeanInfo(oldBean.getClass());
		// newBeanInfo = Introspector.getBeanInfo(newBean.getClass());
		// } catch (IntrospectionException e) {
		// e.printStackTrace();
		// return list;
		// }
		//
		// List<PropertyDescriptor> propList = Arrays.asList(oldBeanInfo
		// .getPropertyDescriptors());
		// List<PropertyDescriptor> newPropList = Arrays.asList(newBeanInfo
		// .getPropertyDescriptors());
		//
		// Map<String, PropertyDescriptor> newPropMap = new HashMap<>();
		//
		// for (PropertyDescriptor p : newPropList) {
		// newPropMap.put(p.getName(), p);
		// }
		//
		// System.out.println("-------------");
		// for (PropertyDescriptor p : propList) {
		// String propName = p.getName();
		// Object oldValue = p.getValue(propName);
		// Object newValue = newPropMap.get(propName).getValue(propName);
		// System.out.println(propName);
		// System.out.println(newBeanInfo.getBeanDescriptor().getValue(
		// propName));
		// System.out.println(oldValue);
		// System.out.println(newValue);
		//
		// Field f;
		// try {
		// f = oldBean.getClass().getDeclaredField(propName);
		// f.setAccessible(true);
		// Field g = oldBean.getClass().getDeclaredField(propName);
		// System.out.println(g.get(newBean));
		// } catch (NoSuchFieldException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (SecurityException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// if (oldValue != null) {
		// if (!oldValue.equals(newValue)) {
		// System.out.println(oldValue);
		// System.out.println(newValue);
		// }
		// } else if (newValue != null) {
		// System.out.println(oldValue);
		// System.out.println(newValue);
		// }
		//
		// }

		return sb.toString();
	}

	public static String report(Object oldBean, Object newBean) {
		return "";
	}

}

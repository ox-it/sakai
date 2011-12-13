package uk.ac.ox.oucs.oxam.readers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.management.RuntimeErrorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

public class SheetExporter {
	
	private final static Log LOG = LogFactory.getLog(SheetExporter.class);
	
	public <T> void writeSheet(OutputStream out, Format format, Iterable<T> iterable) throws IOException {
		
		// Make the map sorted by the order.
		Map<Order, Column> columnToField = new TreeMap<Order, Column>();
		Iterator<T> objects = iterable.iterator();
		if (!objects.hasNext()) {
			// Should write an empty sheet, although calling code should really not call us.
			return ;
		}
		
		// Generics mean we need an actual instance.
		T object = objects.next();
		Class<?> clazz = object.getClass();
		SheetWriter writer = new SheetWriterCSV(out);
		
		walkClass(columnToField, object, null, null);
		// Now we should have all the headers, so lets write them out.
		for(Column column: columnToField.values()) {
			writer.writeColumn(column.name);
		}
		writer.nextRow();
		
		// And now the actual data, do loop as we only have an iterator 
		// and have already looked at the first object.
		do {
			
			for (Column finder : columnToField.values()) {
				try {
					Object value = finder.valueFinder.get(object);
					// If this is 
					writer.writeColumn((value == null)?"":value.toString());
				} catch (Exception e) {
					LOG.error(e);
				}
			}
			writer.nextRow();
			
		} while (objects.hasNext() && (object = objects.next()) != null);
		writer.flush();
	}


	/**
	 * This is just ugly as we walk an example, rather than the actual class hierarchy.
	 *
	 */
	protected void walkClass(Map<Order, Column> columnToField, Object example, Order parent, ValueFinder path) {
		// TODO Find a better way when we are using generics.
		Class<?> clazz = example.getClass();
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			// Look at getters and setters.
			for(PropertyDescriptor pd : propertyDescriptors) {
				
				Method getter = pd.getReadMethod();
				if (getter != null) {
					if (getter.isAnnotationPresent(Include.class)) {
						// This is horrible, but we need todo it as with generics we can't tell the type of the getter
						// at runtime.
						Object getterExample = getter.invoke(example, null);
						walkClass(columnToField, getterExample, getOrder(parent, getter), new MethodValueFinder(getter, path));
					}
					if (getter.isAnnotationPresent(ColumnMapping.class)){
						ColumnMapping mapping = getter.getAnnotation(ColumnMapping.class);
						// Treat everything as a string.
						columnToField.put(getOrder(parent, getter), new Column(mapping.value(), new MethodValueFinder(getter, path)));
					}
				}
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		
		for (Field field : clazz.getDeclaredFields()) {
			Order order = getOrder(parent, field);
			
			if (field.isAnnotationPresent(Include.class)) {
				// Recurse.
				walkClass(columnToField, field.getType(), order, new FieldValueFinder(field, path));
			}
			if (field.isAnnotationPresent(ColumnMapping.class)){
				ColumnMapping mapping = field.getAnnotation(ColumnMapping.class);
				// Treat everything as a string.
				columnToField.put(order, new Column(mapping.value(), new FieldValueFinder(field, path)));
			}
		}
	}

	protected Order getOrder(Order parent, AccessibleObject object) {
		Order order;
		if (object.isAnnotationPresent(Ordered.class)) {
			int offset = object.getAnnotation(Ordered.class).value();
			order = new Order(parent, offset);
		} else {
			order = new Order(parent);
		}
		return order;
	}
	
	
	/**
	 * Small class to hold the value finder and the column name as one.
	 * @author buckett
	 *
	 */
	public class Column {
		ValueFinder valueFinder;
		String name;
		
		Column(String name, ValueFinder valueFinder) {
			this.name = name;
			this.valueFinder = valueFinder;
		}
	}
	
	public interface ValueFinder {
		public Object get(Object obj) throws Exception;
	}
	
	public class MethodValueFinder implements ValueFinder {
		Method method;
		ValueFinder parent;
		
		MethodValueFinder(Method method) {
			this.method = method;
		}
		
		MethodValueFinder (Method method, ValueFinder parent) {
			this.method = method;
			this.parent = parent;
		}
		
		public Object get(Object obj) throws Exception {
			return (parent == null) ? method.invoke(obj, null) : method.invoke(parent.get(obj),null);
		}

	}
	
	/**
	 * This allows chains of values to be retrieved, which is needed for @Include support.
	 * @author buckett
	 *
	 */
	public class FieldValueFinder implements ValueFinder{
		Field field;
		ValueFinder parent;
		
		FieldValueFinder(Field field) {
			this.field = field;
		}
		
		FieldValueFinder (Field field, ValueFinder parent) {
			this.field = field;
			this.parent = parent;
		}
		
		public Object get(Object obj) throws Exception {
			return (parent == null) ? field.get(obj) : field.get(parent.get(obj));
		}
	}
	
	/**
	 * Nested ordering.
	 * Doesn't override equals and hashcode, so that if you have two of the same
	 * order position (1.2, 1.2) then they can both exist in a set.
	 * @author buckett
	 *
	 */
	public static class Order implements Comparable<Order>{
		int[] values;
		
		/**
		 * Add an unordered item.
		 * @param other
		 */
		public Order(Order other) {
			this(other, Integer.MAX_VALUE);
		}
		
		public Order(int value) {
			this(null, value);
		}
		
		public Order(Order other, int value) {
			if (other == null) {
				this.values = new int[]{value};
			} else {
				values = Arrays.copyOf(other.values, other.values.length+1);
				values[values.length-1] = value;
			}
		}
		
		public Order(String source) {
			String[] parts = source.split("\\.");
			values = new int[parts.length];
			for (int i = 0; i < parts.length && i < values.length; i++) {
				values[i] = Integer.parseInt(parts[i]);
			}
		}
		
		public int compareTo(Order that) {
			// Do this first as it's fast
			for(int i = 0; i < this.values.length && i < that.values.length; i++) {
				int compareValue = this.values[i] - that.values[i];
				if (compareValue != 0) {
					return compareValue;
				}
			}
			int compareLength = this.values.length - that.values.length;
			if (compareLength != 0) {
				return compareLength;
			}
			return hashCode() - that.hashCode();
		}

		@Override
		public String toString() {
			StringBuilder out = new StringBuilder();
			boolean needJoin = false;
			for (int value: values) {
				if (needJoin) {
					out.append(".");
				} else {
					needJoin = true;
				}
				out.append(value);
			}
			return out.toString();
		}
	}
}

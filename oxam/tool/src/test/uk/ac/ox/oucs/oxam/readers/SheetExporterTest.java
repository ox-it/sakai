package uk.ac.ox.oucs.oxam.readers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SheetExporterTest {

	@Test
	public void test() throws IOException {
		List<ToWrite> list = Arrays.asList(new ToWrite[]{
				new ToWrite("Hello", new Nested("All")),
				new ToWrite("World", new Nested("!"))
		});
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SheetExporter exporter = new SheetExporter();
		exporter.writeSheet(out, null, list);
		System.out.print(new String(out.toByteArray()));
	}
	
	@Test
	public void testWithGenerics() throws IOException {
		List<WithGenerics<Nested>> list = new ArrayList<WithGenerics<Nested>>();
		list.add(new WithGenerics<Nested>("one", new Nested("1")));
		list.add(new WithGenerics<Nested>("two", new Nested("2")));
		list.add(new WithGenerics<Nested>("three", new Nested("3")));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SheetExporter exporter = new SheetExporter();
		exporter.writeSheet(out, null, list);
		System.out.print(new String(out.toByteArray()));
	}

	class ToWrite {
		
		@ColumnMapping("header")
		@Ordered(2)
		String value;
		
		@Include
		@Ordered(1)
		Nested other;
		

		ToWrite(String value, Nested other) {
			this.value = value;
			this.other = other;
		}
		
	}
	
	class Nested {
		@ColumnMapping("nested header")
		@Ordered(1)
		String value;
		
		Nested(String value) {
			this.value = value;
		}
	}
	
	class WithGenerics<T> {
		
		String name;
		T object;
		
		WithGenerics(String name, T object) {
			this.name = name;
			this.object = object;
		}
		
		@ColumnMapping("name")
		public String getName() {
			return this.name;
		}
		
		@Include
		public T getObject() {
			return this.object;
		}
	}

}

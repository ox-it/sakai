package org.sakaiproject.exporter.util;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipOutputStream;
import java.io.IOException;

public class ZipPrintStream extends ZipOutputStream {

	public ZipPrintStream(OutputStream out){
		super(out, Charset.forName("UTF-8"));
	}

	public void print(String s) {
		try {
			write(s.getBytes());
		} catch (IOException e) {
		}
	}

	public void println(String s) {
		try {
			write(s.getBytes());
			write("\n".getBytes());
		} catch (IOException e) {
		}
	}
}
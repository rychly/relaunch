/*
 * Copyright (C) 2011 Andrew Mochalov <avmae@mail.ru>
 * 
 *  This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
 */
package ebook.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ebook.EBookFormat;

/**
 * InstantParser - very fast, instant handler of the information contained 
 * in the files of e-books
 */
public class InstantParser extends ebook.parser.Parser {
	protected void parseFile() {
		if (SOP.fb2File.matcher(this.eBook.fileName).matches()) {
			this.eBook.format = EBookFormat.FB2;
			this.parseFb2();
		}
		if (SOP.fb2zipFile.matcher(this.eBook.fileName).matches()) {
			this.eBook.format = EBookFormat.FB2;
			this.parseFb2Zip();
		}
		if (SOP.epubFile.matcher(this.eBook.fileName).matches()) {
			this.eBook.format = EBookFormat.EPUB;
			this.parseEpub();
		} else {
			this.eBook.format = EBookFormat.UNSUPPORTED;
		}
	}

	private void parseFb2() {
		try {
			InputStream inputStream = new FileInputStream(this.eBook.fileName);
			Fb2InstantParser parser = new Fb2InstantParser(this.eBook,
					inputStream);
			parser.parse();
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseFb2Zip() {
		try {
			ZipFile zipFile = new ZipFile(this.eBook.fileName);
			ZipEntry entry = zipFile.entries().nextElement();
			InputStream inputStream = zipFile.getInputStream(entry);
			Fb2InstantParser parser = new Fb2InstantParser(this.eBook,
					inputStream);
			parser.parse();
			inputStream.close();
			zipFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseEpub() {
		try {
			EpubInstantParser parser = new EpubInstantParser(this.eBook);
			parser.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

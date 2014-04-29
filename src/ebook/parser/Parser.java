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

import ebook.EBook;

/**
 * Parser - abstract class from which the e-books handlers are created 
 */
abstract public class Parser {
	protected EBook eBook;

	/**
	 * Handles the e-book extracts contained therein meta-information
	 * @param fileName - the name of the file to be processed
	 * @return - instance of the class EBook with the fields filled with
	 * e-book meta-information
	 */
	public EBook parse(String fileName) {
		return this.parse(fileName, false);
	}
	
	/**
	 * @param fileName
	 * @param extractCover
	 * @return
	 */
	public EBook parse(String fileName, boolean extractCover) {
		this.eBook = new EBook();
		this.eBook.fileName = fileName;
		this.eBook.doExtractCover = extractCover;
		this.eBook.isOk = false;
		this.parseFile();
		return this.eBook;
	}
	
	
	abstract protected void parseFile();

	/**
	 * Returns instance of the class EBook with the fields filled with
	 * e-book meta-information 
	 * @return - instance of the class EBook 
	 */
	public EBook getEBoook() {
		return this.eBook;
	}
}

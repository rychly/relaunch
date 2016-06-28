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

import java.util.regex.Pattern;

/**
 * Store of Patterns
 */
class SOP {
	protected static Pattern fb2File;
	protected static Pattern fb2zipFile;
	protected static Pattern epubFile;
	protected static Pattern opfFile;
	protected static Pattern xmlEncoding;
	protected static Pattern fb2FirstName;
	protected static Pattern fb2MiddleName;
	protected static Pattern fb2LastName;
	protected static Pattern fb2Author;
	protected static Pattern fb2Title;
	protected static Pattern fb2genre;
	protected static Pattern fb2Language;
	protected static Pattern fb2Sequence;
	protected static Pattern fb2SequenceName;
	protected static Pattern fb2SequenceNumber;
	protected static Pattern fb2Annotation;
	protected static Pattern fb2CoverName;
	protected static Pattern epubDescription;
	protected static Pattern epubTitle;
	protected static Pattern epubAuthor;
	protected static Pattern epubLanguage;
	protected static Pattern epubGenre;
	protected static Pattern epubCover;
    protected static Pattern epubMeta;
    protected static Pattern epubSequenceName;
    protected static Pattern epubSequenceNumber;
    protected static Pattern epubSequenceContent;

	static {
		fb2File = Pattern.compile("(?i).*fb2$");
		fb2zipFile = Pattern.compile("(?i).*fb2\\.zip$");
		epubFile = Pattern.compile("(?i).*epub$");
		opfFile = Pattern.compile("(?i).*opf$");
		xmlEncoding = Pattern.compile("(?i).*encoding=[\"'](.*?)[\"'].*");
		fb2FirstName = Pattern.compile("(?s)<first-name>(.*)</first-name>");
		fb2MiddleName = Pattern
				.compile("(?s)<middle-name>(.*)</middle-name>");
		fb2LastName = Pattern.compile("(?s)<last-name>(.*)</last-name>");
		fb2Author = Pattern.compile("(?s)<author>(.*?)</author>");
		fb2Title = Pattern.compile("(?s)<book-title>(.*?)</book-title>");
		fb2genre = Pattern.compile("(?s)<genre>(.*?)</genre>");
		fb2Language = Pattern.compile("(?s)<lang>(.*?)</lang>");
		fb2Sequence = Pattern.compile("(?s)<sequence(.*)>");
		fb2SequenceName = Pattern.compile("name=\"(.*?)\"");
		fb2SequenceNumber = Pattern.compile("number=\"(.*?)\"");

		fb2Annotation = Pattern
				.compile("(?s)<annotation>(.*?)</annotation>");
		fb2CoverName = Pattern
				.compile("(?s)<coverpage>.*href=\"#(.*?)\".*</coverpage>");
		epubDescription = Pattern
				.compile("(?s)<dc:description>(.*?)</dc:description>");
		epubTitle = Pattern
				.compile("(?s)<dc:title.*?>(.*?)</dc:title>");
		epubAuthor = Pattern
				.compile("(?s)<dc:creator.*?>(.*?)</dc:creator>");
		epubLanguage = Pattern
				.compile("(?s)<dc:language.*?>(.*?)</dc:language>");
		epubGenre = Pattern
				.compile("(?s)<dc:subject.*?>(.*?)</dc:subject>");
		epubCover = Pattern
				.compile("(?s)<embeddedcover>(.*?)</embeddedcover>");
        epubMeta = Pattern
                .compile("(?s)<meta(.*?)/>");
        epubSequenceName = Pattern
                .compile("name=\"calibre:series\"");
        epubSequenceNumber = Pattern
                .compile("name=\"calibre:series_index\"");
        epubSequenceContent = Pattern
                .compile("content=\"(.*?)\"");
	}
}

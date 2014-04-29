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
package ebook;

/**
 * Class containing the data of the person, the author or translator
 */
public class Person {
	/**
	 * Last name of the person
	 */
	public String lastName;
	/**
	 * First name of the person
	 */
	public String firstName;
	/**
	 * Middle name of the person
	 */
	public String middleName;
	/**
	 * Nick name of the person
	 */
	public String nickName;

	/**
	 * The class constructor, fills the fields with null values.
	 */
	public Person() {
		lastName = firstName = middleName = nickName= null;
	}

	/**
	 * The class constructor, fills the fields from parsing person
	 * full name
	 * @param name - The person full name
	 */
	public Person(String name) {
		name.trim();
		String[] nameParts = name.split("[\\s]+");
		int count = nameParts.length;
		if (count > 2) {
			this.firstName = nameParts[0];
			this.lastName = nameParts[count - 1];
			this.middleName = "";
			for (int index = 1; index < count - 1; index++) {
				this.middleName += nameParts[index];
			}
		} else if (count == 2) {
			this.firstName = nameParts[0];
			this.lastName = nameParts[count - 1];
		} else if (count == 1) {
			this.lastName = nameParts[0];
		}
	}
}

/*
 * $Source: /usr/cvsroot/melati/poem/src/main/java/org/melati/poem/csv/CSVFileParser.java,v $
 * $Revision: 1.8 $
 *
 * Copyright (C) 2001 Myles Chippendale
 * 
 * Part of Melati (http://melati.org), a framework for the rapid
 * development of clean, maintainable web applications.
 *
 * Melati is free software; Permission is granted to copy, distribute
 * and/or modify this software under the terms either:
 *
 * a) the GNU General Public License as published by the Free Software
 *    Foundation; either version 2 of the License, or (at your option)
 *    any later version,
 *
 *    or
 *
 * b) any version of the Melati Software License, as published
 *    at http://melati.org
 *
 * You should have received a copy of the GNU General Public License and
 * the Melati Software License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
 * GNU General Public License and visit http://melati.org to obtain the
 * Melati Software License.
 *
 * Feel free to contact the Developers of Melati (http://melati.org),
 * if you would like to work out a different arrangement than the options
 * outlined here.  It is our intention to allow Melati to be used by as
 * wide an audience as possible.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Contact details for copyright holder:
 *
 *     Myles Chippendale <mylesc At paneris.org>
 */
package net.pizey.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * A utility for tokenising a file made up of comma-separated variables. We
 * allow for fields having returns in them.
 * 
 * <PRE>
 *   foo, bar om,,"baz, ,oof",xyz,   ->
 *     "foo", " bar om", "", "baz, , oof", "xyz", ""
 * 
 *   foo, "bar
 *   bar
 *   bar", baz ->
 *   "foo", "bar\u0015bar\u0015bar", "baz"
 * </PRE>
 * 
 * Each record (which is usually a line, unless some fields have a line break in
 * them) is accessed one at a time by calling <code>nextRecord()</code>. Within
 * each record <code>recordHasMoreFields()</code> and <code>nextField()</code>
 * can be used like an Enumeration to iterate through the fields.
 * 
 * @author mylesc, based heavily on original CSVStringEnumeration by williamc
 */

public class CsvFileParser {

  private BufferedReader reader = null;

  int lineNo = 0; // The first line will be line '1'
  private String line = "";
  private boolean emptyLastField = false;
  int position = 0;

  /**
   * Constructor.
   * 
   * @param reader
   *          file reader
   */
  public CsvFileParser(BufferedReader reader) {
    this.reader = reader;
  }

  /**
   * @return whether there is another line
   */
  public boolean hasNextRecord() {
    // Not confident about this
    // but we need to return false if we have reached end and closed the file
    try {
      if (!reader.ready())
        return false;

      line = reader.readLine();
      // This should be false anyway if we're called from nextToken()
      emptyLastField = false;
      position = 0;
      if (line == null) {
        reader.close();
        return false;
      }

      lineNo++;
      return true;
    } catch (IOException e) {
      throw new CsvParseException("Unexpected IO exception", e);
    }
  }

  /**
   * Return the line number.
   * 
   * @return the current lineNo
   */
  public int getLineNo() {
    return lineNo;
  }

  /**
   * Are there any more tokens to come?
   * 
   * @return whether there are more fields
   */
  public boolean recordHasMoreFields() {
    System.err.println("recordHasMoreFields:" + emptyLastField + " position "
        + position + " line.length() " + line.length() + " so "
        + (emptyLastField || position < line.length()));
    return emptyLastField || position < line.length();
  }

  /**
   * @return the next token as a String
   */
  public String nextField() {
    return nextToken(false);
  }

  /**
   * @return the next token as a String
   */
  private String nextToken(boolean inUnclosedQuotes) {

    if (emptyLastField) {
      emptyLastField = false;
      System.err.println("Returning empty");
      return "";
    }

    if (position >= line.length())
      throw new NoSuchElementException("Position " + position + " line length "
          + line.length());

    if (inUnclosedQuotes || (line.charAt(position) == '"' && (++position > 0))) {

      // we need to allow for quotes inside quoted fields, so now test for ",
      int q = line.indexOf("\",", position);
      // if it is not there, we are (hopefully) at the end of a line
      if (q == -1 && (line.indexOf('"', position) == line.length() - 1))
        q = line.length() - 1;

      // If we don't find the end quote try reading in more lines
      // since fields can have \n in them
      if (q == -1) {
        String sofar = line.substring(position, line.length());
        if (!hasNextRecord())
          throw new IllegalArgumentException("Unclosed quotes on line "
              + lineNo);
        return sofar + "\n" + nextToken(true);
      }

      String it = line.substring(position, q);

      ++q;
      position = q + 1;
      if (q < line.length()) {
        if (line.charAt(q) != ',') {
          position = line.length();
          throw new IllegalArgumentException("No comma after quotes on line "
              + lineNo);
        } else if (q == line.length() - 1)
          emptyLastField = true;
      }
      return it;
    } else {
      int q = line.indexOf(',', position);
      if (q == -1) {
        String it = line.substring(position);
        position = line.length();
        return it;
      } else {
        String it = line.substring(position, q);
        if (q == line.length() - 1)
          emptyLastField = true;
        position = q + 1;
        return it;
      }
    }
  }

}
package net.pizey.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.TestCase;

/**
 * @author timp
 * 
 */
public class CsvTableTest extends TestCase {

  public void testRemoveExtension() {
    assertEquals("sheet1", CsvTable.removeExtension("sheet1.csv"));
    assertEquals("sheet1", CsvTable.removeExtension("sheet1"));
  }

  public void testConstruct() throws Exception {
    String sheet1Name = "src/test/resources/sheet1.csv";

    CsvTable sheet1 = new CsvTable(sheet1Name);

    String input = "Id,field1,\n1,f1,\n2,2f1,\n";
    assertEquals(input, sheet1.toString());
    assertEquals("sheet1", sheet1.getName());
    String outputFileName = "target/sheet1out.csv";
    sheet1.outputToFile(outputFileName);
    BufferedReader reader = new BufferedReader(new FileReader(outputFileName));
    StringBuffer outputBuffer = new StringBuffer();
    String line = null;
    while ((line = reader.readLine()) != null) {
      outputBuffer.append(line);
      if (!line.equals(""))
        outputBuffer.append("\n");
    }
    assertEquals(input, outputBuffer.toString());
    reader.close();
  }

  public void testConstructFromInstance() {
    String sheet1Name = "src/test/resources/sheet1.csv";

    CsvTable sheet1 = new CsvTable(sheet1Name);
    CsvTable sheet2 = new CsvTable(sheet1);
    assertTrue(sheet1.equals(sheet2));
    assertEquals(sheet1, sheet2);

  }

  public void testConstructWithPrimeKey() {
    String sheet1Name = "src/test/resources/sheet1.csv";

    CsvTable sheet1 = new CsvTable(sheet1Name, "Id");
    CsvTable sheet2 = new CsvTable(sheet1);
    assertTrue(sheet1.equals(sheet2));
    assertEquals(sheet1, sheet2);

  }

  public void testMakeFirst() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv");

    String input = "Id,field1,field2,\n1,f1,f2,\n2,2f1,2f2,\n";
    assertEquals(input, sheet.toString());
    assertEquals("Id", sheet.getColumn("Id").getName());
    assertEquals("Id(PK)", sheet.getColumn("Id").toString());
    assertEquals("field1", sheet.getColumn("field1").toString());

    sheet.makeFirstAndPrimary("field1");
    assertEquals("Id", sheet.getColumn("Id").toString());
    assertEquals("field1(PK)", sheet.getColumn("field1").toString());
    assertEquals("field1,Id,field2,\nf1,1,f2,\n2f1,2,2f2,\n", sheet.toString());

  }

  public void testFailToUnifyMutated() {
    CsvTable sheet1 = new CsvTable("src/test/resources/sheet2.csv",
        UnificationOptions.LOG);
    CsvTable sheet2 = new CsvTable(
        "src/test/resources/mutatedCopyOfSheet2.csv", UnificationOptions.LOG);
    try {
      sheet1.unify(sheet2, false).toString();
      fail("Should have bombed");
    } catch (CsvException e) {
      assertEquals(
          "Table mutatedCopyOfSheet2 line 3 value found for field1 but not equal to current value : '2f1' != '2f1-mutated'",
          e.getMessage());
    }
  }

  public void testUnifyTHROW() {
    String sheet1Name = "src/test/resources/sheet1.csv";
    String sheet2Name = "src/test/resources/sheet2.csv";
    String sheet3Name = "src/test/resources/sheet3.csv";

    CsvTable sheet1 = new CsvTable(sheet1Name, UnificationOptions.THROW);
    CsvTable sheet2 = new CsvTable(sheet2Name, UnificationOptions.THROW);
    CsvTable sheet3 = new CsvTable(sheet3Name, UnificationOptions.THROW);
    try {
      sheet1.unify(sheet2, false).unify(sheet3, false).toString();
      fail("Should have bombed");
    } catch (CsvRecordNotFoundException e) {
      e = null;
    }
  }

  public void testUnifyLOG() {
    String sheet1Name = "src/test/resources/sheet1.csv";
    String sheet2Name = "src/test/resources/sheet2.csv";
    String sheet3Name = "src/test/resources/sheet3.csv";

    CsvTable sheet1 = new CsvTable(sheet1Name, UnificationOptions.LOG);
    CsvTable sheet2 = new CsvTable(sheet2Name, UnificationOptions.LOG);
    CsvTable sheet3 = new CsvTable(sheet3Name, UnificationOptions.LOG);

    String expected = "Id,field1,\n" + "1,f1,\n"
        + "2,2f1,\n";
    CsvTable sheet1u2 = sheet1.unify(sheet2, false);
    CsvTable sheet1u2u3 = sheet1u2.unify(sheet3, false);

    String out = sheet1u2u3.toString();
    assertEquals(expected, out);
  }

  public void testUnifyLOGunifyWithEmpty() {
    String sheet1Name = "src/test/resources/sheet1.csv";
    String sheet2Name = "src/test/resources/sheet2.csv";
    String sheet3Name = "src/test/resources/sheet3.csv";

    CsvTable sheet1 = new CsvTable(sheet1Name, UnificationOptions.LOG);
    CsvTable sheet2 = new CsvTable(sheet2Name, UnificationOptions.LOG);
    CsvTable sheet3 = new CsvTable(sheet3Name, UnificationOptions.LOG);

    CsvTable sheet1u2 = sheet1.unify(sheet2, true);
    String expected = "Id,field1,field2,\n" + "1,f1,f2,\n"
        + "2,2f1,2f2,\n";
    assertEquals(expected, sheet1u2.toString());

    CsvTable sheet1u2u3 = sheet1u2.unify(sheet3, true);
    expected = "Id,field1,field2,field3,field4,\n" + "1,f1,f2,f3,f4,\n"
        + "2,2f1,2f2,2f3,2f4,\n";

    String out = sheet1u2u3.toString();
    assertEquals(expected, out);

  }

  public void testUnifyDEFAULT() {
    String sheet1Name = "src/test/resources/sheet1.csv";
    String sheet2Name = "src/test/resources/sheet2.csv";
    String sheet3Name = "src/test/resources/sheet3.csv";

    CsvTable sheet1 = new CsvTable(sheet1Name, UnificationOptions.DEFAULT);
    CsvTable sheet2 = new CsvTable(sheet2Name, UnificationOptions.DEFAULT);
    CsvTable sheet3 = new CsvTable(sheet3Name, UnificationOptions.DEFAULT);
    String expected = "Id,field1,\n" + "1,f1,\n"
        + "2,2f1,\n" + "3,3f1,\n";
    String out = sheet1.unify(sheet2, false).unify(sheet3, false).toString();
    assertEquals(expected, out);

  }

  public void testGetUnificationOption() {
    String sheetName = "src/test/resources/sheet1.csv";

    assertEquals(UnificationOptions.LOG, new CsvTable(sheetName, UnificationOptions.LOG)
        .getUnificationOption());
    assertEquals(UnificationOptions.THROW, new CsvTable(sheetName, UnificationOptions.THROW)
        .getUnificationOption());
    assertEquals(UnificationOptions.DEFAULT, new CsvTable(sheetName, UnificationOptions.DEFAULT)
        .getUnificationOption());

  }

  public void testGetColumn() {
    String sheetName = "src/test/resources/sheet1.csv";
    CsvTable sheet = new CsvTable(sheetName, UnificationOptions.LOG);
    assertTrue(sheet.hasColumn("Id"));
    assertTrue(sheet.hasColumn("field1"));
    assertFalse(sheet.hasColumn("field2"));
  }

  public void testUnifyDEFAULTUnifyWithEmpty() {
    CsvTable holey = new CsvTable("src/test/resources/sheet2WithBlanks.csv",
        UnificationOptions.DEFAULT);
    CsvTable sheet2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.DEFAULT);
    String expected = "Id,field1,field2,\n" + "1,f1,f2,\n"
        + "2,2f1,2f2,\n";
    String out = holey.unify(sheet2, true).toString();
    assertEquals(expected, out);

  }

  public void testUnifyDEFAULTNotUnifyWithEmpty() {
    CsvTable holey = new CsvTable("src/test/resources/sheet2WithBlanks.csv",
        UnificationOptions.DEFAULT);
    CsvTable sheet2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.DEFAULT);
    try {
      holey.unify(sheet2, false).toString();
      fail("Should have bombed");
    } catch (CsvRecordUnificationException e) {
      e = null;
    }

  }

  public void testmultiLine() {
    CsvTable sheet1 = new CsvTable("src/test/resources/multilineField.csv",
        UnificationOptions.LOG);
    assertEquals("line1\nline2", sheet1.get("1").get("field1").getValue());
  }

  public void testColumn() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    assertEquals("Id(PK)", sheet.getColumn("Id").toString());
    assertEquals("Id", sheet.getColumn("Id").getName());
    assertEquals("field1", sheet.getColumn("field1").getName());
    assertEquals("Id", sheet.getPrimaryKeyColumn().getName());
  }

  public void testMalformedRecordCannotBeAdded() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvRecord bad = new CsvRecord(sheet);
    try {
      sheet.add(bad);
      fail("Should have bombed");
    } catch (CsvMissingPrimaryKeyException e) {
      e = null;
    }
  }

  public void testMalformedRecordCannotBeDefaulted() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvRecord bad = new CsvRecord(sheet);
    try {
      sheet.addMissingFields(bad);
      fail("Should have bombed");
    } catch (CsvMissingPrimaryKeyException e) {
      e = null;
    }
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#clear()}.
   */
  public void testClear() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    assertEquals(2, sheet.size());
    CsvField f = sheet.get("2").get("field2");
    assertEquals("2f2", f.getValue());
    sheet.clear();
    assertEquals("2f2", f.getValue());
    assertEquals(0, sheet.size());
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#get(java.lang.Object)}.
   */
  public void testGet() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    assertEquals("f1", sheet.get("1").get("field1").getValue());
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#containsValue(java.lang.Object)}.
   */
  public void testContainsValue() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    assertTrue(sheet.containsValue(sheet.get("1")));
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#entrySet()}.
   */
  public void testEntrySet() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    String keys = "";
    String values = "";
    for (Entry<String, CsvRecord> e : sheet.entrySet()) {
      keys = keys + (keys != "" ? "," : "") + e.getKey();
      values = values + (values != "" ? "," : "") + e.getValue().toJSON();
    }
    assertEquals("2,1", keys);
    // Not the order that you might expect
    assertEquals(
        "{\"Id\": \"2\",\"field1\": \"2f1\",\"field2\": \"2f2\"},{\"Id\": \"1\",\"field1\": \"f1\",\"field2\": \"f2\"}",
        values);
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#isEmpty()}.
   */
  public void testIsEmpty() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    assertFalse(sheet.isEmpty());
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#keySet()}.
   */
  public void testKeySet() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    Set<String> s = sheet.keySet();
    assertEquals("[2, 1]", s.toString());
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#put(java.lang.String, net.pizey.csv.CsvRecord)} .
   */
  public void testPut() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvRecord r = new CsvRecord(sheet);
    r.setLineNo(100);
    CsvField pk = new CsvField(sheet.getPrimaryKeyColumn(), "3");
    r.addField(pk);
    r.addField(new CsvField(sheet.getColumn("field1"), "new"));
    sheet.put("3", r);
    assertEquals("Id,field1,field2,\n1,f1,f2,\n2,2f1,2f2,\n3,new,,\n", sheet.toString());
    try {
      sheet.put("3", r);
      fail("Should have bombed");
    } catch (CsvDuplicateKeyException e) {
      e = null;
    }
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#putAll(java.util.Map)}.
   */
  public void testPutAll() {
    CsvTable sheet1 = new CsvTable("src/test/resources/sheet1.csv", UnificationOptions.LOG);
    CsvTable sheet2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvTable sheet2a = new CsvTable("src/test/resources/sheet2a.csv", UnificationOptions.LOG);
    try {
      sheet1.putAll(sheet2);
      fail("Should have bombed");
    } catch (CsvDuplicateKeyException e) {
      e = null;
    }
    sheet2.putAll(sheet2a);
    assertEquals("Id,field1,field2,\n1,f1,f2,\n2,2f1,2f2,\n3,3f1,3f2,\n4,4f1,4f2,\n", sheet2
        .toString());
    sheet1.putAll(sheet2a);
    assertEquals("Id,field1,\n1,f1,\n2,2f1,\n3,3f1,\n4,4f1,\n", sheet1.toString());

  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#remove(java.lang.Object)}.
   */
  public void testRemove() {
    CsvTable sheet2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvTable sheet2a = new CsvTable("src/test/resources/sheet2a.csv", UnificationOptions.LOG);
    sheet2.putAll(sheet2a);
    assertEquals("Id,field1,field2,\n1,f1,f2,\n2,2f1,2f2,\n3,3f1,3f2,\n4,4f1,4f2,\n", sheet2
        .toString());
    sheet2.remove("4");
    assertEquals("Id,field1,field2,\n1,f1,f2,\n2,2f1,2f2,\n3,3f1,3f2,\n", sheet2.toString());
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#size()}.
   */
  public void testSize() {
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#values()}.
   */
  public void testValues() {
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#containsKey(java.lang.Object)}.
   */
  public void testContainsKey() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    assertTrue(sheet.containsKey("2"));
  }

  /**
   * Test method for {@link net.pizey.csv.CsvTable#iterator()}.
   */
  public void testIterator() {
    CsvTable sheet = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    int i = 0;
    for (CsvRecord record : sheet) {
      i++;
      assertEquals(new Integer(i).toString(), record.getPrimaryKey());
      assertEquals(i + 1, record.getLineNo());
    }
  }

  public void testEquals() {
    CsvTable t = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvTable t2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.DEFAULT);
    CsvTable t3 = new CsvTable("src/test/resources/sheet2a.csv", UnificationOptions.DEFAULT);

    assertEquals(t, t);
    assertEquals(t, new CsvTable(t));
    assertFalse(t.equals(t2));
    assertFalse(t.equals(t3));
    assertFalse(t2.equals(t3));

    assertFalse(t.equals(null));
    assertFalse(t.equals(new Object()));

    CsvTable order1 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvTable order2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    order2.makeFirstAndPrimary("field1");
    assertFalse(order1.equals(order2));

    CsvTable key1 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvTable key2 = new CsvTable("src/test/resources/sheet2.csv", "field1", UnificationOptions.LOG);
    assertFalse(key1.equals(key2));

    CsvTable records1 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvTable records2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvRecord extraRecord = new CsvRecord(records2);
    extraRecord.addField(new CsvField(records2.getPrimaryKeyColumn(), "3"));
    extraRecord.addField(new CsvField(records2.getColumn("field1"), "3f1"));
    extraRecord.addField(new CsvField(records2.getColumn("field2"), "3f2"));
    records2.add(extraRecord);
    assertFalse(records1.equals(records2));

    CsvTable columns1 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvTable columns2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    columns2.addColumn(new CsvColumn("field3", false));
    assertFalse(columns1.equals(columns2));

    CsvTable fields1 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    CsvTable fields2 = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    fields2.get("1").get("field1").setValue("Changed");
    assertFalse(fields1.equals(fields2));
  }

  public void testHashCode() {
    CsvTable t = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    assertEquals(-2094807155, t.getColumnsInOrder().hashCode());
    assertEquals(-1088807178, t.getDataFile().hashCode());

    assertEquals(1268261037, t.getKeyToRecord().hashCode());
    assertEquals(2530, t.getKeys().hashCode());
    assertEquals(-903459149, t.getName().hashCode());
    assertEquals(489510, t.getNameToColumn().hashCode());
    assertEquals(41485, t.getPrimaryKeyColumn().hashCode());
    assertEquals(521094758, t.hashCode());
  }

  public void testEnumHashCode() {
    assertEquals(UnificationOptions.THROW.hashCode(), UnificationOptions.THROW.hashCode());
    assertEquals(UnificationOptions.DEFAULT.hashCode(), UnificationOptions.DEFAULT.hashCode());
    assertEquals(UnificationOptions.LOG.hashCode(), UnificationOptions.LOG.hashCode());

    assertFalse(UnificationOptions.LOG.hashCode() == UnificationOptions.DEFAULT.hashCode());
    assertFalse(UnificationOptions.LOG.hashCode() == UnificationOptions.THROW.hashCode());
    assertFalse(UnificationOptions.DEFAULT.hashCode() == UnificationOptions.THROW.hashCode());

    /**
     * NOTE Previously I was using unificationOption.hashCode()
     * 
     * This integer need not remain consistent from one execution of an application to another execution of the same
     * application.
     * 
     * @see java.lang.Object#hashCode()
     */
    // assertEquals(190960491, UnificationOptions.THROW.hashCode());
    // assertEquals(2086984721, UnificationOptions.LOG.hashCode());
    // assertEquals(1101799396, UnificationOptions.DEFAULT.hashCode());
  }

  public void testGeneratedBridgeMethods() throws Exception {

    // for (Method m : CsvTable.class.getMethods()) {
    //   System.out.println(m.toGenericString()); 
    // }

    CsvTable t = new CsvTable("src/test/resources/sheet2.csv", UnificationOptions.LOG);
    Object o = t.get((Object) "1");
    CsvRecord r = t.get("1");
    assertTrue(o.equals(r));

    // Object o2 = t.put("jj", o);
    Method method = t.getClass().getMethod("put", new Class[] { Object.class, Object.class });
    method.invoke(t, "jj", o);
    Object o3 = t.remove("1");
    assertEquals(r, o3);
  }

}

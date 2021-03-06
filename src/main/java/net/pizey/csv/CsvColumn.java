package net.pizey.csv;

/**
 * A column in a CSV file.
 * <p>
 * A CSVColumn can be a primary key for this table.
 * <p>
 * Every CsvTable must have one primary key.
 * 
 */
public class CsvColumn implements Cloneable {

  private String name;
  private boolean isPrimaryKey;

  /**
   * Constructor for a key value into another table.
   * 
   * @param name
   *          the name of the column
   * @param isPrimaryKey
   *          flag to indicate this is the primary key column.
   */
  public CsvColumn(String name, boolean isPrimaryKey) {
    super();
    if (name == null)
      throw new NullPointerException();
    this.name = name;
    this.isPrimaryKey = isPrimaryKey;
  }

  public String getName() {
    return name;
  }

  public boolean isPrimaryKey() {
    return isPrimaryKey;
  }

  public void setPrimaryKey(boolean v) {
    this.isPrimaryKey = v;
  }

  @Override
  public String toString() {
    return name + (isPrimaryKey ? "(PK)" : "");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (isPrimaryKey ? 1231 : 1237);
    result = prime * result + name.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CsvColumn)) {
      return false;
    }
    CsvColumn other = (CsvColumn) obj;
    if (isPrimaryKey != other.isPrimaryKey) {
      return false;
    }
    if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  protected Object clone() {
    return new CsvColumn(this.name, this.isPrimaryKey);
  }
}

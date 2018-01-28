package org.ripreal.textclassifier2.model;

public class CharacteristicValue {

  private int id;

  private int orderNumber;

  private String value;

  private Characteristic characteristic;

  public CharacteristicValue() {
  }

  public CharacteristicValue(int orderNumber, String value) {
    this.orderNumber = orderNumber;
    this.value = value;
  }

  public CharacteristicValue(String value) {
    this(0, value);
  }

  public void setCharacteristic(Characteristic characteristic) {
    this.characteristic = characteristic;
  }

  public int getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(int orderNumber) {
    this.orderNumber = orderNumber;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    return ((o instanceof CharacteristicValue) && (this.value.equals(((CharacteristicValue) o).getValue())));
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }
}
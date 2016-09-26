package com.veracloud.jton;

import javafx.util.Callback;

public abstract class KeyProvider implements Callback<JtonElement, String> {

  @Override
  public abstract String call(JtonElement param);

}
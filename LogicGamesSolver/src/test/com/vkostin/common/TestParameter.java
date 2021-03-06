package com.vkostin.common;

public class TestParameter {
  private final int _number;
  private final String _puzzle;
  private final String _solution;

  public TestParameter(int number, String puzzle, String solution) {
    this._number = number;
    this._puzzle = puzzle;
    this._solution = solution;
  }

  public int number() { return _number; }
  public String puzzle() { return _puzzle; }
  public String solution() { return _solution; }

}

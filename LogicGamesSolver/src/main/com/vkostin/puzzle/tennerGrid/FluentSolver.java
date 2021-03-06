package com.vkostin.puzzle.tennerGrid;

import com.vkostin.common.AbstractFluentCheckSingleChangeSolverInstance;
import com.vkostin.common.FluentCell;
import com.vkostin.common.Puzzle;
import com.vkostin.common.ValueCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FluentSolver implements com.vkostin.common.Solver {

  @Override
  public Puzzle solve(Puzzle puzzle) {
    return new SolverInstance(puzzle).solve();
  }

  static class SolverInstance extends AbstractFluentCheckSingleChangeSolverInstance {

    final private List<Integer> _assumptions;

    public SolverInstance(final Puzzle puzzle) {
      super(puzzle);
      _assumptions = IntStream.rangeClosed(Rules.MIN_ALLOWED_VALUE, Rules.MAX_ALLOWED_VALUE)
              .boxed()
              .collect(Collectors.toList());
    }

    private ValueCell valueCellAt(int row, int col) {
      return (ValueCell) _puzzle.at(row, col);
    }

    @Override
    protected boolean isValueCellUnsolved(final FluentCell cell) {
      return Optional.of(cell.cell())
              .map(c -> c.as(ValueCell.class))
              .map(Rules::hasUnsolvedValue)
              .orElse(false);
    }

    @Override
    protected Iterable<? extends Object> assumptionsToBeMade() { return _assumptions; }

    @Override
    protected boolean isAnyRuleBroken(final FluentCell cell) {
      if (doesColumnContainError(cell.address().col())) return true;
      if (!areAllProperValueUniqueInRow(cell.address().row())) return true;
      if (!valuesInContiguousCellsAreDifferent(cell)) return true;

      return false;
    }

    private boolean doesColumnContainError(final int col) {
      List<ValueCell> valueCells = new ArrayList<>();
      for (int rowIndex = 0; rowIndex < _puzzle.rowCount() - 1; rowIndex++) {
        valueCells.add(valueCellAt(rowIndex, col));
      }

      int expectedSumOfValues = ((TaskCell)_puzzle.at(_puzzle.rowCount()-1, col)).getSumOfValuesAbove();
      return ValueCell.doValueCellsFailToMeetExpectation(expectedSumOfValues, valueCells, Rules::hasProperValue, true);
    }

    private boolean areAllProperValueUniqueInRow(final int row) {
      List<ValueCell> valueCellsWithProperValues = new ArrayList<>();
      for (int colIndex = 0; colIndex < _puzzle.rowLength(); colIndex++) {
        Optional.of(valueCellAt(row, colIndex))
                .filter(Rules::hasProperValue)
                .ifPresent(valueCellsWithProperValues::add);
      }

      long countOfDistinctValues = valueCellsWithProperValues.stream()
              .mapToInt(ValueCell::getValue)
              .distinct()
              .count();

      return valueCellsWithProperValues.size() == countOfDistinctValues;
    }

    private boolean valuesInContiguousCellsAreDifferent(final FluentCell cell) {
      int value = ((ValueCell)cell.cell()).getValue();
      if (!threeConsecutiveValueCellsInRowDoNotHaveValuesEqualTo(value, cell.address().row()-1, cell.address().col()-1)) return false;
      if (!threeConsecutiveValueCellsInRowDoNotHaveValuesEqualTo(value, cell.address().row()+1, cell.address().col()-1)) return false;

      return true;
    }

    private boolean threeConsecutiveValueCellsInRowDoNotHaveValuesEqualTo(final int valueToCheck, final int rowIndex, final int lowestColumnIndex) {
      if (0 > rowIndex || _puzzle.rowCount() - 1 <= rowIndex) return true;

      for (int colIndex = Math.max(lowestColumnIndex, 0); colIndex <= Math.min(lowestColumnIndex + 2, _puzzle.rowLength() - 1); colIndex++) {
        if (Optional.of(valueCellAt(rowIndex, colIndex))
                .filter(Rules::hasProperValue)
                .map(ValueCell::getValue)
                .filter(value -> valueToCheck == value)
                .isPresent()) return false;
      }
      return true;
    }

    @Override
    protected void setValue(final Object value, final FluentCell cell) {
      ((ValueCell)cell.cell()).setValue((Integer) value);
    }

    @Override
    protected void clearValue(final FluentCell cell) {
      Rules.clearValue((ValueCell) cell.cell());
    }

  }

}

package com.adalbert.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;

@State(Scope.Benchmark)
public class Weird {

    static class Cell {
        private final int row, column;
        private final boolean isAlive;

        private Cell(int row, int column, boolean isAlive) {
            this.row = row;
            this.column = column;
            this.isAlive = isAlive;
        }

        public static Cell livingCell(int row, int column) {
            return new Cell(row, column, true);
        }

        public static Cell deadCell(int row, int column) {
            return new Cell(row, column, false);
        }

        public int row() {
            return row;
        }

        public int column() {
            return column;
        }

        public boolean isAlive() {
            return isAlive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return row == cell.row && column == cell.column && isAlive == cell.isAlive;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, column, isAlive);
        }

        @Override
        public String toString() {
            return "Cell{" +
                    "row=" + row +
                    ", column=" + column +
                    ", isAlive=" + isAlive +
                    '}';
        }
    }

    private static final List<Cell> startingPoint = Arrays.asList(
        Cell.livingCell(0,0), Cell.livingCell(1,0), Cell.livingCell(2,0),
        Cell.livingCell(3,1),
        Cell.livingCell(1,2), Cell.livingCell(2,2),
        Cell.livingCell(3,3),
        Cell.livingCell(0,4), Cell.livingCell(1,4), Cell.livingCell(2,4)
    );

    private static void addLivingCell(Set<Cell> collection, int row, int column) {
        collection.remove(Cell.deadCell(row, column));
        collection.add(Cell.livingCell(row, column));
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!(i == 0 && j == 0) && !collection.contains(Cell.livingCell(row+i, column+j)))
                    collection.add(Cell.deadCell(row+i, column+j));
            }
        }
    }

    private static void removeLivingCell(Set<Cell> collection, int row, int column) {
        collection.remove(Cell.livingCell(row, column));
        if (neighboursCount(collection, row, column) != 0)
            collection.add(Cell.deadCell(row, column));
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!(i == 0 && j == 0) && neighboursCount(collection, row+i, column+j) == 0)
                    collection.remove(Cell.deadCell(row+i, column+j));
            }
        }
    }

    private static int neighboursCount(Set<Cell> collection, int row, int column) {
        int neighbours = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!(i == 0 && j == 0) && collection.contains(Cell.livingCell(row+i, column+j)))
                    neighbours += 1;
            }
        }
        return neighbours;
    }

    private static void addLivingCell(scala.collection.mutable.Set<Cell> collection, int row, int column) {
        collection.remove(Cell.deadCell(row, column));
        collection.add(Cell.livingCell(row, column));
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!(i == 0 && j == 0) && !collection.contains(Cell.livingCell(row+i, column+j)))
                    collection.add(Cell.deadCell(row+i, column+j));
            }
        }
    }

    private static void removeLivingCell(scala.collection.mutable.Set<Cell> collection, int row, int column) {
        collection.remove(Cell.livingCell(row, column));
        if (neighboursCount(collection, row, column) != 0)
            collection.add(Cell.deadCell(row, column));
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!(i == 0 && j == 0) && neighboursCount(collection, row+i, column+j) == 0)
                    collection.remove(Cell.deadCell(row+i, column+j));
            }
        }
    }

    private static int neighboursCount(scala.collection.mutable.Set<Cell> collection, int row, int column) {
        int neighbours = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!(i == 0 && j == 0) && collection.contains(Cell.livingCell(row+i, column+j)))
                    neighbours += 1;
            }
        }
        return neighbours;
    }

    public static void displayCells(Set<Cell> collection) {
        int minRow = collection.stream().min(Comparator.comparingInt(c -> c.row)).get().row;
        int maxRow = collection.stream().max(Comparator.comparingInt(c -> c.row)).get().row;
        int minCol = collection.stream().min(Comparator.comparingInt(c -> c.column)).get().column;
        int maxCol = collection.stream().max(Comparator.comparingInt(c -> c.column)).get().column;
        System.out.printf("r:(%d, %d), c:(%d, %d)\n", minRow, maxRow, minCol, maxCol);
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                if (collection.contains(Cell.livingCell(i, j)))
                    System.out.print("#");
                else if (collection.contains(Cell.deadCell(i, j)))
                    System.out.print(".");
                else
                    System.out.print(" ");
            }
            System.out.println();
        }
        System.out.print("\n\n\n");
    }

    @Benchmark
    @Fork(1)
    @Measurement(time=1)
    @Warmup(time=1)
    public void testJHashSet(Blackhole bh) {
        HashSet<Cell> collection = new HashSet<>();
        for (Cell cell : startingPoint) addLivingCell(collection, cell.row(), cell.column());
        List<Cell> additions = new ArrayList<>();
        List<Cell> removals = new ArrayList<>();
        while (!collection.isEmpty()) {
            collection.forEach(cell -> {
                int neighbours = neighboursCount(collection, cell.row(), cell.column());
                if (!cell.isAlive() && neighbours == 3)
                    additions.add(Cell.livingCell(cell.row(), cell.column()));
                else if (cell.isAlive() && (neighbours < 2 || neighbours > 3))
                    removals.add(Cell.livingCell(cell.row(), cell.column()));
            });
            additions.forEach(c -> addLivingCell(collection, c.row(), c.column()));
            removals.forEach(c -> removeLivingCell(collection, c.row(), c.column()));
            additions.clear(); removals.clear();
        }
    }

    @Benchmark
    @Fork(1)
    @Measurement(time=1)
    @Warmup(time=1)
    public void testJLinkedSet(Blackhole bh) {
        LinkedHashSet<Cell> collection = new LinkedHashSet<>();
        for (Cell cell : startingPoint) addLivingCell(collection, cell.row(), cell.column());
        List<Cell> additions = new ArrayList<>();
        List<Cell> removals = new ArrayList<>();
        while (!collection.isEmpty()) {
            collection.forEach(cell -> {
                int neighbours = neighboursCount(collection, cell.row(), cell.column());
                if (!cell.isAlive() && neighbours == 3)
                    additions.add(Cell.livingCell(cell.row(), cell.column()));
                else if (cell.isAlive() && (neighbours < 2 || neighbours > 3))
                    removals.add(Cell.livingCell(cell.row(), cell.column()));
            });
            additions.forEach(c -> addLivingCell(collection, c.row(), c.column()));
            removals.forEach(c -> removeLivingCell(collection, c.row(), c.column()));
            additions.clear(); removals.clear();
        }
    }

    @Benchmark
    @Fork(1)
    @Measurement(time=1)
    @Warmup(time=1)
    public void testSHashSet(Blackhole bh) {
        scala.collection.mutable.HashSet<Cell> collection = new scala.collection.mutable.HashSet<>();
        for (Cell cell : startingPoint) addLivingCell(collection, cell.row(), cell.column());
        List<Cell> additions = new ArrayList<>();
        List<Cell> removals = new ArrayList<>();
        while (!collection.isEmpty()) {
            collection.foreach(cell -> {
                int neighbours = neighboursCount(collection, cell.row(), cell.column());
                if (!cell.isAlive() && neighbours == 3)
                    additions.add(Cell.livingCell(cell.row(), cell.column()));
                else if (cell.isAlive() && (neighbours < 2 || neighbours > 3))
                    removals.add(Cell.livingCell(cell.row(), cell.column()));
                return null;
            });
            additions.forEach(c -> addLivingCell(collection, c.row(), c.column()));
            removals.forEach(c -> removeLivingCell(collection, c.row(), c.column()));
            additions.clear(); removals.clear();
        }
    }

    @Benchmark
    @Fork(1)
    @Measurement(time=1)
    @Warmup(time=1)
    public void testSLinkedSet(Blackhole bh) {
        scala.collection.mutable.LinkedHashSet<Cell> collection = new scala.collection.mutable.LinkedHashSet<>();
        for (Cell cell : startingPoint) addLivingCell(collection, cell.row(), cell.column());
        List<Cell> additions = new ArrayList<>();
        List<Cell> removals = new ArrayList<>();
        while (!collection.isEmpty()) {
            collection.foreach(cell -> {
                int neighbours = neighboursCount(collection, cell.row(), cell.column());
                if (!cell.isAlive() && neighbours == 3)
                    additions.add(Cell.livingCell(cell.row(), cell.column()));
                else if (cell.isAlive() && (neighbours < 2 || neighbours > 3))
                    removals.add(Cell.livingCell(cell.row(), cell.column()));
                return null;
            });
            additions.forEach(c -> addLivingCell(collection, c.row(), c.column()));
            removals.forEach(c -> removeLivingCell(collection, c.row(), c.column()));
            additions.clear(); removals.clear();
        }
    }

    public static void main(String[] args) {
        HashSet<Cell> collection = new HashSet<>();
        for (Cell cell : startingPoint) addLivingCell(collection, cell.row(), cell.column());
        List<Cell> additions = new ArrayList<>();
        List<Cell> removals = new ArrayList<>();
        while (!collection.isEmpty()) {
            displayCells(collection);
            Iterator<Cell> iter = collection.iterator();
            while (iter.hasNext()) {
                Cell cell = iter.next();
                int neighbours = neighboursCount(collection, cell.row(), cell.column());
                if (!cell.isAlive() && neighbours == 3)
                    additions.add(Cell.livingCell(cell.row(), cell.column()));
                else if (cell.isAlive() && (neighbours < 2 || neighbours > 3))
                    removals.add(Cell.livingCell(cell.row(), cell.column()));
            }
            additions.forEach(c -> addLivingCell(collection, c.row(), c.column()));
            removals.forEach(c -> removeLivingCell(collection, c.row(), c.column()));
            additions.clear(); removals.clear();
        }
    }
}

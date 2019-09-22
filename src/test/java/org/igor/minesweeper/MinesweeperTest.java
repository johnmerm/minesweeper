package org.igor.minesweeper;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class MinesweeperTest {

    private byte[] testMinePlan = new byte[]{
            4, 8,
            10 + 1, 10 + 7,
            20 + 3, 20 + 5,
            30 + 4,
            40,
            70 + 7,
            90 + 5
    };

    private Minesweeper minesweeper = new Minesweeper(10, 10, 10, testMinePlan);

    @Test
    void testAdjacent() {
        int dimX = minesweeper.getDimX();
        int dimY = minesweeper.getDimY();

        byte pos = minesweeper.getPos(5, 5);
        byte[] adj = minesweeper.adjacent(pos);
        assertEquals(8, adj.length);

        pos = minesweeper.getPos(0, 0);
        adj = minesweeper.adjacent(pos);
        assertEquals(3, adj.length);

        pos = minesweeper.getPos(dimX - 1, dimY - 1);
        adj = minesweeper.adjacent(pos);
        assertEquals(3, adj.length);

        pos = minesweeper.getPos(0, 2);
        adj = minesweeper.adjacent(pos);
        assertEquals(5, adj.length);

    }

    @Test
    void testNumOfMines() {
        byte pos = minesweeper.getPos(5, 5);
        int numOfMines = minesweeper.getNumberOfAdjacentMines(pos);
        assertEquals(0, numOfMines);

        pos = minesweeper.getPos(4, 5);
        numOfMines = minesweeper.getNumberOfAdjacentMines(pos);
        assertEquals(1, numOfMines);

        pos = minesweeper.getPos(3, 4);
        numOfMines = minesweeper.getNumberOfAdjacentMines(pos);
        assertEquals(-1, numOfMines);

        pos = minesweeper.getPos(2, 4);
        numOfMines = minesweeper.getNumberOfAdjacentMines(pos);
        assertEquals(3, numOfMines);
    }

    @Test
    public void testMatchOne() {
        byte pos = minesweeper.getPos(4, 5);
        assertTrue(minesweeper.matches(pos, 1, testMinePlan));

        pos = minesweeper.getPos(1, 7);
        assertFalse(minesweeper.matches(pos, 1, testMinePlan));
    }

    @Test
    public void testRegenerateExhaustive() {
        Map<Byte, Integer> soFar = new TreeMap<>();
        soFar.put((byte) 0, 0);
        soFar.put((byte) 1, 0);
        soFar.put((byte) 2, 0);
        soFar.put((byte) 3, 0);
        soFar.put((byte) 4, 0);
        soFar.put((byte) 5, 0);
        soFar.put((byte) 6, 1);
        soFar.put((byte) 10, 0);
        soFar.put((byte) 11, 0);
        soFar.put((byte) 12, 0);
        soFar.put((byte) 13, 0);
        soFar.put((byte) 14, 0);
        soFar.put((byte) 15, 0);
        soFar.put((byte) 16, 1);
        soFar.put((byte) 20, 0);
        soFar.put((byte) 21, 0);
        soFar.put((byte) 22, 0);
        soFar.put((byte) 23, 0);
        soFar.put((byte) 24, 1);
        soFar.put((byte) 25, 1);
        soFar.put((byte) 26, 1);
        soFar.put((byte) 27, 1);
        soFar.put((byte) 28, 2);
        soFar.put((byte) 30, 0);
        soFar.put((byte) 31, 0);
        soFar.put((byte) 32, 0);
        soFar.put((byte) 33, 1);
        soFar.put((byte) 34, 2);
        soFar.put((byte) 36, 1);
        soFar.put((byte) 37, 0);
        soFar.put((byte) 38, 2);
        soFar.put((byte) 40, 0);
        soFar.put((byte) 41, 0);
        soFar.put((byte) 42, 0);
        soFar.put((byte) 43, 2);
        soFar.put((byte) 45, 3);
        soFar.put((byte) 46, 1);
        soFar.put((byte) 47, 0);
        soFar.put((byte) 48, 2);
        soFar.put((byte) 50, 0);
        soFar.put((byte) 51, 0);
        soFar.put((byte) 52, 1);
        soFar.put((byte) 53, 3);
        soFar.put((byte) 55, 2);
        soFar.put((byte) 56, 0);
        soFar.put((byte) 57, 0);
        soFar.put((byte) 58, 1);
        soFar.put((byte) 59, 1);
        soFar.put((byte) 60, 0);
        soFar.put((byte) 61, 0);
        soFar.put((byte) 62, 1);
        soFar.put((byte) 65, 1);
        soFar.put((byte) 66, 0);
        soFar.put((byte) 67, 0);
        soFar.put((byte) 68, 0);
        soFar.put((byte) 69, 0);
        soFar.put((byte) 70, 1);
        soFar.put((byte) 71, 1);
        soFar.put((byte) 72, 2);
        soFar.put((byte) 75, 1);
        soFar.put((byte) 76, 0);
        soFar.put((byte) 77, 0);
        soFar.put((byte) 78, 0);
        soFar.put((byte) 79, 0);
        soFar.put((byte) 85, 1);
        soFar.put((byte) 86, 0);
        soFar.put((byte) 87, 0);
        soFar.put((byte) 88, 0);
        soFar.put((byte) 89, 0);
        soFar.put((byte) 95, 1);
        soFar.put((byte) 96, 0);
        soFar.put((byte) 97, 0);
        soFar.put((byte) 98, 0);
        soFar.put((byte) 99, 0);

        byte[] plan = new byte[]{7, 8, 9, 17, 18, 19, 29, 35, 39, 44};

        assertTrue(minesweeper.matches(soFar,plan));
    }
}
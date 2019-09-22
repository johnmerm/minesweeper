package org.igor.minesweeper;


import com.sun.istack.internal.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Minesweeper {
    private int dimX,dimY,numOfMines;
    byte[] minePositions;

    public Minesweeper() {
        this(10,10,10);
    }

    public Minesweeper(int dimX,int dimY,int numOfMines) {
        this.dimX = dimX;
        this.dimY = dimY;
        this.numOfMines = numOfMines;
        if (dimX*dimY >=256){
            throw new RuntimeException("cannot hold "+(dimX*dimY)+" positions in a byte");
        }
        minePositions = init(dimX, dimY, numOfMines,null);
    }

    /**
     * Test - only constructor
     * @param dimX
     * @param dimY
     * @param numOfMines
     * @param minePositions the array of mines to set
     */
    Minesweeper(int dimX,int dimY,int numOfMines,byte[] minePositions) {
        this.dimX = dimX;
        this.dimY = dimY;
        this.numOfMines = numOfMines;
        if (dimX * dimY >= 256) {
            throw new RuntimeException("cannot hold " + (dimX * dimY) + " positions in a byte");
        }
        this.minePositions = new byte[numOfMines];
        System.arraycopy(minePositions,0,this.minePositions,0,numOfMines);

    }


    static byte[] init(int dimX, int dimY, int numOfMines,@Nullable Map<Byte,Integer> revealed){
        Map<Byte,Integer> openedSoFar = revealed!=null?revealed: Collections.emptyMap();

        int totalSize = dimX*dimY;
        int minesFound = (int) openedSoFar.values().stream().filter(v->v== -1).count();
        int minesAvailable = numOfMines - minesFound;
        int boxesOpen = openedSoFar.size();
        int boxesAvailable = totalSize - boxesOpen;

        byte[] boxesAvailablePositions = new byte[boxesAvailable];
        AtomicInteger cnt = new AtomicInteger(0);
        IntStream.range(0,totalSize)
                .filter(i-> !openedSoFar.containsKey((byte)i))
                .forEach(ip->{
                    boxesAvailablePositions[cnt.getAndIncrement()] = (byte)ip;
                });

        Set<Byte> soFar = openedSoFar.entrySet().stream().filter(e->e.getValue() == -1).map(Map.Entry::getKey).collect(Collectors.toSet());




        byte[] minePositions = new byte[numOfMines];

        TreeSet<Byte> temp = new TreeSet<>(soFar);
        for (int i =0;i<minesAvailable;i++){
            int tmpIdx = (int)Math.floor(Math.random()*boxesAvailable);
            byte tmp = boxesAvailablePositions[tmpIdx];
            while (temp.contains(tmp)){
                tmpIdx = (int)Math.floor(Math.random()*boxesAvailable);
                tmp = boxesAvailablePositions[tmpIdx];
            }
            temp.add(tmp);
        }
        Iterator<Byte> iterator = temp.iterator();
        for (int i=0;i<numOfMines;i++){
            minePositions[i] = iterator.next();
        }
        return minePositions;
    }

    public byte getPos(int x,int y){
        if (x >= dimX || y >= dimY ||x <0 || y<0){
            throw new IllegalArgumentException();
        }
        return (byte) (x*dimX+y);
    }

    byte[] adjacent(byte pos){
        return adjacent(pos,dimX,dimY);
    }

    static byte[] adjacent(byte pos,int dimX,int dimY){
        byte xPos = (byte) (pos/dimX);
        byte yPos = (byte)(pos % dimX);

        int cnt = 0;
        byte[] ret = new byte[8];

        for (int i=-1;i<=1;i++){
            byte nXPos= (byte) (xPos + i);
            if (nXPos<0 || nXPos > dimX-1){
                continue;
            }
            for (int j=-1;j<=1;j++){
                byte nYPos= (byte) (yPos + j);
                if (nYPos<0 || nYPos > dimY-1){
                    continue;
                }

                if (nXPos == xPos && nYPos == yPos){
                    continue;
                }
                byte nPos = (byte) (nXPos*dimX+nYPos);
                ret[cnt++] = nPos;
            }
        }

        return Arrays.copyOfRange(ret,0,cnt);
    }

    public int getDimX() {
        return dimX;
    }

    public int getDimY() {
        return dimY;
    }

    public int getDimTotal(){
        return dimX*dimY;
    }

    public int getNumOfMines() {
        return numOfMines;
    }

    public int getNumberOfAdjacentMines(byte pos){
        return getNumberOfAdjacentMines(pos,dimX,dimY,minePositions);
    }

    public static int getNumberOfAdjacentMines(byte pos,int dimX,int dimY,byte[] minePositions){

        if (test(pos,minePositions)){
            return -1;
        }else{
            int cnt = 0;
            byte[] adjs = adjacent(pos,dimX,dimY);
            for (byte adj:adjs){
                if (test(adj,minePositions)){
                    cnt ++;
                }
            }
            return cnt;
        }
    }

    public boolean test(byte pos){
        return test(pos,minePositions);
    }

    public static boolean test(byte pos,byte[] plan){
        if (Arrays.binarySearch(plan,pos) >=0){
            //Found mine
            return true;
        }else{
            return false;
        }
    }

    public boolean matches(byte pos,int value,byte[] plan){
        if (test(pos,plan)){
            return value == -1;
        }
        byte[] adjs = adjacent(pos);
        int _value = 0;
        for (byte adj:adjs){
            if (test(adj,plan)){
                _value ++;
            }
        }
        return value == _value;
    }

    public boolean matches(Map<Byte,Integer> revealed, byte[] plan){
        return revealed.entrySet()
                .stream()
                .allMatch(p->{
                    return this.matches(p.getKey(),p.getValue(),plan);
                });
    }


}

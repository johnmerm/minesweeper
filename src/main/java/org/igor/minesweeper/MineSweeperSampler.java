package org.igor.minesweeper;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MineSweeperSampler {
    private static final Logger LOG = LoggerFactory.getLogger(MineSweeperSampler.class);


    private Minesweeper minesweeper;
    private int numOfSamples;
    private List<byte[]> samples;
    public MineSweeperSampler(int numOfSamples,Minesweeper minesweeper){
        this.numOfSamples = numOfSamples;
        this.minesweeper = minesweeper;
        samples = IntStream.range(0,numOfSamples).mapToObj(i->Minesweeper.init(minesweeper.getDimX(),minesweeper.getDimY(),minesweeper.getNumOfMines(),null)).collect(Collectors.toList());
    }

    private Map<Byte,Integer> openedSoFar = new TreeMap<>();

    CompletableFuture<List<byte[]>> regenerateCF;
    private volatile boolean cancel;
    private boolean exhausted = false;

    List<byte[]> regenerateBySampling(){
        Map<Byte,Integer> openedSoFar = new TreeMap<>(this.openedSoFar);
        List<byte[]> newSamples = new ArrayList<>();
        LOG.info("RegenerateBySampling STARTED for {}",openedSoFar.size());
        for (int i=0;i<numOfSamples/10-samples.size();i++){
            byte[] plan = Minesweeper.init(minesweeper.getDimX(), minesweeper.getDimY(), minesweeper.getNumOfMines(),openedSoFar);
            while (!minesweeper.matches(openedSoFar, plan)){
                if (cancel){
                    LOG.info("RegenerateCF cancelled #1");
                    throw new CancellationException("RegenerateCF cancelled #1");
                }

                plan = Minesweeper.init(minesweeper.getDimX(), minesweeper.getDimY(), minesweeper.getNumOfMines(),openedSoFar);
            }
            newSamples.add(plan);
        }
        LOG.info("RegenerateBySampling FINISHED for {}",openedSoFar.size());
        return newSamples;
    }

    List<byte[]> regenerateExhaustivelly(){
        if (exhausted){
            return Collections.emptyList();
        }
        Map<Byte,Integer> openedSoFar = new TreeMap<>(this.openedSoFar);
        int minesFound = (int) openedSoFar.values().stream().filter(v->v== -1).count();
        int minesAvailable = minesweeper.getNumOfMines() - minesFound;
        int boxesOpen = openedSoFar.size();
        int boxesAvailable = minesweeper.getDimTotal() - boxesOpen;

        LOG.info("RegenerateExhaustivelly STARTED for {}/{}/{}",boxesAvailable,minesAvailable,openedSoFar.size());
        List<byte[]> newSamples = new ArrayList<>();
        byte[] boxesAvailablePositions = new byte[boxesAvailable];
        AtomicInteger cnt = new AtomicInteger(0);
        IntStream.range(0,minesweeper.getDimTotal())
                .filter(i-> !openedSoFar.containsKey((byte)i))
                .forEach(ip->{
                    boxesAvailablePositions[cnt.getAndIncrement()] = (byte)ip;
                });

        Set<Byte> soFar = openedSoFar.entrySet().stream().filter(e->e.getValue() == -1).map(Map.Entry::getKey).collect(Collectors.toSet());

        Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(boxesAvailable,minesAvailable);
        while(iterator.hasNext()){
            if (cancel){
                LOG.info("RegenerateCF cancelled");
                throw new CancellationException("RegenerateCF cancelled #2");
            }
            int[] pzs = iterator.next();
            Set<Byte> _soFar = new TreeSet<>(soFar);
            Arrays.stream(pzs).forEach(pz->{
                byte pos = boxesAvailablePositions[pz];
                _soFar.add(pos);
            });
            byte[] plan = new byte[_soFar.size()];
            Iterator<Byte> _soFarIterator = _soFar.iterator();
            IntStream.range(0,plan.length).forEach(i->{
                plan[i] = _soFarIterator.next();
            });

            /*
            if (!minesweeper.matches(openedSoFar,plan)){
                LOG.error("Invalid plan added {} -> {}",openedSoFar,plan);
                throw new IllegalStateException("Invalid plan added");
            }
            */

            if (minesweeper.matches(openedSoFar,plan)) {
                newSamples.add(plan);
            }
        };
        LOG.info("RegenerateExhaustivelly FINISHED for {}/{}/{}",boxesAvailable,minesAvailable,openedSoFar.size());
        exhausted = true;
        return newSamples;
    }

    public void open(Map<Byte,Integer> moves){
        synchronized (this) {
            LOG.info("Open called for {} moves",moves.size());
            openedSoFar.putAll(moves);
            boolean changed = samples.removeIf(plan -> !minesweeper.matches(openedSoFar, plan));
        }
        try {
            if (regenerateCF!=null){
                cancel = true;
                try {
                    regenerateCF.get();
                }catch (InterruptedException |ExecutionException e) {
                    e.toString();
                }
                cancel = false;
            }

            regenerateCF = CompletableFuture.supplyAsync(()->{
                LOG.info("Regenerate called for {} moves",moves.size());

                int minesFound = (int) openedSoFar.values().stream().filter(v->v== -1).count();
                int minesAvailable = minesweeper.getNumOfMines() - minesFound;
                int boxesOpen = openedSoFar.size();
                int boxesAvailable = minesweeper.getDimTotal() - boxesOpen;

                long combos = CombinatoricsUtils.binomialCoefficient(boxesAvailable,minesAvailable);

                if (combos > 10_000* numOfSamples) {
                    return regenerateBySampling();


                }else{
                    return regenerateExhaustivelly();
                }


            });

            List<byte[]> newSamplesAsync = regenerateCF.get();
            synchronized (this) {
                samples.addAll(newSamplesAsync);
            }
            LOG.info("Open done for {} moves",moves.size());
        } catch (InterruptedException |ExecutionException e) {
            LOG.error("While regenerating",e);
        }


    }

    public void open(byte pos,int value){
        open(Collections.singletonMap(pos,value));
    }

    /**
     *
     * @return an array stating in how many samples a given position contains mines
     */
    public synchronized double[] test(){
        final int size = samples.size();
        return IntStream.range(0,minesweeper.getDimTotal()).mapToDouble(ipos->{
            byte pos = (byte)ipos;
            long cnt =  samples.stream().filter(sample->Minesweeper.test(pos,sample)).count();
            return (double)cnt/size;
        }).toArray();
    }
}

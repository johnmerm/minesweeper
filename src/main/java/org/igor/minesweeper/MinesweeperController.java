package org.igor.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class MinesweeperController extends JPanel {

    static class ButtonWithPB extends JButton{
        private JProgressBar progressBar;

        public ButtonWithPB() {
            super();
            BorderLayout layout = new BorderLayout();
            setLayout(layout);
            progressBar = new JProgressBar();
            progressBar.setMinimum(0);
            progressBar.setMaximum(1000);
            add(progressBar,BorderLayout.SOUTH);
        }

        public void setProgress(int pg){
            this.progressBar.setValue(pg);
        }
    }

    private Minesweeper minesweeper;
    private MineSweeperSampler mineSweeperSampler;
    private ButtonWithPB[] buttons;

    private CompletableFuture<Void> probCalculatorCF;

    class BtnActionListener implements ActionListener{
        public BtnActionListener(byte pos, ButtonWithPB button) {
            this.pos = pos;
            this.button = button;

        }

        private byte pos;
        private ButtonWithPB button;


        private void openRec(byte pos,Map<Byte,Integer> map){
            byte[] adjs = minesweeper.adjacent(pos);
            for (byte adj:adjs){
                if (map.containsKey(adj)){
                    continue;
                }
                int value = minesweeper.getNumberOfAdjacentMines(adj);
                map.put(adj,value);
                if (value == 0){
                    openRec(adj,map);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            int mines = minesweeper.getNumberOfAdjacentMines(pos);

            if (probCalculatorCF!=null){
                probCalculatorCF.cancel(true);
            }

            if (mines == 0) {
                Map<Byte,Integer> toOpen = new TreeMap<>();
                toOpen.put(pos,mines);
                openRec(pos, toOpen);
                toOpen.forEach((k,v)->{
                    ButtonWithPB b = buttons[k];
                    b.disable();
                    b.setLabel(""+v);
                    b.setBackground(Color.GRAY);
                });
                probCalculatorCF = CompletableFuture.runAsync(()->mineSweeperSampler.open(toOpen));

            }else {
                button.setLabel(""+mines);
                button.disable();
                if(mines == -1){
                    button.setBackground(Color.RED);
                }else {
                    button.setBackground(Color.GRAY);
                }
                probCalculatorCF = CompletableFuture.runAsync(()->mineSweeperSampler.open(pos, mines));
            }


            probCalculatorCF.thenAccept(_void->{
                double[] probs = mineSweeperSampler.test();
                EventQueue.invokeLater(()->{
                    IntStream.range(0,buttons.length).forEach(i->{
                        double p = probs[i];

                        byte[] adjs = minesweeper.adjacent((byte)i);
                        double avProb = IntStream.range(0,adjs.length).mapToDouble(z->probs[adjs[z]]).average().getAsDouble();
                        ButtonWithPB _button = buttons[i];

                        Color hsbColor = Color.getHSBColor(1-(float)p,0.5f,1);
                        _button.setToolTipText(""+(100*p)+"\navProb:"+(100*avProb));
                        _button.setProgress((int)(1000*p));
                        //_button.setBackground(hsbColor);
                    });
                });
            });






        }
    }

    public MinesweeperController(int dimX,int dimY,int numOfMines){
        System.out.println("starting");
        minesweeper = new Minesweeper(dimX,dimY,numOfMines);
        buttons = new ButtonWithPB[dimX*dimY];
        mineSweeperSampler = new MineSweeperSampler(100_000,minesweeper);
        System.out.println("Ready to paint");


        JPanel mwPanel = new JPanel();

        GridLayout gridLayout = new GridLayout(dimX,dimY);
        mwPanel.setLayout(gridLayout);
        IntStream.range(0,dimX*dimY).forEach(iPos->{
            byte pos = (byte)iPos;
            ButtonWithPB button =  new ButtonWithPB();
            int val = minesweeper.getNumberOfAdjacentMines(pos);

            //button.setLabel(pos+":"+val);


            button.addActionListener(new BtnActionListener(pos,button));
            buttons[iPos] = button;
            mwPanel.add(button);
        });

        this.setLayout(new BorderLayout());
        this.add(mwPanel,BorderLayout.CENTER);
        System.out.println("ready");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame();

            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add(new MinesweeperController(10, 10, 10));
            frame.setSize(4*320, 4*240);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

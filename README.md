A small Minesweeper game that tries to solve the problem 
"what is the possibility of a given square containing a mine given surrounding information"

MineSweeper Sampler tries to calculate the outcome by generating random configurations that are compatible 
with the information revealed so far. After a given threshold, an exhaustive iteration takes place

TODO:
* Use a roulette based sampling method in order to increase sampling success rate. 
* Parallelize the exhaustive method
* MAke it look like areal game: Stop on mine or on success, reload 

RUN:
MinesweeperController will open a JFrame containing the game  
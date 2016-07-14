package capsis.lib.emerge;

import capsis.kernel.Step;

public interface UseEmergeDB {

    /**
     * get the EmergeDB associated with the model
     */
    public EmergeDB getEmergeDB();

    /***
     * This method is called by the DB to udpate itself for a given step.
     * Must use objetcs in capsis.lib.emerge.treecompartementsdata and EmergeDB.add() method
     * to load data in the database.
     */
    public void getBMMValues(Step step);
}

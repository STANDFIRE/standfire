package capsis.extension.intervener.simcopintervener.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author thomas.bronner@gmail.com
 */
public class DoubleList extends ArrayList<Double> {

    public DoubleList() {
        super();
    }

    public DoubleList(Collection collection) {
        super(collection);
    }

    public DoubleList(double[] array) {
        super();
        for (int i = 0; i < array.length; i++) {
            add(array[i]);
        }
    }

    public double getSum() {
        double sum = 0d;
        for (Double d : this) {
            sum += d;
        }
        return sum;
    }

    public void roundAndPreserveSumBD() throws Exception {
        boolean allInteger = true;
        BigDecimal checksum = BigDecimal.ZERO, sum = BigDecimal.ZERO;
        BigDecimal[] temp = new BigDecimal[size()];
        for (int j = 0; j < temp.length; j++) {
            if (get(j) < 0d) {
                throw new Exception("DoubleList.roundAndPreserveSumBD() : unexpected negative number " + get(j));
            }
            temp[j] = BigDecimal.valueOf(get(j));
            checksum = checksum.add(temp[j]);
            if (temp[j].remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                allInteger = false;
            }
        }
        if (allInteger) {
            return;
        }
        checksum = checksum.setScale(0, RoundingMode.HALF_UP);
        if (checksum.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new Exception("DoubleList.roundAndPreserveSumBD() : sum(list) is not an integer ");
        }
        //round an keep track of roundings 
        BigDecimal[] roundingAmount = new BigDecimal[temp.length];
        BigDecimal maxRounding = BigDecimal.ZERO;
        for (int i = 0; i < temp.length; i++) {
            BigDecimal rawValue = temp[i];
            temp[i] = temp[i].setScale(0, RoundingMode.HALF_UP);
            roundingAmount[i] = rawValue.subtract(temp[i]).abs();
            if (roundingAmount[i].compareTo(maxRounding) > 0) {
                maxRounding = roundingAmount[i];
            }
        }
        //get max rounding indexes
        ArrayList<Integer> maxRoundingIndexes = new ArrayList<Integer>();
        for (int i = 0; i < temp.length; i++) {
            if (roundingAmount[i].compareTo(maxRounding) == 0) {
                maxRoundingIndexes.add(i);
            }
        }
        //check total number and adjust in the largests rounded elements if needed
        int noInfiniteLoop = 0;
        Iterator<Integer> it = maxRoundingIndexes.iterator();
        while (true) {
            noInfiniteLoop++;
            sum = BigDecimal.ZERO;
            for (int i = 0; i < temp.length; i++) {
                sum = sum.add(temp[i]);
            }
            if (checksum.compareTo(sum) == 0) {
                break;
            }
            if (!it.hasNext()) {
                it = maxRoundingIndexes.iterator();
            }
            //adjust in max rounded elements
            int i = it.next();
            if (sum.compareTo(checksum) > 0) {
                temp[i] = temp[i].subtract(BigDecimal.ONE);
            } else {
                temp[i] = temp[i].add(BigDecimal.ONE);
            }
            if (noInfiniteLoop > 100) {
                throw new Exception("DoubleList.roundAndPreserveSumBD() : maximum number of iterations reached while trying to adjust elements ");
            }
        }
        clear();
        for (int j = 0; j < temp.length; j++) {
            add(temp[j].doubleValue());
        }
    }

    /**
     * *
     * convert a list of double of wich the sum is an integer to a list of wich
     * the sum remains the same but the elements are integers
     */
    public void roundAndPreserveSum() throws Exception {
        boolean allInteger = true;
        double checksum = 0, sum = 0;
        double[] temp = new double[size()];
        for (int j = 0; j < temp.length; j++) {
            temp[j] = get(j);
            checksum += temp[j];
            if (!isInteger(temp[j])) {
                allInteger = false;
            }
        }
        if (allInteger) {
            return;
        }
        if (!isInteger(checksum)) {
            throw new Exception("DoubleList.roundAndPreserveSum() : sum(list) is not an integer ");
        }
        //round an keep track of roundings 
        double[] roundingAmount = new double[temp.length];
        double maxRounding = 0d;
        for (int i = 0; i < temp.length; i++) {
            double rawValue = temp[i];
            temp[i] = Math.round(rawValue);
            roundingAmount[i] = Math.abs(rawValue - temp[i]);
            if (roundingAmount[i] > maxRounding) {
                maxRounding = roundingAmount[i];
            }
        }
        //get max rounding indexes
        ArrayList<Integer> maxRoundingIndexes = new ArrayList<Integer>();
        for (int i = 0; i < temp.length; i++) {
            if (roundingAmount[i] == maxRounding) {
                maxRoundingIndexes.add(i);
            }
        }
        //check total number and adjust in the largests rounded elements if needed
        int noInfiniteLoop = 0;
        Iterator<Integer> it = maxRoundingIndexes.iterator();
        while (true) {
            noInfiniteLoop++;
            sum = 0d;
            for (int i = 0; i < temp.length; i++) {
                sum += temp[i];
            }
            if (checksum == sum) {
                break;
            }
            if (!it.hasNext()) {
                it = maxRoundingIndexes.iterator();
            }
            //adjust in max rounded elements
            if (sum > checksum) {
                temp[it.next()] -= 1;
            } else {
                temp[it.next()] += 1;
            }
            if (noInfiniteLoop > 100) {
                throw new Exception("DoubleList.roundAndPreserveSum() : maximum number of iterations reached while trying to adjust elements ");
            }
        }
        clear();
        for (int j = 0; j < temp.length; j++) {
            add(temp[j]);
        }
    }

    public static boolean isInteger(double test) {
        //an ugly alternative to the use of BigDecimal, wich require a string constructor to be accurate
        test = test % 1d;
        return test > -0.000000001d && test < 0.000000001d;
    }

}

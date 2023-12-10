package uk.gov.hmrc.eutu55.request;

import java.util.Random;
import java.util.Stack;

public class RandomNumberGenerator {

    private Stack randomStack = new Stack();

    public RandomNumberGenerator(int limit, int max) {
        new Random().ints(0, max)
                .distinct()
                .limit(limit)
                .forEach(n -> randomStack.push(n));
    }

    public int random() {
        int number = (int) randomStack.pop();
        return number;
    }
}

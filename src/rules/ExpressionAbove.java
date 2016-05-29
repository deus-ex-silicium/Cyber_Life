package rules;

import java.util.List;

/**
 * Created by Nibiru on 2016-05-28.
 */
public class ExpressionAbove extends AbstractExpression {
    private eState s;
    private eComparison comp;
    private int howMany;
    private eState center;

    public ExpressionAbove(eState s, eComparison comp, int howMany, eState center) {
            this.s = s;
            this.comp = comp;
            this.howMany = howMany;
            this.center = center;
    }
    @Override
    public boolean solve(List<List<eState>> neighborhood) {
        if (!checkCenter(neighborhood, center))
            return false;
        int count = 0;
        for(int x = 0; x < neighborhood.size(); x++) {
            if (neighborhood.get(x).get(0) == s)
                count++;
            if (neighborhood.get(x).get(1) == s)
                count++;
        }
        return this.compare(count, howMany, comp);
    }
}

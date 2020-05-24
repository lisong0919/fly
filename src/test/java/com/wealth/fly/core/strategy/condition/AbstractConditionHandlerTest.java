package com.wealth.fly.core.strategy.condition;

import com.wealth.fly.core.strategy.criteria.condition.AbstractConditionHandler;
import com.wealth.fly.core.strategy.criteria.condition.Condition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class AbstractConditionHandlerTest {

    private AbstractConditionHandler handler = new AbstractConditionHandler();

    @Test
    public void compareTest() {

        Assertions.assertTrue(handler.compare(new BigDecimal("103.44"), new BigDecimal("101.2"), Condition.ConditionValueType.EXACT, "1.5") > 0);
        Assertions.assertTrue(handler.compare(new BigDecimal("103.44"), new BigDecimal("103.43"), Condition.ConditionValueType.ANY, null) > 0);

        Assertions.assertTrue(handler.compare(new BigDecimal("103.44"), new BigDecimal("50"), Condition.ConditionValueType.PERCENT, "100") > 0);
        Assertions.assertTrue(handler.compare(new BigDecimal("103.44"), new BigDecimal("50"), Condition.ConditionValueType.PERCENT, "107") < 0);

    }
}

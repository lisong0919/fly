package com.wealth.fly.core.strategy.criteria;

import lombok.Data;

@Data
public class Condition {

    private ConditionType type;
    private ConditionValueType valueType;
    private Object value;

    public Condition() {
    }

    public Condition(ConditionType type, ConditionValueType valueType, Object value) {
        this.type = type;
        this.valueType = valueType;
        this.value = value;
    }

    public enum ConditionValueType {
        PERCENT,
        EXACT,
        ANY
    }


    public enum ConditionType {

        /**
         *超越
         */
        BEYOND,

        /**
         * 落后
         */
        BEHIND,
        /**
         * 顺势，如开多时，均线的顺势表示均线方向向上
         */
        FOLLOW,
        /**
         * 背离，如开多时，均线的背离表示均线方向向下
         */
        AGAINST,

        /**
         * 大于
         */
        GREAT_THAN,

        /**
         * 小于
         */
        LESS_THAN;
    }
}

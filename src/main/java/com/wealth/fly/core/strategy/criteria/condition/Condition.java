package com.wealth.fly.core.strategy.criteria.condition;

import lombok.Data;

@Data
public class Condition {

    private ConditionType type;
    private ConditionValueType valueType;
    private String value;

    public Condition() {
    }

    public Condition(ConditionType type, ConditionValueType valueType, String value) {
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
         * 超越
         */
        BEYOND(new BeyondConditionHandler()),

        /**
         * 落后
         */
        BEHIND(new BehindConditionHandler()),
        /**
         * 顺势，如开多时，均线的顺势表示均线方向向上
         */
        FOLLOW(new FollowConditionHandler()),
        /**
         * 背离，如开多时，均线的背离表示均线方向向下
         */
        AGAINST(null),

        /**
         * 大于
         */
        GREAT_THAN(new GreatThanConditionHandler()),

        /**
         * 小于
         */
        LESS_THAN(new LessThanConditionHandler());

        private ConditionHandler conditionHandler;

        ConditionType(ConditionHandler handler) {
            this.conditionHandler = handler;
        }

        public ConditionHandler getConditionHandler() {
            return this.conditionHandler;
        }
    }
}

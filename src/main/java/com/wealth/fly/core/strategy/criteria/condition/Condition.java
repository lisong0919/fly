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

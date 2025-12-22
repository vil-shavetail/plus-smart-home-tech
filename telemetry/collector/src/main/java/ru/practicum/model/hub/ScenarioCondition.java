package ru.practicum.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.model.hub.enums.ActionType;
import ru.practicum.model.hub.enums.ConditionOperation;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    private String sensorId;
    private ActionType type;
    private ConditionOperation operation;
    private int value;
}

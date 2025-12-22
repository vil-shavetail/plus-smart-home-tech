package ru.practicum.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.model.hub.enums.DeviceType;
import ru.practicum.model.hub.enums.HubEventType;

@Getter
@Setter
@ToString
public class DeviceAddedEvent extends HubEvent{
    private String id;
    private DeviceType type;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED_EVENT;
    }
}

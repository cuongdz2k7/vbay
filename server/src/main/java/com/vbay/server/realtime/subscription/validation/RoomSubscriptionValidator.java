package com.vbay.server.realtime.subscription.validation;

import java.util.List;

import com.vbay.server.network_connection.ClientSession;
import com.vbay.shared.dto.realtimeDTO.Room;

///Validator gom lại hết rules và check cho Room tổng quát luôn
public class RoomSubscriptionValidator {
    private final List<RoomSubscriptionRule> rules;

    public RoomSubscriptionValidator(List<RoomSubscriptionRule> rules) {
        this.rules = rules;
    }

    public void validate(Room room, ClientSession session) {
        for (RoomSubscriptionRule rule : rules) {
            if (rule.support(room)) {
                rule.validate(room, session);
            }
        }
    }

}

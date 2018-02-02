package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Lifter {
    TalonSRX left;
    TalonSRX right;
    public Lifter(TalonSRX left, TalonSRX right) {
        this.left = left;
        left.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 0);
        left.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 0);
        left.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);

        this.right = right;
        right.configForwardLimitSwitchSource(RemoteLimitSwitchSource.RemoteCANifier, LimitSwitchNormal.NormallyOpen, left.getDeviceID(), 0);
        right.configReverseLimitSwitchSource(RemoteLimitSwitchSource.RemoteCANifier, LimitSwitchNormal.NormallyOpen, left.getDeviceID(), 0);
        left.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
        right.follow(left);
    }

    public void setLevel(Level height) {
        left.set(ControlMode.Position, height.setPos);
    }

    public enum Level {
        GROUND(0), SWITCH(5), SCALE(10);
        int setPos;
        Level(int setPos) {
            this.setPos = setPos;
        }
    }
}

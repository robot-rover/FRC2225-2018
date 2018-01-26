package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.RemoteLimitSwitchSource;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Lifter {
    TalonSRX left;
    TalonSRX right;
    public Lifter(TalonSRX left, TalonSRX right) {
        this.left = left;
        left.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 0);
        left.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 0);

        this.right = right;
        right.configForwardLimitSwitchSource(RemoteLimitSwitchSource.RemoteCANifier, LimitSwitchNormal.NormallyOpen, left.getDeviceID(), 0);
        right.configReverseLimitSwitchSource(RemoteLimitSwitchSource.RemoteCANifier, LimitSwitchNormal.NormallyOpen, left.getDeviceID(), 0);
    }
}

package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class Sucker {
    TalonSRX left;
    TalonSRX right;
    public Sucker(TalonSRX left, TalonSRX right) {
        this.left = left;
        this.right = right;
        right.follow(left);
    }

    public void Suck(boolean on) {
        right.set(ControlMode.PercentOutput, on ? 1 : 0);
    }
}

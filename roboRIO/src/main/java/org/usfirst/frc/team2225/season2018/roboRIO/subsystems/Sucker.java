package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.command.Subsystem;

public class Sucker extends Subsystem {
    VictorSP left;
    VictorSP right;
    public Sucker(VictorSP left, VictorSP right) {
        this.left = left;
        this.right = right;
    }

    public void suck(double speed) {
        right.set(speed); left.set(-speed);
    }

    public void turn(double speed) {
        right.set(speed); left.set(-speed);
    }

    @Override
    protected void initDefaultCommand() {

    }
}

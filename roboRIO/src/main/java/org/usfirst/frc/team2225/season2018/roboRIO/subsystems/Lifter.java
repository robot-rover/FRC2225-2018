package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Lifter extends Subsystem {
    public TalonSRX left;
    public TalonSRX right;
    public Lifter(TalonSRX left, TalonSRX right) {
        this.left = left;
        /*left.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 0);
        left.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, 0);*/
        left.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
        left.setSelectedSensorPosition(0, 0, 0);
        /*left.configForwardSoftLimitThreshold(945, 0);
        left.configReverseSoftLimitThreshold(0, 0);
        */
        left.configForwardSoftLimitEnable(false, 0);
        left.configReverseSoftLimitEnable(false, 0);
        left.setNeutralMode(NeutralMode.Brake);

        this.right = right;
        //left.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
        right.set(ControlMode.Follower, left.getDeviceID());
        right.setInverted(true);
        right.setNeutralMode(NeutralMode.Brake);

        double p = 0.8;
        double i = 0.001;
        double nominal = 0.3;
        double d = 40;
        double err = 10;
        left.config_kP(0, p, 0);
        //left.config_kI(0, i, 0);
        left.config_kD(0, d,0);
        //left.config_IntegralZone(0, 50, 0);
        left.configAllowableClosedloopError(0, (int) err, 0);
        left.configNominalOutputReverse(-nominal, 0);
        left.configNominalOutputForward(nominal, 0);
    }

    public void setLevel(Level height) {
        //left.set(ControlMode.Position, height.setPos);
    }

    @Override
    protected void initDefaultCommand() {

    }

    @Override
    public void periodic() {
        SmartDashboard.putNumber("Lifter Pos", left.getSelectedSensorPosition(0));

    }

    public enum Level {
        GROUND(10), SWITCH(300), SCALE(940);
        int setPos;
        Level(int setPos) {
            this.setPos = setPos;
        }
    }

    final double deadzone = 0.0001;
    int pos = 0;
    public void move(double speed) {
        if(Math.abs(speed) < deadzone) {
            left.set(ControlMode.Position, pos);
            //DriverStation.reportWarning("Controlling with Position: " + left.getSelectedSensorPosition(0), false);
        }
        else {
            pos = left.getSelectedSensorPosition(0);
            left.set(ControlMode.PercentOutput, speed);
            //DriverStation.reportWarning("Controlling with Percent: " + speed, false);
        }
    }
}

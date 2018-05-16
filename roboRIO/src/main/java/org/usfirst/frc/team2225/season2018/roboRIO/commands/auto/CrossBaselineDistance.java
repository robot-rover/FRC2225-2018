package org.usfirst.frc.team2225.season2018.roboRIO.commands.auto;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain;

public class CrossBaselineDistance extends Command {
    public CrossBaselineDistance() {
        requires(RoboRIOMain.drivetrain);
    }

    long startTime;
    double target;
    double targetRot;
    @Override
    protected void initialize() {
        target = Drivetrain.cmToCounts((new Vector2D(0, 330)).dot(Drivetrain.backLeftVec)) + RoboRIOMain.drivetrain.backLeft.getSelectedSensorPosition(0);
        SmartDashboard.putNumber("AllowableError", PlaceCube.allowableClosedLoopError);
        targetRot = RoboRIOMain.drivetrain.gyro.getAngle();
    }

    @Override
    protected void execute() {
        SmartDashboard.putNumber("Drivetrain Error", RoboRIOMain.drivetrain.getAverageError());

        final double p = 1.0/150.0;
        final double d = 1.0/400.0;

        double pTerm = (RoboRIOMain.drivetrain.gyro.getAngle() - targetRot) * p;
        double dTerm = RoboRIOMain.drivetrain.gyro.getRate() * d;
        dTerm = Math.copySign(Math.max(0, Math.abs(dTerm) - 0.1), dTerm);
        double rotate = pTerm + dTerm;
        rotate = Math.max(-0.5, Math.min(rotate, 0.5));
        RoboRIOMain.drivetrain.omniDrive(new Vector2D(0, 0.5), rotate);
    }

    @Override
    protected void end() {
        DriverStation.reportWarning("Done", false);
        RoboRIOMain.drivetrain.tankDrive(0,0);
    }

    @Override
    protected boolean isFinished() {
        return RoboRIOMain.drivetrain.backLeft.getSelectedSensorPosition(0) > target;
    }

}

package org.usfirst.frc.team2225.season2018.roboRIO.commands.auto;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain;

public class PlaceCube extends Command {
    int switchStage;
    boolean isFinished;
    double sideSign = 1;
    Long timeNextStep = null;

    /**
     * Left is negative sidesign
     * @param sideSign which way to drive
     */
    public PlaceCube(int sideSign) {
        requires(RoboRIOMain.drivetrain);
        switchStage = 0;
        isFinished = false;
        this.sideSign = sideSign;
    }

    @Override
    protected void initialize() {
        switchStage = 0;
        nextObjective();
    }

    final static double allowableClosedLoopError = Drivetrain.cmToCounts(8);

    @Override
    protected void execute() {
        SmartDashboard.putNumber("Auto Stage", switchStage-1);
        SmartDashboard.putNumber("Drivetrain Error", RoboRIOMain.drivetrain.getAverageError());
        if (timeNextStep != null) {
            if (System.currentTimeMillis() > timeNextStep) {
                nextObjective();
                timeNextStep = null;

            }
        } else if (RoboRIOMain.drivetrain.getAverageError() < allowableClosedLoopError)
            nextObjective();
    }

    private void nextObjective() {
        switch (switchStage) {
            case 0:
                RoboRIOMain.drivetrain.omniDistance(new Vector2D(0, 10));
                break;
            case 1:
                RoboRIOMain.drivetrain.omniDistance(new Vector2D(0, -10));
                break;
            case 2:
                timeNextStep = System.currentTimeMillis() + 500;
                RoboRIOMain.lifter.move(0.5);
                break;
            case 3:
                RoboRIOMain.lifter.move(0);
                RoboRIOMain.drivetrain.omniDistance(new Vector2D(sideSign * 426.72, 0));
                break;
            case 4:
                RoboRIOMain.sucker.suck(-1);
                break;
            default:
                isFinished = true;
                break;
        }
        switchStage++;
    }

    @Override
    protected void end() {
        super.end();
    }

    @Override
    protected boolean isFinished() {
        return isFinished;
    }
}

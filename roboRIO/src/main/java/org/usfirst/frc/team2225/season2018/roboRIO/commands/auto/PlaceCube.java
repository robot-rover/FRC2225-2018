package org.usfirst.frc.team2225.season2018.roboRIO.commands.auto;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain;

import java.util.function.IntConsumer;

public class PlaceCube extends Command {
    /** The stage of the State Machine */
    int switchStage;

    /** Is the Autonomous Finished */
    boolean isFinished;

    /** Positive goes Right, Negative Left */
    double sideSign = 1;

    /** If non-null: moves to next state after <i>timeNextStep</i> ms */
    Long timeNextStep = null;

    /** If non-null: moves to next state after the encoder moves <i>encoderNextStep</i> encoder counts */
    Double encoderNextStep = null;

    /** If non-null: moves to next state after the gyro is past <i>gyroNextStep</i> degrees */
    Double gyroNextStep = null;

    /** Tells the continue condition if it is waiting for the actual value to be greater (true) or less (false) than the expected value */
    boolean directionForward;

    IntConsumer drivetrainRefresh;

    /**
     * Left is negative sidesign
     * @param sideSign which way to drive
     */
    public PlaceCube(int sideSign) {
        requires(RoboRIOMain.drivetrain);
        this.sideSign = sideSign;
    }

    /**
     * Helper Function for the position of the Robot
     * @return the position in encoder counts
     */
    protected double currPos() {
        return RoboRIOMain.drivetrain.backLeft.getSelectedSensorPosition(0);
    }

    /** State machine can move on if actual and expected value are within <i>allowableClosedLoopError</i> */
    final static double allowableClosedLoopError = Drivetrain.cmToCounts(10);

    /**
     * This command is called every time we start autonomous
     * Sets up variables for starting state, then starts first task
     */
    @Override
    protected void initialize() {
        isFinished = false;
        switchStage = 0;
        timeNextStep = null;
        encoderNextStep = null;
        gyroNextStep = null;
        nextObjective();
    }

    /**
     * Main Loop of State Machine
     * Checks if any continue conditions are active
     * if yes, checks if they are fulfilled and continues the state machine
     */
    @Override
    protected void execute() {
        SmartDashboard.putNumber("Auto Stage", switchStage-1);
//        SmartDashboard.putNumber("Drivetrain Error", RoboRIOMain.drivetrain.getAverageError());
        if (timeNextStep != null) {
            if (System.currentTimeMillis() > timeNextStep) {
                timeNextStep = null;
                drivetrainRefresh = null;
                DriverStation.reportWarning("advanced by time", false);
                nextObjective();
            }
        } else if (encoderNextStep != null) {
            if (directionForward ? RoboRIOMain.drivetrain.backLeft.getSelectedSensorPosition(0) > encoderNextStep : RoboRIOMain.drivetrain.backLeft.getSelectedSensorPosition(0) < encoderNextStep) {
                encoderNextStep = null;
                drivetrainRefresh = null;
                DriverStation.reportWarning("advanced by encoder", false);
                nextObjective();
            }
        } else if (gyroNextStep != null) {
            if(Math.abs(RoboRIOMain.drivetrain.gyro.getAngle() - gyroNextStep) < 5) {
                gyroNextStep = null;
                drivetrainRefresh = null;
                DriverStation.reportWarning("advanced by gyro", false);
                nextObjective();
            }
        }
        if(drivetrainRefresh != null) {
            drivetrainRefresh.accept(0);
        }
    }

    /**
     * List of Targets and Commands for State Machine
     */
    private void nextObjective() {
        switch (switchStage) {
            // Step 0-1 nudge the sucker into the deployed position by moving forward then back
            case 0:
                drivetrainRefresh = null;
                RoboRIOMain.drivetrain.omniDrive(new Vector2D(0, 0.5), 0);
                encoderNextStep = Drivetrain.cmToCounts(10) + currPos();
                break;
            case 1:
                RoboRIOMain.drivetrain.omniDrive(new Vector2D(0, -0.5), 0);
                encoderNextStep = currPos() - Drivetrain.cmToCounts(10);
                break;
            // Tank drive stops the robot from moving
            // This Task also lifts the sucker
            case 2:
                RoboRIOMain.drivetrain.tankDrive(0, 0);
                RoboRIOMain.lifter.move(0.6);
                timeNextStep = System.currentTimeMillis() + 1400;
                break;
            // Setting the lifter to 0.4 then to 0 makes sure the PID holds in the up position
            // This Step decides whether to continue after moving sideways to face towards the outside edges of the switch
            // If it is on the incorrect side, it sets the state machine to step 100, halting execution after the current task is completed
            case 3:
                RoboRIOMain.lifter.move(0.4);
                RoboRIOMain.lifter.move(0);
                encoderNextStep = Drivetrain.cmToCounts(new Vector2D(sideSign * 380.72, 0).dot(Drivetrain.backLeftVec)) + currPos();
                final double target = RoboRIOMain.drivetrain.gyro.getAngle();
                drivetrainRefresh = (i) -> RoboRIOMain.drivetrain.omniDriveGyroTarget(new Vector2D(sideSign * 0.5, 0), target);
                // The game specific message is a sequence of three letters, each one either L or R
                // The first letter tells you which side of the close switch is your color, from the perspective of your Driver Station
                // The second likewise for the scale
                // The third likewise for the opposing switch
                String sides = DriverStation.getInstance().getGameSpecificMessage();
                DriverStation.reportWarning("Game Message: " + (sides == null ? "null" : sides), false);
                if(sides != null && sides.length() > 0)
                    if(sides.substring(0, 1).equals((sideSign > 0 ? "L" : "R"))) {
                        switchStage = 100;
                        DriverStation.reportWarning("Stopping because of Sides", false);
                        break;
                    }
                break;
            // Then drives forward for 2 seconds (No gyro so the side of the switch can straighten us out)
            case 4:
                RoboRIOMain.drivetrain.tankDrive(0, 0);
                timeNextStep = System.currentTimeMillis() + 2000;
                final double target2 = RoboRIOMain.drivetrain.gyro.getAngle();
                drivetrainRefresh = (i) -> RoboRIOMain.drivetrain.omniDriveGyroTarget(new Vector2D(0, 0.5), target2);
                break;
            // Spit out the cube
            case 5:
                RoboRIOMain.drivetrain.tankDrive(0,0);
                timeNextStep = System.currentTimeMillis() + 500;
                RoboRIOMain.sucker.suck(-1);
                break;
            default:
                RoboRIOMain.sucker.suck(0);
                isFinished = true;
                break;
        }
        // Sets the directionForward correctly so the continue condition can go past and still complete
        if(encoderNextStep != null) {
            if(encoderNextStep > currPos())
                directionForward = true;
            else
                directionForward = false;
        }
        DriverStation.reportWarning("Entering Stage " + switchStage, false);
        switchStage++;
    }

    @Override
    protected void end() {
        super.end();
    }

    /** Tells the Command Framework this command has finished executing */
    @Override
    protected boolean isFinished() {
        return isFinished;
    }
}

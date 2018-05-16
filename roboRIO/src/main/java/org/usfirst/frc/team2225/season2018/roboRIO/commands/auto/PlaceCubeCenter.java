package org.usfirst.frc.team2225.season2018.roboRIO.commands.auto;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2225.season2018.roboRIO.RoboRIOMain;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain;

public class PlaceCubeCenter extends Command {
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

    public PlaceCubeCenter() {
        requires(RoboRIOMain.drivetrain);
    }

    /**
     * Helper Function for the position of the Robot
     * @return the position in encoder counts
     */
    protected double currPos() {
        return RoboRIOMain.drivetrain.backLeft.getSelectedSensorPosition(0);
    }

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

    /** State machine can move on if actual and expected value are within <i>allowableClosedLoopError</i> */
    final static double allowableClosedLoopError = Drivetrain.cmToCounts(10);

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
                DriverStation.reportWarning("advanced by time", false);
                nextObjective();
            }
        } else if (encoderNextStep != null) {
            if (directionForward ? RoboRIOMain.drivetrain.backLeft.getSelectedSensorPosition(0) > encoderNextStep : RoboRIOMain.drivetrain.backLeft.getSelectedSensorPosition(0) < encoderNextStep) {
                encoderNextStep = null;
                DriverStation.reportWarning("advanced by encoder", false);
                nextObjective();
            }
        } else if (gyroNextStep != null) {
            if(Math.abs(RoboRIOMain.drivetrain.gyro.getAngle() - gyroNextStep) < 5) {
                gyroNextStep = null;
                DriverStation.reportWarning("advanced by gyro", false);
            }
        }
    }

    /**
     * List of Targets and Commands for State Machine
     */
    private void nextObjective() {
        switch (switchStage) {
            // Step 0-1 nudge the sucker into the deployed position by moving forward then back
            // Forward is 10cm farther in order to avoid the lip of the exchange portal
            case 0:
                RoboRIOMain.drivetrain.omniDrive(new Vector2D(0, 0.5), 0);
                encoderNextStep = Drivetrain.cmToCounts(20) + currPos();
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
            // This Step decides which direction to translate
            // Moves either left or right to face away from the driver station toward the correct switch "bin"
            case 3:
                RoboRIOMain.lifter.move(0.4);
                RoboRIOMain.lifter.move(0);
                // The game specific message is a sequence of three letters, each one either L or R
                // The first letter tells you which side of the close switch is your color, from the perspective of your Driver Station
                // The second likewise for the scale
                // The third likewise for the opposing switch
                String sides = DriverStation.getInstance().getGameSpecificMessage();
                DriverStation.reportWarning("Game Message: " + (sides == null ? "null" : sides), false);
                if(sides != null && sides.length() > 0)
                    if(sides.substring(0, 1).equals("R")) {
                        DriverStation.reportWarning("Moving Right", false);
                        sideSign = 1;
                    } else {
                        DriverStation.reportWarning("Moving Left", false);
                        sideSign = -1;
                    }
                encoderNextStep = Drivetrain.cmToCounts(new Vector2D(sideSign * 132.08, 0).dot(Drivetrain.backLeftVec)) + currPos();
                RoboRIOMain.drivetrain.omniDriveGyroTarget(new Vector2D(sideSign * 0.5, 0), RoboRIOMain.drivetrain.gyro.getAngle());
                break;
            // Drive forward for 5 seconds (Use the edge of the switch to square ourselves)
            case 4:
                RoboRIOMain.drivetrain.tankDrive(0, 0);
                timeNextStep = System.currentTimeMillis() + 5000;
                RoboRIOMain.drivetrain.omniDriveGyroTarget(new Vector2D(0, 0.5), RoboRIOMain.drivetrain.gyro.getAngle());
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

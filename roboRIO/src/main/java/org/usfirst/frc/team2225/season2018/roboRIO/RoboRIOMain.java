package org.usfirst.frc.team2225.season2018.roboRIO;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usfirst.frc.team2225.season2018.roboRIO.commands.Teleop;
import org.usfirst.frc.team2225.season2018.roboRIO.commands.auto.CrossBaseline;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.DriverInput;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Drivetrain;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Lifter;
import org.usfirst.frc.team2225.season2018.roboRIO.subsystems.Sucker;

public class RoboRIOMain extends IterativeRobot {
    public static Drivetrain drivetrain;
    public static DriverInput driverInput;
    public static Sucker sucker;
    public static Lifter lifter;
    private static Logger log = LoggerFactory.getLogger(RoboRIOMain.class);
    Command autonomousCommand;
    SendableChooser<Command> chooser = new SendableChooser<>();

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {

        drivetrain = new Drivetrain(
                new TalonSRX(Bindings.frontLeftTalon),
                new TalonSRX(Bindings.frontRightTalon),
                new TalonSRX(Bindings.backLeftTalon),
                new TalonSRX(Bindings.backRightTalon),
                new ADXRS450_Gyro()
        );
        lifter = new Lifter(
                new TalonSRX(Bindings.leftLifterTalon),
                new TalonSRX(Bindings.rightLifterTalon)
        );
        sucker = new Sucker(
                new VictorSP(Bindings.leftSuckerTalon),
                new VictorSP(Bindings.rightSuckerTalon)
        );
        driverInput = new DriverInput();
        chooser.addDefault("Default", null);
        chooser.addObject("Cross Baseline", new CrossBaseline());
        SmartDashboard.putData("Auto mode", chooser);
        SmartDashboard.putNumber("P", 0);
        SmartDashboard.putNumber("I", 0);
        SmartDashboard.putNumber("I WindUp", 0);
        SmartDashboard.putNumber("err", 0);
        SmartDashboard.putNumber("D", 0);
    }

    /**
     * This function is called once each time the robot enters Disabled mode.
     * You can use it to reset any subsystem information you want to clear when
     * the robot is disabled.
     */
    @Override
    public void disabledInit() {

    }

    @Override
    public void disabledPeriodic() {
        Scheduler.getInstance().run();
        /*SmartDashboard.putNumber("Motor Position", drivetrain.frontLeft.getSelectedSensorPosition(0));
        SmartDashboard.putNumber("Motor Velocity", drivetrain.frontLeft.getSelectedSensorVelocity(0));*/
        //DriverStation.reportWarning("Joy Out: " + driverInput.getJoy().getY(GenericHID.Hand.kLeft) + ", " + driverInput.getJoy().getX(GenericHID.Hand.kLeft), false);
    }

    /**
     * This autonomous (along with the chooser code above) shows how to select
     * between different autonomous modes using the dashboard. The sendable
     * chooser code works with the Java SmartDashboard. If you prefer the
     * LabVIEW Dashboard, remove all of the chooser code and uncomment the
     * getString code to get the auto name from the text box below the Gyro
     * <p>
     * You can add additional auto modes by adding additional commands to the
     * chooser code above (like the commented example) or additional comparisons
     * to the switch structure below with additional strings & commands.
     */
    @Override
    public void autonomousInit() {
        autonomousCommand = chooser.getSelected();

        // schedule the autonomous command (example)
        if (autonomousCommand != null)
            autonomousCommand.start();
        else
            DriverStation.reportError("No Autonomous Selected", false);
    }

    /**
     * This function is called periodically during autonomous
     */
    @Override
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
    }

    @Override
    public void teleopInit() {
		/* This makes sure that the autonomous stops running when
        teleop starts running. If you want the autonomous to
        continue until interrupted by another command, remove
        this line or comment it out. */
        if (autonomousCommand != null)
            autonomousCommand.cancel();
        drivetrain.frontLeft.setSelectedSensorPosition(0, 0, 0);
        drivetrain.frontRight.setSelectedSensorPosition(0, 0, 0);
        drivetrain.backLeft.setSelectedSensorPosition(0, 0, 0);
        drivetrain.backRight.setSelectedSensorPosition(0, 0, 0);
        drivetrain.reset();
        /*double p = SmartDashboard.getNumber("P", 0);
        double i = SmartDashboard.getNumber("I", 0);
        double iWind = SmartDashboard.getNumber("I WindUp", 0);
        double d = SmartDashboard.getNumber("D", 0);
        double err = SmartDashboard.getNumber("err", 1);
        lifter.left.setSelectedSensorPosition(0, 0,0);
        lifter.right.setSelectedSensorPosition(0,0,0);
        lifter.left.config_kP(0, p, 0);
        lifter.left.config_kI(0, i, 0);
        lifter.left.config_kD(0, d,0);
        lifter.left.configAllowableClosedloopError(0, (int) err, 0);
        lifter.left.configNominalOutputReverse(-iWind, 0);
        lifter.left.configNominalOutputForward(iWind, 0);
        lifter.right.config_kP(0, p, 0);
        lifter.right.config_kI(0, i, 0);
        lifter.right.config_kD(0, d,0);
        lifter.right.configNominalOutputReverse(-iWind, 0);
        lifter.right.configNominalOutputForward(iWind, 0);*/
    }

    /**
     * This function is called periodically during operator control
     */
    @Override
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
    }

    /**
     * This function is called periodically during test mode
     */
    @Override
    public void testPeriodic() {

    }
}
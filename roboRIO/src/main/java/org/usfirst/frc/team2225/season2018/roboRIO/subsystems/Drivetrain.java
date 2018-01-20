package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;
import org.usfirst.frc.team2225.season2018.roboRIO.commands.OmniDrive;

public class Drivetrain extends Subsystem {
    static final Vector2D frontLeftVec = new Vector2D(Math.sqrt(2) / 2, Math.sqrt(2) / 2);
    static final Vector2D frontRightVec = new Vector2D(-Math.sqrt(2) / 2, Math.sqrt(2) / 2);
    static final Vector2D backLeftVec = new Vector2D(-Math.sqrt(2) / 2, Math.sqrt(2) / 2);
    static final Vector2D backRightVec = new Vector2D(Math.sqrt(2) / 2, Math.sqrt(2) / 2);
    public TalonSRX frontLeft;
    public TalonSRX frontRight;
    public TalonSRX backLeft;
    public TalonSRX backRight;

    /**
     * Contructs an Omnidrive object
     *
     * @param frontLeft  The motor controller in the front left
     * @param frontRight The motor controller in the front right
     * @param backLeft   The motor controller in the back left
     * @param backRight  The motor controller in the back right
     */
    public Drivetrain(TalonSRX frontLeft, TalonSRX frontRight, TalonSRX backLeft, TalonSRX backRight) {
        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.backLeft = backLeft;
        backLeft.setInverted(true);
        this.backRight = backRight;
        frontLeft.setInverted(true);
        for(TalonSRX motor : new TalonSRX[]{frontLeft, frontRight, backLeft, backRight}) {
            motor.setNeutralMode(NeutralMode.Brake);
            motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
            motor.configNominalOutputForward(0.2, 0);
            motor.configNominalOutputReverse(0.2, 0);
            motor.configPeakOutputForward(1, 0);
            motor.configPeakOutputReverse(1, 0);
            motor.config_kP(0, 0.5, 0);
            motor.config_kI(0, 0, 0);
            motor.config_kD(0, 5, 0);
        }
    }

    @Override
    public void periodic() {
        SmartDashboard.putNumber("Motor Position", frontLeft.getSelectedSensorPosition(0));
        SmartDashboard.putNumber("Motor Velocity", frontLeft.getSelectedSensorVelocity(0));
    }

    public static double padValue(double pad, double value, boolean includePad) {
        double sign = Math.signum(value);
        return sign * ((1 - pad) * Math.abs(value) + (includePad ? pad : 0));
    }

    @Override
    protected void initDefaultCommand() {
        setDefaultCommand(new OmniDrive());
    }

    /**
     * A simple control scheme where you give an input for left and right sides.
     * It is similar to driving a tank, thus its name.
     *
     * @param left  The left input
     * @param right The right input
     */
    public void tankDrive(double left, double right) {
        setMotorVoltage(left, right, left, right);
    }

    /**
     * A slightly more complex control scheme where you give a throttle and turn input
     * It is similar to driving an rc car.
     *
     * @param throttle The throttle input
     * @param turn     The turning input
     */
    public void arcadeDrive(double throttle, double turn) {
        Vector2D map = new Vector2D(turn, throttle).mapSquareToDiamond();
        double left = map.y, right = map.y;
        left += map.x;
        right -= map.x;
        setMotorVoltage(left, right, left, right);
    }

    /**
     * Sets the output levels for all drive motors
     *
     * @param fl The output level for the front left motor
     * @param fr The output level for the front right motor
     * @param bl The output level for the back left motor
     * @param br The output level for the back right motor
     */
    public void setMotorVoltage(double fl, double fr, double bl, double br) {
        frontLeft.set(ControlMode.PercentOutput, fl);
        frontRight.set(ControlMode.PercentOutput, fr);
        backLeft.set(ControlMode.PercentOutput, bl);
        backRight.set(ControlMode.PercentOutput, br);
    }

    public void setMotorSpeed(double fl, double fr, double bl, double br) {
        frontLeft.set(ControlMode.Velocity, fl);
        frontRight.set(ControlMode.Velocity, fr);
        backLeft.set(ControlMode.Velocity, bl);
        backRight.set(ControlMode.Velocity, br);
    }

    static final int encoderUnitConversion = 0;
    public int getPosition(TalonSRX motor) {

        return motor.getSelectedSensorPosition(0);
    }

    /**
     * A complex drive scheme utilizing omniwheels to give the robot 3 degrees of freedom
     * It can move in 2D space and rotate
     *
     * @param translate A vector representing the desired movement on a plane
     * @param rotate    The amount of rotation desired (Positive is counter-clockwise)
     */
    public void omniDrive(Vector2D translate, double rotate) {
        SmartDashboard.putNumber("Translate X", translate.x);
        SmartDashboard.putNumber("Translate Y", translate.y);
        SmartDashboard.putNumber("Rotate", rotate);
        translate.mapSquareToDiamond().divide(Math.sqrt(2) / 2);
        double fr, fl, br, bl;
        fl = translate.dot(frontLeftVec);
        fr = translate.dot(frontRightVec);
        bl = translate.dot(backLeftVec);
        br = translate.dot(backRightVec);
        SmartDashboard.putNumber("FL", fl);
        SmartDashboard.putNumber("FR", fr);
        SmartDashboard.putNumber("BL", bl);
        SmartDashboard.putNumber("BR", br);
        if (rotate != 0) {
            fr = padValue(rotate, fr, false) + rotate;
            br = padValue(rotate, br, false) + rotate;
            fl = padValue(rotate, fl, false) - rotate;
            bl = padValue(rotate, bl, false) - rotate;
        }
        setMotorVoltage(fl, fr, bl ,br);
    }

    public void reset() {
        setMotorVoltage(0, 0, 0, 0);
    }
}

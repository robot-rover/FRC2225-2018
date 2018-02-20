package org.usfirst.frc.team2225.season2018.roboRIO.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2225.season2018.roboRIO.Vector2D;
import org.usfirst.frc.team2225.season2018.roboRIO.commands.Teleop;

public class Drivetrain extends Subsystem {
    static final Vector2D frontLeftVec = new Vector2D(Math.sqrt(2) / 2, Math.sqrt(2) / 2);
    static final Vector2D frontRightVec = new Vector2D(-Math.sqrt(2) / 2, Math.sqrt(2) / 2);
    static final Vector2D backLeftVec = new Vector2D(-Math.sqrt(2) / 2, Math.sqrt(2) / 2);
    static final Vector2D backRightVec = new Vector2D(Math.sqrt(2) / 2, Math.sqrt(2) / 2);
    public TalonSRX frontLeft;
    public TalonSRX frontRight;
    public TalonSRX backLeft;
    public TalonSRX backRight;
    public ADXRS450_Gyro gyro;
    double targetRot;
    int resetTargetRot;

    /**
     * Contructs an Omnidrive object
     *
     * @param frontLeft  The motor controller in the front left
     * @param frontRight The motor controller in the front right
     * @param backLeft   The motor controller in the back left
     * @param backRight  The motor controller in the back right
     */
    public Drivetrain(TalonSRX frontLeft, TalonSRX frontRight, TalonSRX backLeft, TalonSRX backRight, ADXRS450_Gyro gyro) {
        this.gyro = gyro;
        gyro.reset();
        targetRot = 0;
        this.frontLeft = frontLeft;
        frontRight.setInverted(true);
        frontRight.setSensorPhase(true);
        this.frontRight = frontRight;
        this.backLeft = backLeft;
        backRight.setInverted(true);
        backRight.setSensorPhase(true);
        this.backRight = backRight;
        for(TalonSRX motor : new TalonSRX[]{frontLeft, frontRight, backLeft, backRight}) {
            motor.setNeutralMode(NeutralMode.Brake);
            motor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
            motor.configNominalOutputForward(0.0, 0);
            motor.configNominalOutputReverse(0.0, 0);
            motor.configPeakOutputForward(1, 0);
            motor.configPeakOutputReverse(-1, 0);
            motor.configClosedloopRamp(0.4, 0);
            motor.config_kP(0, 4, 0);
            motor.config_kI(0, 0, 0);
            motor.config_kD(0, 13, 0);
        }
    }

    @Override
    public void periodic() {
        SmartDashboard.putNumberArray("Gyro Reading", new double[]{gyro.getAngle(), gyro.getRate()});
        double fr, fl, br, bl;
        fl = frontLeft.getSelectedSensorPosition(0);
        fr = frontRight.getSelectedSensorPosition(0);
        bl = backLeft.getSelectedSensorPosition(0);
        br = backRight.getSelectedSensorPosition(0);
        SmartDashboard.putNumberArray("Motor Position", new double[]{fr, fl, br, bl});
        fl = frontLeft.getMotorOutputPercent();
        fr = frontRight.getMotorOutputPercent();
        bl = backLeft.getMotorOutputPercent();
        br = backRight.getMotorOutputPercent();
        SmartDashboard.putNumberArray("Motor Output", new double[]{fr, fl, br, bl});
        fl = frontLeft.getSelectedSensorVelocity(0);
        fr = frontRight.getSelectedSensorVelocity(0);
        bl = backLeft.getSelectedSensorVelocity(0);
        br = backRight.getSelectedSensorVelocity(0);
        SmartDashboard.putNumberArray("Motor Velocity", new double[]{fr, fl, br, bl});
    }

    public static double padValue(double pad, double value, boolean includePad) {
        double sign = Math.signum(value);
        return sign * ((1 - pad) * Math.abs(value) + (includePad ? pad : 0));
    }

    @Override
    protected void initDefaultCommand() {
        setDefaultCommand(new Teleop());
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
        reset();
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
        SmartDashboard.putNumberArray("Motor Outputs", new double[]{fl, fr, bl, br});

        frontLeft.set(ControlMode.PercentOutput, fl);
        frontRight.set(ControlMode.PercentOutput, fr);
        backLeft.set(ControlMode.PercentOutput, bl);
        backRight.set(ControlMode.PercentOutput, br);

    }

    static final int maxMotorSpeed = 600;

    public void setMotorVelocity(double fl, double fr, double bl, double br) {
        SmartDashboard.putNumberArray("Motor Outputs", new double[]{fl, fr, bl, br});
        frontLeft.set(ControlMode.Velocity, fl * maxMotorSpeed);
        frontRight.set(ControlMode.Velocity, fr * maxMotorSpeed);
        backLeft.set(ControlMode.Velocity, bl * maxMotorSpeed);
        backRight.set(ControlMode.Velocity, br * maxMotorSpeed);
    }

    final int countsPerMotorRotation = 80;
    final int motorRotationsPerWheelRotation = 16;
    final int wheelDiameterIn = 6;
    final double wheelDiameterCm = wheelDiameterIn * 2.54;
    final double wheelCircumferenceCm = wheelDiameterCm * Math.PI;

    public double cmToCounts(double cm) {
        return cm / wheelCircumferenceCm * motorRotationsPerWheelRotation * countsPerMotorRotation;
    }

    public double getAverageError() {
        double averageError = 0;
        for(TalonSRX motor : new TalonSRX[]{frontLeft, frontRight, backLeft, backRight}) {
            averageError += motor.getClosedLoopError(0);
        }
        averageError /= 4;
        return averageError;
    }

    public void setMotorPosition(double fl, double fr, double bl, double br) {
        fl = cmToCounts(fl);
        fr = cmToCounts(fr);
        bl = cmToCounts(bl);
        br = cmToCounts(br);
        SmartDashboard.putNumberArray("Motor Outputs", new double[]{fl, fr, bl, br});
        frontLeft.set(ControlMode.Position, fl);
        frontRight.set(ControlMode.Position, fr);
        backLeft.set(ControlMode.Position, bl);
        backRight.set(ControlMode.Position, br);
    }

    static final int encoderCountsPerRevolution = 1280;
    public double getRotations(TalonSRX motor) {
        return (double) motor.getSelectedSensorPosition(0) / encoderCountsPerRevolution;
    }

    /**
     * A complex drive scheme utilizing omniwheels to give the robot 3 degrees of freedom.
     * It can move in 2D space and rotate.
     *
     * @param translate A vector representing the desired movement on a plane
     * @param rotateIn  The amount of rotation desired (Positive is counter-clockwise)
     */
    public void omniDrive(Vector2D translate, double rotateIn) {
        final double p = 1.0/150.0;
        final double d = 1.0/400.0;
        translate.mapSquareToDiamond().divide(Math.sqrt(2) / 2);
        double fr, fl, br, bl;
        fl = translate.dot(frontLeftVec);
        fr = translate.dot(frontRightVec);
        bl = translate.dot(backLeftVec);
        br = translate.dot(backRightVec);

        double rotate = 0;
        if(rotateIn != 0) {
            resetTargetRot = 10;
            rotate = rotateIn;
        }
        if(resetTargetRot > 0) {
            targetRot = gyro.getAngle();
            resetTargetRot--;
        }
        if(rotateIn == 0) {
            double pTerm = resetTargetRot > 0 ? 0 : (gyro.getAngle() - targetRot) * p;
            double dTerm = gyro.getRate() * d;
            dTerm = Math.copySign(Math.max(0, Math.abs(dTerm) - 0.1), dTerm);
            rotate = pTerm + dTerm;
            rotate = Math.max(-1, Math.min(rotate, 1));
        }


        fr = padValue(rotate, fr, false) + rotate;
        br = padValue(rotate, br, false) + rotate;
        fl = padValue(rotate, fl, false) - rotate;
        bl = padValue(rotate, bl, false) - rotate;
        setMotorVoltage(fl, fr, bl, br);
    }

    public void omniDistance(Vector2D translate) {
        double fr, fl, br, bl;
        fl = translate.dot(frontLeftVec);
        fr = translate.dot(frontRightVec);
        bl = translate.dot(backLeftVec);
        br = translate.dot(backRightVec);


    }

    public void omniRotate(double rotateIn) {

    }

    public void reset() {
        setMotorVoltage(0, 0, 0, 0);
        gyro.reset();
        targetRot = 0;
    }
}

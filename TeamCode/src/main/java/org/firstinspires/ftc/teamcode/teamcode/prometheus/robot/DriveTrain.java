package org.firstinspires.ftc.teamcode.teamcode.prometheus.robot;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.lib.Angle;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.lib.MotionProfile;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.lib.Pos;

public class DriveTrain {

    public DcMotor frontRight;
    public DcMotor frontLeft;
    public DcMotor backRight;
    public DcMotor backLeft;

    public DcMotor[] motors;

    private OpMode opMode;

    //ZEROS
    public PIDF xPID = new PIDF(0,  0, 0, 0);
    public PIDF yPID = new PIDF(0,   0 , 0, 0);
    public PIDF rPID = new PIDF(0,  0, 0, 0);

//    public PIDF xPID = new PIDF(-0.012000000000000006  ,-1.225E-5,4.999999999999998E-4 , 0);
//     public PIDF yPID = new PIDF(-0.012000000000000006  ,-1.225E-5,4.999999999999998E-4 , 0);
//    public PIDF rPID = new PIDF(-0.0800000000000009 ,  -0.02000000000000012, 0.0039999999999999975 , 0);


    // 3/5/21 PID TUNING W/ LAKSHMI :) AND prathik
    //public PIDF xPID = new PIDF(-0.02200000000000001, -6.000000000005866E-4, 9.867933835512599E-20, 0);
    //public PIDF yPID = new PIDF(-0.02200000000000001, -6.000000000005866E-4, 9.867933835512599E-20, 0);
    //public PIDF rPID = new PIDF(0 ,   0 , 0, 0);

    //3/9/2021
    //public PIDF xPID = new PIDF(-0.013999999999999998, -6.000000000005866E-4, 9.867933835512599E-20, 0);
    //public PIDF yPID = new PIDF(-0.013999999999999998,   -6.000000000005866E-4 , 9.867933835512599E-20, 0);
    //public PIDF rPID = new PIDF(-0.05800000000000005,  -0.050300000000001, 0, 0);


    //Second TOurnmanet
    //public PIDF xPID = new PIDF(-0.012,-0.00300000000000058, -0.89999999999999E-5, 0);
    //public PIDF yPID = new PIDF(-0.012, -0.00300000000000058, -0.89999999999999E-5, 0);
    //public PIDF rPID = new PIDF(-0.012000000000000004 ,   -0.04460000000000001 , 7.000000000000008E-6, 0);

    //First Tournament
    //    public PIDF xPID = new PIDF(-0.03139999999999987 ,-0.0033500000000000058, -6.19999999999999E-5, 0);
    //    public PIDF yPID = new PIDF(-0.03139999999999987 , -0.0033500000000000058, -6.19999999999999E-5, 0);
    //    public PIDF rPID = new PIDF(-0.00599999999999955,   -0.09600000000000007, 1.1000000000000001E-5, 0);


    public TrackerWheels trackerWheels;



    //PID 2



    public DriveTrain(OpMode opMode) {
        this.opMode = opMode;

        updateConstants();

        frontRight = opMode.hardwareMap.get(DcMotor.class, "fr");
        frontLeft = opMode.hardwareMap.get(DcMotor.class, "fl");
        backRight = opMode.hardwareMap.get(DcMotor.class, "br");
        backLeft = opMode.hardwareMap.get(DcMotor.class, "bl");

        motors = new DcMotor[]{frontRight, frontLeft, backLeft, backRight};

        trackerWheels = new TrackerWheels(opMode);
    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior){
        for(DcMotor m : motors){
            m.setZeroPowerBehavior(behavior);
        }
    }

    public void fieldCentric(double y, double x, double r, Angle a){
        double nx = Math.cos(a.getRadians()) * x - Math.sin(a.getRadians()) * y;
        double ny = Math.cos(a.getRadians()) * y + Math.sin(a.getRadians()) * x;

        setFromAxis(ny, nx, r);
    }

    // Y is forward/back (forward is positive)
    // X is right/left (left is positive)
    public void setFromAxis(double y, double x, double r, double scale){
        setFromAxis(y * scale, x * scale, r * scale);
    }

    public void setFromAxis(double y, double x, double r){
        double[] values = new double[]{y + x + r, - y + x + r, - y - x + r, y - x + r};

        double max = 1.0;

        for(double num : values){
            if(Math.abs(num) > max){
                max = Math.abs(num);
            }
        }

        for(int i = 0; i < 4; i++) {
            motors[i].setPower(values[i] / max);
        }
    }

    public static double round(double n, int c){
        return Math.pow(10, c) * (Math.round(n / Math.pow(10, c)));
    }

    public void stop(){
        for(int i = 0; i < 4; i++) {
            motors[i].setPower(0);
        }
    }


    //Tracker Wheels
    public void resetTrackerWheels(){
        trackerWheels.reset(backLeft.getCurrentPosition(),frontLeft.getCurrentPosition(),frontRight.getCurrentPosition());
    }

    public void updateTrackerWheels(double seconds){
        trackerWheels.update(backLeft.getCurrentPosition(),frontLeft.getCurrentPosition(),frontRight.getCurrentPosition(), seconds);
    }


  /*  @Deprecated
    public void xPID(Pos vel, double targetspd, double angle, double targretAngle){
        double error = targetspd - vel.x;
        sumError += error;
        double output = targetspd * velP + sumError * velI;

        double angleError = targretAngle - angle;


        setFromAxis(output, 0, angleError * -0.03);

        opMode.telemetry.addData("Velocity X", vel.x);
        opMode.telemetry.addData("Target Speed", targetspd);
    }

    @Deprecated
    public void xPIDwRotation(Pos vel, double targetspd, double rotatePer){
        double error = targetspd - vel.x;
        sumError += error;
        double output = targetspd * velP + sumError * velI;

        setFromAxis(output, 0, output*rotatePer);

        opMode.telemetry.addData("Velocity X", vel.x);
        opMode.telemetry.addData("Target Speed", targetspd);
    }
*/
    public void updateConstants(){
        xPID.p = RobotConfig.xp;
        xPID.i = RobotConfig.xi;
        xPID.d = RobotConfig.xd;
        yPID.p = RobotConfig.yp;
        yPID.i = RobotConfig.yi;
        yPID.d = RobotConfig.yd;
        rPID.p = RobotConfig.rp;
        rPID.i = RobotConfig.ri;
        rPID.d = RobotConfig.rd;
    }



    public void updateMovement(Pos target, MotionProfile moveProfile, MotionProfile rotProfile, double time, boolean setMotors) {


        Pos delta = target.sub(trackerWheels.pos);

        //opMode.telemetry.addData("Delta", delta);

        double distance = delta.getDistance();
        double speed = trackerWheels.velocity.getDistance();

        double moveTargetSpeed = moveProfile.getTargetSpeed(distance, speed, 2);
        double rotTargetSpeed = rotProfile.getTargetSpeed(delta.angle.rad(), trackerWheels.velocity.angle.rad(), 2);

        Pos robotDelta = delta.rotate(trackerWheels.pos.angle.negative());

        //opMode.telemetry.addData("Robot Delta", robotDelta);

        Pos move = new Pos(moveTargetSpeed, 0, new Angle());
        move = move.rotate(robotDelta.translationAngle());

        opMode.telemetry.addData("X-Vel", trackerWheels.velocity.x);
        opMode.telemetry.addData("X-Tar", move.x);
        opMode.telemetry.addData("Y-Vel", trackerWheels.velocity.y);
        opMode.telemetry.addData("Y-Tar", move.y);
        opMode.telemetry.addData("R-Vel", trackerWheels.velocity.angle.rad());
        opMode.telemetry.addData("R-Tar", rotTargetSpeed);

        if (setMotors) {
            double x = xPID.update(trackerWheels.velocity.x, move.x, time);
            double y = yPID.update(trackerWheels.velocity.y, move.y, time);
            double r = rPID.update(trackerWheels.velocity.angle.rad(), rotTargetSpeed, time);

            /*if(Math.abs(robotDelta.x) < 1){
                x = 0;
            }

            if (Math.abs(robotDelta.y) < 1){
                y = 0;
            }

            if(Math.abs(robotDelta.angle.getDegrees()) < 2){
                r = 0;
            }*/

            opMode.telemetry.addData("X OUT", x);
            opMode.telemetry.addData("Y OUT", y);
            opMode.telemetry.addData("R OUT", r);

            setFromAxis(x, y, r);
        } else {
            setFromAxis(opMode.gamepad1.left_stick_y, opMode.gamepad1.left_stick_x, opMode.gamepad1.right_stick_x * 0.75);

            xPID.resetI();
            yPID.resetI();
            rPID.resetI();
        }
    }

    public void resetPID(){
        xPID.resetI();
        yPID.resetI();;
        rPID.resetI();
    }
}

package org.firstinspires.ftc.teamcode.teamcode.prometheus.opmodes;

import android.app.ActionBar;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.teamcode.prometheus.lib.Angle;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.lib.MotionProfile;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.lib.Pos;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.opmodes.auto.Autonomous;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.Camera;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.Collector;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.DriveTrain;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.SaveGetFile;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.Shooter;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.WobbleArm;
@TeleOp(name = "febTeleop")

public class Teleop extends LinearOpMode {
    DriveTrain dt;
    Shooter shooter;
    Collector collector;
    WobbleArm wobbleArm;
    Camera camera;
    boolean collectorOn = false;


    MotionProfile moveProfile = new MotionProfile(100, 80);
    MotionProfile rotProfile = new MotionProfile(3, 2);
    int shoot = 1;

    enum AutoShoot{
        Not,
        Prep,
        Shoot1,
        ShootDrive1,
        Shoot2,
        ShootDrive2,
        Shoot3,
    }


    ElapsedTime timer = new ElapsedTime();
    ElapsedTime loopTime = new ElapsedTime();
    ElapsedTime spam = new ElapsedTime();


    @Override
    public void runOpMode() throws InterruptedException {
        dt = new DriveTrain(this);
        dt.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        shooter = new Shooter(this);

        collector = new Collector(this);

        wobbleArm = new WobbleArm(this, true);

        camera = new Camera(this);

        Pos p = SaveGetFile.getPosition();
        dt.trackerWheels.pos = p;
        dt.trackerWheels.oldPos = p;

        waitForStart();

        camera.servoBack();
        wobbleArm.servoClose();
        shooter.shooterLiftMiddle();
        shooter.pusherBack();
        shooter.indexerDown();


        Pos target;

        AutoShoot action = AutoShoot.Not;

        timer.reset();
        loopTime.reset();

        double calAngle = 0;

        while (opModeIsActive() && !isStopRequested()) {

            telemetry.addData("Mag", wobbleArm.mag.isPressed());

            Pos t = new Pos(136, -8, new Angle()).sub(dt.trackerWheels.pos);

            Angle angle = t.translationAngle();

            if(action == AutoShoot.Not){
                //dt.setFromAxis(gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x * 0.75);



                if(gamepad1.dpad_left){
                }else{
                    dt.fieldCentric(gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x * 0.75, new Angle().setDegrees(dt.trackerWheels.pos.angle.getDegrees() - calAngle));
                    dt.resetPID();
                }

                telemetry.addData("Angle towards goal", angle);

                if(gamepad1.x){
                    dt.resetTrackerWheels();
                    dt.trackerWheels.pos = new Pos(66,-14, new Angle());

                    calAngle = dt.trackerWheels.pos.angle.getDegrees() + 90;


                    dt.trackerWheels.updateAngle();
                    dt.trackerWheels.robotAngle = new Angle();
                }
            }

            //Wobble Stuff
            if ((gamepad1.dpad_up || gamepad2.dpad_up) && wobbleArm.mag.isPressed() == false) {
                wobbleArm.wobbleArm.setPower(-1);
            }

            else if (gamepad1.dpad_down || gamepad2.dpad_down) {
                wobbleArm.wobbleArm.setPower(1);
            }

            else {
                wobbleArm.wobbleArm.setPower(0);
            }

            if (gamepad2.dpad_right) {
                wobbleArm.servoClose();
            }

            if (gamepad2.dpad_left) {
                wobbleArm.servoOpen();
            }


            //SHOOTER
            switch(shooter.state){

                case Collecting:
                    shooter.target = 0;

                    if(gamepad1.left_bumper || gamepad2.left_bumper){
                        shooter.state = Shooter.ShooterState.ActionPrep;
                    }

                    shooter.indexerDown();

                    if(spam.seconds() < .2){
                        shooter.pusherBack();
                    }else if(spam.seconds() < .4){
                        shooter.pusherOut();
                    }else{
                        spam.reset();
                    }

                    break;
                case ActionPrep:

                    collectorOn = false;
                    collector.collector.setPower(0);

                    shooter.pusherOut();

                    shooter.target = shooter.ShooterGoalSpeed;

                    shooter.state = Shooter.ShooterState.Prep;

                    shooter.shooterLiftMiddle();

                    break;
                case Prep:


                    if(gamepad2.b){
                        shooter.state = Shooter.ShooterState.ActionPrepShooter;
                        shooter.push3.reset();
                    }

                    break;
                case ActionPrepShooter:

                    if(shooter.indexerUpSequence()){
                        shooter.state = Shooter.ShooterState.PrepShoot;
                    }

                    break;
                case PrepShoot:
                    if(gamepad2.left_trigger > .8){
                        shooter.target = shooter.ShooterPowerSpeedTele;
                        shooter.shooterLiftDown();
                    }

                    if(gamepad1.b){
                        shooter.push3.reset();
                        shooter.state = Shooter.ShooterState.ActionShoot;
                    }
                    break;
                case ActionShoot:
                    if(shooter.push3RingsWithoutPrep()){
                        shooter.state = Shooter.ShooterState.Collecting;

                        collectorOn = true;
                        collector.collector.setPower(1);
                        shooter.indexerDown();
                        shooter.shooterLiftUp();
                        spam.reset();
                    }
                    break;

            }

            if(gamepad1.right_bumper || gamepad2.right_bumper){
                shooter.shooterPusherBack();
                shooter.shooterLiftUp();
                spam.reset();

                switch(shooter.state) {

                    case Collecting:
                    case ActionPrep:
                    case Prep:
                        shooter.pusherBack();
                        shooter.state = Shooter.ShooterState.Collecting;
                        break;
                    case ActionPrepShooter:

                        break;
                    case PrepShoot:
                    case ActionShoot:
                        shooter.indexerDown();
                        shooter.state = Shooter.ShooterState.Collecting;
                        break;
                }



                collectorOn = true;
                collector.collector.setPower(1);
            }

            /*
            // Pusher Stuft

            //TODO
            if (gamepad1.b && !shooter.autoShoot) {
                shooter.autoShoot = true;
                shooter.push3.reset();

            }

            if (shooter.autoShoot) {
                shooter.push3Rings();
            }

            // Collector Stuff
            if (gamepad1.right_bumper || gamepad2.right_bumper) {
                collectorOn = true;
                collector.collector.setPower(1);
            }


            if (gamepad1.left_bumper || gamepad2.left_bumper) {
                collectorOn = false;
                collector.collector.setPower(0);
            }
             */


            if (collectorOn) {
                    if (gamepad1.right_trigger > 0 || gamepad1.left_trigger > 0) {
                        collector.collector.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
                    }


                    else {
                        collector.collector.setPower(1);
                    }
            }

            else {
                collector.collector.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
            }



            /*
            // Shooter Stuff
            //TODO
            if (gamepad1.right_bumper || gamepad2.right_bumper) {
                shooterOn = false;
                shooter.pusherBack();
                shooter.pusherWait();
                shooter.indexerDown();
                shooter.shooterLiftUp();
                shooter.shooter.setPower(0);
            }

            if ((gamepad1.left_bumper || gamepad2.left_bumper) && !shooter.indexerUp) {
                shooterOn = true;
                shooter.shooter.setPower(-1);
                if(shoot == 0) {
                    shooter.shooterLiftDown();
                }else{
                    shooter.shooterLiftMiddle();
                }
                shooter.pusherOut();
            }

            // Setting collector and shooter off
            if (gamepad1.a || gamepad2.a) {
                shooterOn = false;
                collectorOn = false;
                collector.collector.setPower(0);
                shooter.shooter.setPower(0);

            }

             */

            /* OLD
            if(gamepad2.left_stick_y < -0.5){
                shooter.shooterLiftMiddle();
                shoot = 1;
            }

            if(gamepad2.left_stick_y > 0.5){
                shoot = 0;
                shooter.shooterLiftDown();
            }
            */

            /*
            //TODO

            if(shooter.pusherTimerOn){
                shooter.pusherWait();
            }

            if(gamepad2.left_trigger > .7){
                shooter.shooter.setPower(-.7);
            }
             */

            if(action == AutoShoot.Not && gamepad1.y) {
                dt.resetTrackerWheels();
                dt.trackerWheels.updateAngle();
                dt.trackerWheels.robotAngle = new Angle();
                dt.trackerWheels.pos = new Pos();
                dt.trackerWheels.oldPos = new Pos();

                timer.reset();
                action = AutoShoot.Prep;
                shooter.target = shooter.ShooterPowerSpeedTele;
                shooter.shooterLiftDown();

            }

            if(loopTime.milliseconds() > 50) {
                dt.updateTrackerWheels(loopTime.seconds());

                if(shooter.target == 0){
                    if(gamepad1.left_trigger > .5){
                        shooter.shooter.setPower(-.5);
                    }else {
                        shooter.shooter.setPower(-0.5);
                    }
                }else{
                    shooter.update(loopTime.seconds());
                }

                switch (action) {
                    case Not:
                        if(gamepad1.dpad_left) {
                            while (angle.getDegrees() - dt.trackerWheels.pos.angle.getDegrees() < -180 || angle.getDegrees() - dt.trackerWheels.pos.angle.getDegrees() > 180){
                                if (angle.getDegrees() - dt.trackerWheels.pos.angle.getDegrees() < -180) {
                                    angle.setDegrees(angle.deg() + 360);
                                }
                                if (angle.getDegrees() - dt.trackerWheels.pos.angle.getDegrees() > 180) {
                                    angle.setDegrees(angle.deg() - 360);
                                }
                            }
                            dt.updateMovement(new Pos(0, 0, angle), new MotionProfile(0, 0), new MotionProfile(3, 4), loopTime.seconds(), gamepad1.dpad_left);
                        }
                        break;
                    case Prep:

                        shooter.pusherBack();
                        if(timer.seconds() > .3){
                            action = AutoShoot.Shoot1;
                            timer.reset();
                            shooter.indexerUp();
                            shooter.push3.reset();
                        }
                        break;
                    case Shoot1:
                        target = (new Pos(0, 0, new Angle(0)));
                        dt.updateMovement(target, moveProfile, rotProfile, loopTime.seconds(), true);
                        shooter.push1Ring();
                        if (timer.seconds() > 1) {
                            action = AutoShoot.ShootDrive1;
                            shooter.shooterPusherBack();
                        }

                        break;
                    case ShootDrive1:
                        target = (new Pos(0, 7.5, new Angle(0)));
                        dt.updateMovement(target, moveProfile, rotProfile, loopTime.seconds(), true);
                        if (target.atPos(dt.trackerWheels.pos, 1, 1)) {
                            action = AutoShoot.Shoot2;
                            timer.reset();
                            shooter.push3.reset();
                            dt.stop();
                        }
                        break;
                    case Shoot2:
                        target = (new Pos(0, 7.5, new Angle(0)));
                        dt.updateMovement(target, moveProfile, rotProfile, loopTime.seconds(), true);
                        shooter.push1RingWithoutPrep();
                        if (timer.seconds() > .5) {
                            action = AutoShoot.ShootDrive2;
                        }
                        break;
                    case ShootDrive2:
                        target = (new Pos(0, 15, new Angle(0)));
                        dt.updateMovement(target, moveProfile, rotProfile, loopTime.seconds(), true);
                        if (target.atPos(dt.trackerWheels.pos, 1, 1)) {
                            action = AutoShoot.Shoot3;
                            timer.reset();
                            shooter.push3.reset();
                            dt.stop();
                        }
                        break;
                    case Shoot3:
                        target = (new Pos(0, 15, new Angle(0)));
                        dt.updateMovement(target, moveProfile, rotProfile, loopTime.seconds(), true);

                        shooter.push1RingWithoutPrep();
                        if (timer.seconds() > .5) {
                            action = AutoShoot.Not;
                        }
                        break;
                }

                loopTime.reset();
            }


            telemetry.update();
        }
    }
}

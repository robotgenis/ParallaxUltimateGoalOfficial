package org.firstinspires.ftc.teamcode.teamcode.prometheus.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.Camera;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.Collector;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.DriveTrain;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.Shooter;
import org.firstinspires.ftc.teamcode.teamcode.prometheus.robot.WobbleArm;

public class Teleop extends LinearOpMode {
    DriveTrain dt;
    Shooter shooter;
    Collector collector;
    WobbleArm wobbleArm;
    Camera camera;

    @Override
    public void runOpMode() throws InterruptedException {
        dt = new DriveTrain(this);
        dt.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        shooter = new Shooter(this);

        collector = new Collector(this);

        wobbleArm = new WobbleArm(this, true);

        camera = new Camera(this);

        waitForStart();

        while (opModeIsActive()) {
            dt.setFromAxis(gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1.right_stick_x);

            if (gamepad1.dpad_up) {
                wobbleArm.wobbleArm.setPower(1);
            }

            else if (gamepad1.dpad_down) {
                wobbleArm.wobbleArm.setPower(-1);
            }

            else {
                wobbleArm.wobbleArm.setPower(0);
            }

            if (gamepad1.dpad_right) {
                wobbleArm.servoClose();
            }

            if (gamepad1.dpad_left) {
                wobbleArm.servoOpen();
            }
        }
    }
}

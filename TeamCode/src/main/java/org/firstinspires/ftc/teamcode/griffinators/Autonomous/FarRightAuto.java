package org.firstinspires.ftc.teamcode.griffinators.Autonomous;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.griffinators.Parts.ARM_POSITIONS;
import org.firstinspires.ftc.teamcode.griffinators.Parts.Arm;
import org.firstinspires.ftc.teamcode.griffinators.Parts.CLAW_ROTATION;
import org.firstinspires.ftc.teamcode.griffinators.Parts.Claw;
import org.firstinspires.ftc.teamcode.griffinators.Parts.Detection;

import java.util.ArrayList;

@Config
@Autonomous(name = "Far Right", group = "Auto")
public class FarRightAuto extends LinearOpMode
{
    public static class Params{
        public double _0initX = -24;
        public double _0initY = -60;
        public double _0initRot = Math.PI / 2;

        public double _1DetectionStrafeX = -5.2;

        public double _2strafeFrontY = 24.1;
        public double _2strafeFrontX = -5;

        public double _2strafeRightY = 13;
        public double _2strafeRightX = -11.8;

        public double _2splineLeftY = 30;
        public double _2splineLeftX = -0.5;
        public double _2splineLeftRot = -0.14;

        public double _3splineMiddleY = 56;
        public double _3splineMiddleX = 5;
        public double _3splineMiddleRot = 0;

        public double _3splineBoardY = 25;
        public double _3splineBoardX = 72;
        public double _3splineBoardRot = -0.05;

        public double _4strafeBoardFrontY = -2;
        public double _4strafeBoardFrontX = 14;

        public double _4strafeBoardRightY = 2.8;
        public double _4strafeBoardRightX = 13.5;

        public double _4strafeBoardLeftY = -10.8;
        public double _4strafeBoardLeftX = 14;

    }
    public static Params params = new Params();
    private MecanumDrive drive;
    private Detection detection;
    private Claw claw;
    private Arm arm;
    @Override
    public void runOpMode() throws InterruptedException
    {
        drive = new MecanumDrive(hardwareMap, new Pose2d(params._0initX, params._0initY, params._0initRot));
        detection = new Detection(hardwareMap, "R");
        claw = new Claw(hardwareMap);
        arm = new Arm(hardwareMap);

        Action moveToRecognitionPosition = drive.actionBuilder(drive.pose)
                .strafeTo(drive.pose.position.plus(new Vector2d(params._1DetectionStrafeX, 0)))
                .build();

        Action moveToLeftPixelPos = drive.actionBuilder(drive.pose)
                .splineTo(drive.pose.position.plus(new Vector2d(params._2splineLeftX, params._2splineLeftY)), params._2splineLeftRot)
                .build();
        Action moveToCenterPixelPos = drive.actionBuilder(drive.pose)
                .strafeTo(drive.pose.position.plus(new Vector2d(params._2strafeFrontX, params._2strafeFrontY)))
                .build();
        Action moveToRightPixelPos = drive.actionBuilder(drive.pose)
                .strafeTo(drive.pose.position.plus(new Vector2d(params._2strafeRightX, params._2strafeRightY)))
                .build();


        Pose2d nearBoardPos = new Pose2d(drive.pose.position.plus(new Vector2d(params._3splineBoardX, params._3splineBoardY)), params._3splineBoardRot);

        Action moveToBoard = drive.actionBuilder(new Pose2d(drive.pose.position.plus(new Vector2d(0, 10)), drive.pose.heading))
                .strafeTo(drive.pose.position.plus(new Vector2d(-13.5, 10)))
                .splineToConstantHeading(drive.pose.position.plus(new Vector2d(params._3splineMiddleX, params._3splineMiddleY)), params._3splineMiddleRot)
                .strafeTo(drive.pose.position.plus(new Vector2d(params._3splineMiddleX + 56, params._3splineMiddleY)))
                .splineToLinearHeading(new Pose2d(nearBoardPos.position, 0), params._3splineBoardRot, drive.defaultVelConstraint, drive.defaultAccelConstraint)
                .build();

        Action moveToLeftBoard = drive.actionBuilder(nearBoardPos)
                .strafeTo(nearBoardPos.position.plus(new Vector2d(params._4strafeBoardLeftX, params._4strafeBoardLeftY)))
                .build();
        Action moveToCenterBoard = drive.actionBuilder(nearBoardPos)
                .strafeTo(nearBoardPos.position.plus(new Vector2d(params._4strafeBoardFrontX, params._4strafeBoardFrontY)))
                .build();
        Action moveToRightBoard = drive.actionBuilder(nearBoardPos)
                .strafeTo(nearBoardPos.position.plus(new Vector2d(params._4strafeBoardRightX, params._4strafeBoardRightY)))
                .build();

        Action park = drive.actionBuilder(nearBoardPos)
                .strafeTo(nearBoardPos.position.plus(new Vector2d(0, 25)))
                .strafeTo(nearBoardPos.position.plus(new Vector2d(28, 25)))
                .strafeTo(nearBoardPos.position.plus(new Vector2d(28, 17)))
                .build();

        claw.closeLeft();
        claw.closeRight();
        sleep(200);
        claw.controlRotation(CLAW_ROTATION.HIDDEN);
        arm.setPosition(ARM_POSITIONS.GROUND, false);
        waitForStart();
        detection.stream();
        Actions.runBlocking(moveToRecognitionPosition);
        recognitionPos();

        ArrayList<Recognition> recognitions = new ArrayList<>();
        sleep(400);
        Actions.runBlocking(detection.getRecognition(recognitions, 5));
        int pixelPos = parsePixelPos(recognitions);
        telemetry.addData("Pixel pos", "" + pixelPos);
        telemetry.update();
        claw.controlRotation(CLAW_ROTATION.GROUND);
        sleep(700);
        arm.setPosition(ARM_POSITIONS.GROUND, true);
        switch (pixelPos){
            case 0:
                Actions.runBlocking(moveToLeftPixelPos);
                break;
            case 1:
                Actions.runBlocking(moveToCenterPixelPos);
                break;
            case 2:
                Actions.runBlocking(moveToRightPixelPos);
                break;
        }
        claw.openRight();
        sleep(300);
        claw.controlRotation(CLAW_ROTATION.BOARD);
        sleep(300);
        claw.closeRight();
        Actions.runBlocking(moveToBoard);
        switch (pixelPos)
        {
            case 0:
                Actions.runBlocking(moveToLeftBoard);
                break;
            case 1:
                Actions.runBlocking(moveToCenterBoard);
                break;
            case 2:
                Actions.runBlocking(moveToRightBoard);
                break;
        }

        arm.setPosition(ARM_POSITIONS.BOARD, true);
        sleep(300);
        claw.openLeft();

        Action returnToBoard = drive.actionBuilder(drive.pose)
                .strafeTo(nearBoardPos.position)
                .build();

        Actions.runBlocking(returnToBoard);
        claw.closeRight();
        claw.controlRotation(CLAW_ROTATION.HIDDEN);
        arm.setRotation(0, false);
        arm.setExtension(0, false);
        Actions.runBlocking(park);
    }

    private int parsePixelPos(ArrayList<Recognition> recognitions)
    {
        if (recognitions.size() == 0)
        {
            return 0;
        }
        else
        {
            if ((recognitions.get(0).getRight() + recognitions.get(0).getLeft()) / 2 < 320)
            {
                return 2;
            }
            else
            {
                return 1;
            }
        }
    }

    private void recognitionPos()
    {
        claw.controlRotation(CLAW_ROTATION.DETECTION);
        arm.setPosition(ARM_POSITIONS.DETECTION, true);
        sleep(300);
    }

    public void wait(int time)
    {
        sleep(time);
    }
}


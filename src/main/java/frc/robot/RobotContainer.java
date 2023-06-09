// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.ControllerConstants;
import frc.robot.GlobalVars.DriveConfig;
import frc.robot.commands.ArcadeDriveCommand;
// import frc.robot.commands.PrimeShooterCommand;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.PrimerSubsystem;
import frc.robot.subsystems.ShooterSubsystem;

public class RobotContainer {
  private DriveSubsystem m_robotDrive;
  private ShooterSubsystem m_robotShooter;
  private PrimerSubsystem m_robotPrimer;
  private IntakeSubsystem m_robotIntake;
  private final CommandXboxController m_driverController;
  private boolean runHopper;

  
  
  public RobotContainer() {
    runHopper = true;
    m_driverController = new CommandXboxController(ControllerConstants.kXboxControllerPort);
    
    m_robotDrive = new DriveSubsystem();
    m_robotShooter = new ShooterSubsystem();
    m_robotPrimer = new PrimerSubsystem();
    m_robotIntake = new IntakeSubsystem();

    m_robotDrive.setDefaultCommand(new ArcadeDriveCommand(
      () -> m_driverController.getLeftY(),
      () -> m_driverController.getRightX(),
      m_robotDrive,
      true
    ));

    // m_robotPrimer.setDefaultCommand(new PrimeShooterCommand(m_robotPrimer, m_robotShooter));

    configureBindings();
  }

  private void configureBindings() {
    m_driverController.x().whileTrue(new InstantCommand(() -> m_robotPrimer.runShooterIntake()));
    m_driverController.x().onFalse(new InstantCommand(() -> m_robotPrimer.stopCargoIntake()));

    m_driverController.b().whileTrue(new InstantCommand(() -> m_robotPrimer.expelBall()));
    m_driverController.b().onFalse(new InstantCommand(() -> m_robotPrimer.stopCargoIntake()));
    
    m_driverController.a().onTrue(new InstantCommand(() -> {
      if(runHopper) {
        runHopper = false;
        m_robotShooter.shootNow();
        m_robotPrimer.runHopper();
      }
      else {
        runHopper = true;
        m_robotShooter.stopShooters();
        m_robotPrimer.stopHopper();
      }
    }));
    
    // Shooter Extend
    m_driverController.rightBumper().onTrue(new InstantCommand(()-> {
      runHopper = false;
      m_robotPrimer.runHopper();
      m_robotIntake.newExtend();
    }));

    // Shooter Retract
    m_driverController.leftBumper().onTrue(new InstantCommand(()-> {
      m_robotIntake.newRetract();
      runHopper = true;
      m_robotPrimer.stopHopper();
    }));

    // Hopper
    m_driverController.leftTrigger()
      .whileTrue(new InstantCommand(() -> {if(runHopper) m_robotPrimer.runReverseHopper();}))
      .onFalse(new InstantCommand(() -> m_robotPrimer.stopHopper()));

    m_driverController.rightTrigger()
      .whileTrue(new InstantCommand(() -> {if(runHopper) m_robotPrimer.runHopper();}))
      .onFalse(new InstantCommand(() -> m_robotPrimer.stopHopper()));

    // d-Pad
    m_driverController.povRight().onTrue(new InstantCommand(() -> {
      changeShooterSpeed(1);
    }));
    
    m_driverController.povLeft().onTrue(new InstantCommand(() -> {
      changeShooterSpeed(0.5);
    }));

    m_driverController.povUp()
      .whileTrue(new InstantCommand(() -> m_robotShooter.extendHood()))
      .onFalse(new InstantCommand(() -> m_robotShooter.haltHood()));

    m_driverController.povDown()
      .whileTrue(new InstantCommand(() -> m_robotShooter.retractHood()))
      .onFalse(new InstantCommand(() -> m_robotShooter.haltHood()));  
    
  }
  
  public void changeShooterSpeed(double newSpeed){
    DriveConfig.gDriverSpeed = newSpeed;
    
    // If shooter is running it reassigns updated speed
    if(!runHopper) m_robotShooter.shootNow();
  }

  public Command getAutonomousCommand() {
    return null;
  }
}

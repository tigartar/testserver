/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.application.Application
 *  javafx.event.Event
 *  javafx.event.EventTarget
 *  javafx.fxml.FXMLLoader
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.scene.control.TabPane
 *  javafx.scene.image.Image
 *  javafx.scene.input.MouseButton
 *  javafx.scene.input.MouseEvent
 *  javafx.scene.input.TouchEvent
 *  javafx.scene.input.TouchPoint
 *  javafx.stage.Stage
 */
package com.wurmonline.server.gui;

import com.wurmonline.server.Servers;
import com.wurmonline.server.gui.GuiCommandLineArgument;
import com.wurmonline.server.gui.WurmServerGuiController;
import com.wurmonline.server.utils.SimpleArgumentParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import javafx.stage.Stage;

public final class WurmServerGuiMain
extends Application {
    private static final Logger logger = Logger.getLogger(WurmServerGuiMain.class.getName());

    public static void main(String[] args) {
        logger.info("WurmServerGuiMain starting");
        HashSet<String> allowedArgStrings = new HashSet<String>();
        for (GuiCommandLineArgument argument : GuiCommandLineArgument.values()) {
            allowedArgStrings.add(argument.getArgumentString());
        }
        SimpleArgumentParser parser = new SimpleArgumentParser(args, allowedArgStrings);
        String dbToStart = "";
        if (parser.hasOption(GuiCommandLineArgument.START.getArgumentString()) && ((dbToStart = parser.getOptionValue(GuiCommandLineArgument.START.getArgumentString())) == null || dbToStart.isEmpty())) {
            System.err.println("Start param needs to be followed by server dir: Start=<ServerDir>");
            System.exit(1);
        }
        Servers.arguments = parser;
        String adminPass = "";
        if (parser.hasOption(GuiCommandLineArgument.ADMIN_PWD.getArgumentString())) {
            adminPass = parser.getOptionValue(GuiCommandLineArgument.ADMIN_PWD.getArgumentString());
            if (adminPass == null || adminPass.isEmpty()) {
                System.err.println("The admin password needs to be set or it will not be possible to change the settings within the game.");
            } else {
                WurmServerGuiController.adminPassword = adminPass;
            }
        }
        if (!dbToStart.isEmpty()) {
            System.out.println("Should start without GUI here!");
            WurmServerGuiController.startDB(dbToStart);
        } else {
            Application.launch(WurmServerGuiMain.class, (String[])null);
        }
        logger.info("WurmServerGuiMain finished");
    }

    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(WurmServerGuiMain.class.getResource("WurmServerGui.fxml"));
            TabPane page = (TabPane)loader.load();
            Scene scene = new Scene((Parent)page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Wurm Unlimited Server");
            primaryStage.addEventFilter(TouchEvent.ANY, event -> {
                event.consume();
                TouchPoint touchPoint = event.getTouchPoint();
                int clickCount = 1;
                MouseEvent mouseEvent = new MouseEvent(event.getSource(), event.getTarget(), MouseEvent.MOUSE_CLICKED, touchPoint.getX(), touchPoint.getY(), touchPoint.getScreenX(), touchPoint.getScreenY(), MouseButton.PRIMARY, clickCount, false, false, false, false, true, false, false, true, false, false, null);
                Scene yourScene = primaryStage.getScene();
                Event.fireEvent((EventTarget)yourScene.getRoot(), (Event)mouseEvent);
            });
            ArrayList<Image> iconsList = new ArrayList<Image>();
            iconsList.add(new Image("com/wurmonline/server/gui/img/icon2_16.png"));
            iconsList.add(new Image("com/wurmonline/server/gui/img/icon2_32.png"));
            iconsList.add(new Image("com/wurmonline/server/gui/img/icon2_64.png"));
            iconsList.add(new Image("com/wurmonline/server/gui/img/icon2_128.png"));
            primaryStage.getIcons().addAll(iconsList);
            primaryStage.show();
            WurmServerGuiController controller = (WurmServerGuiController)loader.getController();
            controller.setStage(primaryStage);
            scene.getWindow().setOnCloseRequest(ev -> {
                if (!controller.shutdown()) {
                    ev.consume();
                }
            });
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}


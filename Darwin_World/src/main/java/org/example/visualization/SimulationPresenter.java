package org.example.visualization;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.*;

import org.example.model.*;
import org.example.simulation.Simulation;
import org.example.data.SimulationConfiguration;
import org.example.simulation.SimulationEngine;


public class SimulationPresenter implements MapChangeListener {
    private static final int CELL_SIZE = 15;
    private static final List<Simulation> simulations = new ArrayList<>();
    private Simulation simulation;
    @FXML
    private GridPane mapGrid;
    private WorldMap worldMap;
    private SimulationConfiguration configuration;

    private final HashSet<WorldElement> setAnimals = new HashSet<>();

    private void drawMap(WorldMap worldMap){
        clearGrid();
        mapGrid.setGridLinesVisible(true);
        Boundary currentBounds = worldMap.getCurrentBounds();
        int cols = Math.abs(currentBounds.upperRight().getX()-currentBounds.lowerLeft().getX())+1;
        int rows = Math.abs(currentBounds.upperRight().getY()-currentBounds.lowerLeft().getY())+1;
        Vector2d currentPosition = new Vector2d(currentBounds.lowerLeft().getX(),currentBounds.upperRight().getY());
        addCells(cols, rows);
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                Vector2d addVector = new Vector2d(i,-j);
                Label label = new Label();

                if(worldMap.isOccupied(currentPosition.add(addVector))){
                    setIcon(label, worldMap.objectAt(currentPosition.add(addVector)));
                }
                GridPane.setHalignment(label, HPos.CENTER);
                addLabel(label, i, j);
            }
        }
    }

    private void addLabel(Label label, int i, int j) {
        GridPane.setHalignment(label, HPos.CENTER);
        label.setPrefWidth(CELL_SIZE);
        label.setPrefHeight(CELL_SIZE);
        label.setPadding(Insets.EMPTY);
        mapGrid.add(label, i, j);
    }

    private void setIcon(Label label, List<WorldElement> worldElements) {
        WorldElement worldElement = worldElements.get(0);
        label.setStyle("-fx-background-color: "+ worldElement.toIcon());
        label.setOnMouseClicked(event -> {
            if(worldElement instanceof Animal){
                setAnimals.add(worldElement);
            }
        });
        if(setAnimals.contains(worldElement)){
            label.setStyle("-fx-background-color: #c9a2bf");
        }
    }

    private void addCells(int cols, int rows) {
        for (int i = 0; i < cols; i++) {
            mapGrid.getColumnConstraints().add(new ColumnConstraints(CELL_SIZE));
        }
        for (int i = 0; i < rows; i++) {
            mapGrid.getRowConstraints().add(new RowConstraints(CELL_SIZE));
        }
    }

    private void clearGrid() {
        mapGrid.getChildren().retainAll(mapGrid.getChildren().get(0));
        mapGrid.getColumnConstraints().clear();
        mapGrid.getRowConstraints().clear();
    }
    private static int simulationID = 0;
    public int startSimulationPresenter(SimulationConfiguration configuration, String mapType) {
        setOptions(configuration, mapType);
        increaseID();
        try {
            simulation = new Simulation(configuration.getAnimalsNumber(), configuration.getGenNumbers(), worldMap,
                    configuration.getAnimalEnergy(), configuration.getReadyEnergy(), configuration.getReproductionEnergy(),
                    configuration.getGrassEnergy(), configuration.getGrassNum(), simulationID);
            simulations.add(simulation);

            SimulationEngine simulationEngine = new SimulationEngine(simulations);
            simulationEngine.runAsyncInThreadPool();


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return simulation.getID();
    }

    private void setOptions(SimulationConfiguration configuration, String mapType) {
        //        Kiedyś bedzie działać lepiej
        if(Objects.equals(mapType, "RectangularMap")){
            worldMap = new RectangularMap(configuration.getMapWidth(), configuration.getMapHeight(),1);
        }else {
            worldMap = new RectangularMap(configuration.getMapWidth(), configuration.getMapHeight(),2);
        }
        worldMap.registerObserver(this);
        this.configuration = configuration;
    }

    private void increaseID() {
        synchronized (this){
            simulationID += 1;
        }
    }

    @FXML
    public void onContinueClicked(){
        simulation.continueSimulation();
    }

    @FXML void onStopClicked(){
        simulation.stopSimulation();
    }

    @Override
    public void mapChanged(WorldMap worldMap, String message) {
        Platform.runLater(() -> {
            drawMap(worldMap);
        });
    }
}
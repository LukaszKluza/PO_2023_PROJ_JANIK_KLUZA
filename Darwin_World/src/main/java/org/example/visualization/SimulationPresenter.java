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

import org.example.data.SimulationStatistics;
import org.example.data.Statistics;
import org.example.model.*;
import org.example.simulation.Simulation;
import org.example.data.SimulationConfiguration;
import org.example.simulation.SimulationEngine;


public class SimulationPresenter implements MapChangeListener {
    private static final int CELL_SIZE = 15;
    private static final List<Simulation> simulations = new ArrayList<>();
    private static int simulationID = 0;
    private Simulation simulation;
    private SimulationStatistics simulationStatistics;
    @FXML
    private GridPane mapGrid;
    @FXML
    private Label mapType;
    @FXML
    private Label genomType;
    @FXML
    private Label day;
    @FXML
    private Label allAnimals;
    @FXML
    private Label livingAnimals;
    @FXML
    private Label grass;
    @FXML
    private Label freeFields;
    @FXML
    private Label genotype;
    @FXML
    private Label avgEnergy;
    @FXML
    private Label avgLife;
    @FXML
    private Label avgChildren;
    @FXML
    private Label animalId;
    @FXML
    private Label lengthOfLife;
    @FXML
    private Label energy;
    @FXML
    private Label numOfChildren;
    @FXML
    private Label deathDate;

    private WorldMap worldMap;
    private SimulationConfiguration configuration;
    private final Map<Statistics, Label> mapLabelStatistics = new HashMap<>();

    private Animal chosen = null;

    public int startSimulationPresenter(SimulationConfiguration configuration, int mapType) {
        setOptions(configuration, mapType);
        runStatistics();
        increaseID();
        try {
            simulation = new Simulation(configuration, worldMap, simulationID);
            simulationStatistics = simulation.getSimulationStatistics();

            simulations.add(simulation);

            SimulationEngine simulationEngine = new SimulationEngine(simulations);
            simulationEngine.runAsyncInThreadPool();


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return simulation.getID();
    }

    public void runStatistics() {
        mapLabelStatistics.put(Statistics.DAY, day);
        mapLabelStatistics.put(Statistics.MAP_TYPE, mapType);
        mapLabelStatistics.put(Statistics.GENOM_TYPE, genomType);
        mapLabelStatistics.put(Statistics.NUMBER_OF_ALL_ANIMALS, allAnimals);
        mapLabelStatistics.put(Statistics.NUMBER_OF_LIVING_ANIMALS, livingAnimals);
        mapLabelStatistics.put(Statistics.FIELD_WITH_GRASS, grass);
        mapLabelStatistics.put(Statistics.FREE_FIELDS, freeFields);
        mapLabelStatistics.put(Statistics.MOST_POPULAR_GENOTYPE, genotype);
        mapLabelStatistics.put(Statistics.AVG_ANIMALS_ENERGY, avgEnergy);
        mapLabelStatistics.put(Statistics.AVG_LENGTH_OF_LIFE, avgLife);
        mapLabelStatistics.put(Statistics.AVG_NUMBER_OF_CHILDREN, avgChildren);
    }

    private void drawMap(int day){
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
                    setIcon(label, worldMap.objectAt(currentPosition.add(addVector)), day);
                }
                GridPane.setHalignment(label, HPos.CENTER);
                if(chosen != null && chosen.position().equals(currentPosition.add(addVector))){
                    label.setStyle("-fx-background-color: #c9a2bf");
                    displayAnimalStatistics(chosen);
                }
                addLabel(label, i, j);
            }
        }
    }

    private void displayAnimalStatistics(Animal animal) {
        animalId.setText(Integer.toString(animal.getID()));
        lengthOfLife.setText(Integer.toString(animal.getAge()));
        energy.setText(Integer.toString(animal.getEnergy()));
        numOfChildren.setText(Integer.toString(animal.getNumOfChildren()));
        if(animal.getDayOfDeath().isPresent()) {
            deathDate.setText(Integer.toString(animal.getDayOfDeath().orElse(null)));
        }
        else deathDate.setText("-");
    }

    private void addLabel(Label label, int i, int j) {
        GridPane.setHalignment(label, HPos.CENTER);
        label.setPrefWidth(CELL_SIZE);
        label.setPrefHeight(CELL_SIZE);
        label.setPadding(Insets.EMPTY);
        mapGrid.add(label, i, j);
    }

    private void setIcon(Label label, List<WorldElement> worldElements, int day) {
        WorldElement worldElement = worldElements.get(0);
        label.setStyle("-fx-background-color: "+ worldElement.toIcon());
        label.setOnMouseClicked(event -> {
            if(worldElement instanceof Animal){
                chosen = (Animal) worldElement;
                mapChanged(worldMap, day);
            }
        });
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

    private void setOptions(SimulationConfiguration configuration, int mapType) {
        //        Kiedyś bedzie działać lepiej
        if(mapType == 1){
            worldMap = new RectangularMap(1, configuration.getMapWidth(), configuration.getMapHeight());
        }else {
            worldMap = new HellPortal(2, configuration.getMapWidth(), configuration.getMapHeight());
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

    @FXML
    public void onStopClicked(){
        simulation.stopSimulation();
    }

    @Override
    public void mapChanged(WorldMap worldMap, int day) {
        Platform.runLater(() -> {
            drawMap(day);
            statisticsUpdate(day);
        });
    }

    public void statisticsUpdate(int day) {
        simulationStatistics.updateStatistic(day);
        Map<Statistics, Double> mapStatistics = simulationStatistics.getMapStatistics();
        mapLabelStatistics.forEach((name, label) -> {
            if(name == Statistics.MAP_TYPE) {
                if(mapStatistics.get(name) == 1) {
                    label.setText("Earth");
                }
                else {
                    label.setText(("Hell Portal"));
                }
            }
            else if(name == Statistics.GENOM_TYPE) {
                if(mapStatistics.get(name) == 1) {
                    label.setText("Normal genome");
                }
                else {
                    label.setText(("Back and Forward"));
                }
            }
            else {
                label.setText(mapStatistics.get(name).toString());
            }
        });
    }
}

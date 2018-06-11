package mjr;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class Controller {
    public Spinner approxDegree;
    public LineChart chart;
    public NumberAxis xAxis;
    public NumberAxis yAxis;
    public ToggleButton modeChanger;
    private FileChooser fileChooser = new FileChooser();
    private Approximator approx = new Approximator();
    private double mouseChartX, mouseChartY;
    private int dotIndex;
    private double startX;
    private double startY;
    private double startMinX, startMaxX, startMinY, startMaxY;

    private void initChart() {
        chart.setAnimated(false);

        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);
        yAxis.setAutoRanging(false);
        yAxis.setForceZeroInRange(false);

        xAxis.setTickUnit(1);
        yAxis.setTickUnit(1);

        double padding = 4;

        double[] sortedX = approx.getX();
        Arrays.sort(sortedX);
        double minX = sortedX[0];
        double maxX = sortedX[sortedX.length - 1];
        double borderWidth = Math.abs(maxX - minX) / padding;

        double[] sortedY = approx.getY();
        Arrays.sort(sortedY);
        double minY = sortedY[0];
        double maxY = sortedY[sortedY.length - 1];
        double borderHeight = Math.abs(maxY - minY) / padding;

        drawPoint(approx.getX(), approx.getY());

        xAxis.setLowerBound(minX - borderWidth);
        yAxis.setLowerBound(minY - borderHeight);

        xAxis.setUpperBound(maxX + borderWidth);
        yAxis.setUpperBound(maxY + borderHeight);
    }

    private void drawPoint(double[] X, double[] Y) {
        chart.getData().removeAll(chart.getData());
        XYChart.Series points = new XYChart.Series();
        chart.getData().add(points);

        for (int i = 0; i < X.length; ++i)
            points.getData().add(new XYChart.Data<>(X[i], Y[i]));

    }

    private void drawPolynom(double leftBound, double rightBound, double lowerBound, double upperBound) {
        if (chart.getData().size() > 1)
            chart.getData().removeAll(chart.getData().get(1));
        XYChart.Series approxPolynom = new XYChart.Series();
        chart.getData().add(approxPolynom);

        double dx = 0.05;
        for (double y, x = leftBound; x <= rightBound; x += dx) {
            y = approx.getValue(x);
            approxPolynom.getData().add(new XYChart.Data<>(x, y));
        }

        xAxis.setLowerBound(leftBound);
        yAxis.setLowerBound(lowerBound);

        xAxis.setUpperBound(rightBound);
        yAxis.setUpperBound(upperBound);
    }

    private void setMouseXYOnChart(MouseEvent mouseEvent) {
        Point2D pointInScene = new Point2D(mouseEvent.getSceneX(), mouseEvent.getSceneY());

        double xPosInAxis = xAxis.sceneToLocal(new Point2D(pointInScene.getX(), 0)).getX();
        double yPosInAxis = yAxis.sceneToLocal(new Point2D(0, pointInScene.getY())).getY();

        this.mouseChartX = xAxis.getValueForDisplay(xPosInAxis).doubleValue();
        this.mouseChartY = yAxis.getValueForDisplay(yPosInAxis).doubleValue();
    }

    public void drawApprox(ActionEvent actionEvent) {
        approx.createPolynom((int) approxDegree.getValue());
        drawPolynom(xAxis.getLowerBound(), xAxis.getUpperBound(), yAxis.getLowerBound(), yAxis.getUpperBound());
    }

    public void openFile(ActionEvent actionEvent) throws FileNotFoundException {
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            approx.readFromFile(file);
            approxDegree.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, approx.getMaxDegree(), 1));
            initChart();
        }
    }

    public void zoomChart(ScrollEvent zoomEvent) {
        double minX = xAxis.getLowerBound();
        double maxX = xAxis.getUpperBound();
        double minY = yAxis.getLowerBound();
        double maxY = yAxis.getUpperBound();
        double zoomScale = 3;
        double borderWidth = (maxX - minX) / zoomScale;
        double borderHeight = (maxY - minY) / zoomScale;
        if (zoomEvent.getDeltaY() > 1) {
            if (maxX - minX <= 1 || (maxX - borderWidth <= 0) || (maxY - borderHeight <= 0)) return;

            drawPolynom(minX + borderWidth, maxX - borderWidth,
                    minY + borderHeight, maxY - borderHeight);
        } else {
            if (maxX - minX >= 50) return;

            drawPolynom(minX - borderWidth, maxX + borderWidth,
                    minY - borderHeight, maxY + borderHeight);
        }
    }

    public void chartPressed(MouseEvent mouseEvent) {
        setMouseXYOnChart(mouseEvent);

        this.startX = mouseEvent.getSceneX();
        this.startY = mouseEvent.getSceneY();

        this.startMinX = xAxis.getLowerBound();
        this.startMaxX = xAxis.getUpperBound();
        this.startMinY = yAxis.getLowerBound();
        this.startMaxY = yAxis.getUpperBound();

        if (modeChanger.isSelected()) {
            double diff = Double.MAX_VALUE;

            ObservableList<XYChart.Data> points = ((XYChart.Series) chart.getData().get(0)).getData();
            for (int i = 0; i < points.size(); ++i) {
                XYChart.Data data = points.get(i);
                double xData = (double) data.getXValue();
                double dataDistance = Math.abs(mouseChartX - xData);
                if (dataDistance < diff) {
                    diff = dataDistance;
                    this.dotIndex = i;
                }
            }
        }
    }

    public void chartDragged(MouseEvent mouseEvent) {
        setMouseXYOnChart(mouseEvent);

        if (modeChanger.isSelected()) {
            approx.setYByIndex(dotIndex, this.mouseChartY);
            drawPoint(approx.getX(), approx.getY());
            drawPolynom(this.startMinX, this.startMaxX, this.startMinY, this.startMaxY);
        } else {
            double dx = startX - mouseEvent.getSceneX();
            double dy = startY - mouseEvent.getSceneY();

            dx /= xAxis.getScale();
            dy /= yAxis.getScale();

            drawPolynom(this.startMinX + dx, this.startMaxX + dx,
                    this.startMinY + dy, this.startMaxY + dy);
        }
    }

    public void changeMode(ActionEvent actionEvent) {
        if (modeChanger.isSelected()) modeChanger.setText("Режим редактирования ВКЛ");
        else modeChanger.setText("Режим редактирования ВЫКЛ");
    }
}

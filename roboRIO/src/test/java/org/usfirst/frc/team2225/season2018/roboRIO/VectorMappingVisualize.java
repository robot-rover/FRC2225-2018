package org.usfirst.frc.team2225.season2018.roboRIO;

import org.jfree.chart.*;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class VectorMappingVisualize extends ApplicationFrame {
    private final double[][] delta = new double[][]{
            new double[]{1, 0}, new double[]{0, 1}, new double[]{-1, 0}, new double[]{0, -1}
    };
    XYSeriesCollection dataset;
    ChartPanel panel;

    /**
     * Constructs a new application frame.
     *
     * @param title the frame title.
     */
    public VectorMappingVisualize(String title) {
        super(title);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        dataset = new XYSeriesCollection();
        //drawPlot(51);
        JFreeChart chart = ChartFactory.createXYLineChart("Map Square to Circle", "X", "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
        panel = new ChartPanel(chart);
        panel.setMouseZoomable(false);
        XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setAutoRange(false);
        plot.getRangeAxis().setLowerBound(-10);
        plot.getRangeAxis().setUpperBound(10);
        plot.getDomainAxis().setAutoRange(false);
        plot.getDomainAxis().setLowerBound(-10);
        plot.getDomainAxis().setUpperBound(10);
        panel.setPreferredSize(new java.awt.Dimension(800, 800));
        panel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                MouseEvent click = event.getTrigger();
                Rectangle2D dataArea = panel.getScreenDataArea();
                XYSeries series = new XYSeries(event.hashCode());
                XYPlot plot = (XYPlot) event.getChart().getPlot();
                ValueAxis xAxis = plot.getDomainAxis();
                ValueAxis yAxis = plot.getRangeAxis();
                Vector2D vec = new Vector2D(
                        xAxis.java2DToValue(event.getTrigger().getX(), dataArea,
                                RectangleEdge.BOTTOM),
                        yAxis.java2DToValue(event.getTrigger().getY(), dataArea,
                                RectangleEdge.LEFT));
                System.out.println(vec);
                series.add(vec.x, vec.y);
                vec.mapSquareToCircle();
                series.add(vec.x, vec.y);
                dataset.addSeries(series);
                plot.clearAnnotations();
                plot.getRenderer().setSeriesStroke(dataset.getSeriesCount() - 1, new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
                List<XYAnnotation> a = new ArrayList<>();
                double radius = vec.magnitude();
                System.out.println(radius);
                a.add(new XYLineAnnotation(-radius, radius, radius, radius));
                a.add(new XYLineAnnotation(-radius, radius, -radius, -radius));
                a.add(new XYLineAnnotation(radius, radius, radius, -radius));
                a.add(new XYLineAnnotation(radius, -radius, -radius, -radius));
                a.add(new XYShapeAnnotation(new Ellipse2D.Double(-radius, -radius, 2 * radius, 2 * radius)));
                a.stream().forEach(v -> plot.addAnnotation(v));
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {

            }
        });
        add(panel, BorderLayout.CENTER);
        JSlider slider = new JSlider(3, 100, 51);
        slider.addChangeListener((ChangeEvent e) -> {
            //drawPlot(slider.getValue());
        });
        add(slider, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        VectorMappingVisualize chart = new VectorMappingVisualize("Title");
        chart.pack();
        chart.setVisible(true);
    }

    public void drawPlot(int totalSideLength) {
        dataset.removeAllSeries();
        int x = 0;
        int y = 0;
        totalSideLength -= totalSideLength % 2;
        ArrayList<double[]> data = new ArrayList<>();
        for (int stage = 0; stage < delta.length; stage++) {
            for (int sideLength = 0; sideLength < totalSideLength; sideLength++) {
                data.add(new double[]{x - (totalSideLength) / 2, y - (totalSideLength) / 2});
                x += delta[stage][0];
                y += delta[stage][1];
            }
        }
        XYSeries square = new XYSeries("Square");
        for (double[] point : data) {
            square.add(point[0], point[1]);
        }
        dataset.addSeries(square);
        XYSeries circle = new XYSeries("Circle");
        for (double[] point : data) {
            Vector2D vec = new Vector2D(point[0], point[1]).mapSquareToCircle();
            circle.add(vec.x, vec.y);
        }
        dataset.addSeries(circle);
    }
}
